package firstplayer;

import battlecode.common.*;
import bcutils.Actions.*;
import bcutils.Broadcasting;
import bcutils.Utils;

import java.util.ArrayList;

public strictfp class BotScout extends Bot {
  public static final int deathTime = 8; //rounds
    public static int currentGardenerID = -1;
    public static int currentGardenerIndex = -1;
    private static int victoryRound = 0;
    private static int[] deathIDs = new int[25];
    private static int[] deathRounds = new int[25];
    private static boolean[] deathIsArchon = new boolean[25];
    private static boolean respondingToDistress = false;
    private static int roundRespondedToDistress = 0;

  public static void loop(RobotController rc_) {
    System.out.println("I'm a Scout!");
    Bot.init(rc_);
    int endTurnRoundNum;
    while (true) {
      try {
        update();
        doTurn();
      } catch (GameActionException e) {
        System.out.println(e.toString());
        e.printStackTrace();
      }
      endTurnRoundNum = rc.getRoundNum();
      if (roundNum < endTurnRoundNum) {
        System.out.println("Over bytecode limit!");
      }
      Clock.yield();
    }
  }

  public static void doTurn() throws GameActionException {
    explore();
    tryShake();
    harass();
    sendReceiveIntel();
    reportTheDead();
  }
  
  public static void tryShake() throws GameActionException {
      try {
          TreeInfo[] neutralTrees = rc.senseNearbyTrees(myType.bodyRadius+1, Team.NEUTRAL);
          for (TreeInfo tree : neutralTrees) {
              if (rc.canShake(tree.ID) && tree.containedBullets > 0) {
                  System.out.printf("Shaking tree %d", tree.ID);
                  rc.shake(tree.ID);
              }
          }
      }
      catch (GameActionException e)
      {
          System.out.println(e.getStackTrace());
      }

  }

  static void explore() throws GameActionException {
      if (!evade())
      {
          int numArchons = rc.readBroadcast(Broadcasting.ENEMY_ARCHON_NUMBER);
          int numGardeners = rc.readBroadcast(Broadcasting.ENEMY_GARDENER_NUMBER);
          int numEnemyClumps = rc.readBroadcast(Broadcasting.ENEMY_CLUMP_NUMBER);
          System.out.println("There are " + numArchons + " Enemy Archons, " + numGardeners + " Enemy Gardeners, and " + numEnemyClumps + " Enemy Clumps");
          if(roundNum %100 == 0)
            System.out.println(numArchons);
          MapLocation closestDistress = Broadcasting.closestDistress(rc);
          if (rc.getLocation().distanceTo(closestDistress) < 10 && myID % 2 == 1)
          {
              if (!respondingToDistress)
              {
                  respondingToDistress = true;
                  roundRespondedToDistress = roundNum;
              }
              tryAction(ActionType.MOVE,hover(closestDistress,roundNum-roundRespondedToDistress));
          }
          if (numGardeners > 0)
          {
              System.out.println(numGardeners);
              int myGardenerFirstSlot = Broadcasting.GARDENER1 + (myID % numGardeners) * Broadcasting.SLOTS_USED_PER_LOCATION;
              int roundsSinceSeen = roundNum - rc.readBroadcast(myGardenerFirstSlot+Broadcasting.INDEX_FOR_ROUND);
              if (roundsSinceSeen >= 50)
              {
                  Broadcasting.deadGardener(rc,rc.readBroadcast(myGardenerFirstSlot+Broadcasting.INDEX_FOR_ID_OR_NUM));
              }
              MapLocation previousGardener = Broadcasting.readBroadcastLocation(rc,myGardenerFirstSlot);
              tryAction(ActionType.MOVE,hover(previousGardener,roundsSinceSeen));
              System.out.println("I'm seeking Gardener #" + rc.readBroadcast(myGardenerFirstSlot + Broadcasting.INDEX_FOR_ID_OR_NUM) + " at location: " + previousGardener);

          }
          else if (numArchons > 0)
          {
              int[] activeArchonSlots = new int[4]; //First is how many active archons have been found other 3 are potential slots for them
              for (int i = 0; i<numArchons;i++)
              {
                  if (rc.readBroadcast(Broadcasting.ARCHON1 + Broadcasting.SLOTS_USED_PER_LOCATION*i + Broadcasting.INDEX_FOR_ID_OR_NUM) != 0) {
                      activeArchonSlots[0]++;
                      activeArchonSlots[activeArchonSlots[0]] = Broadcasting.ARCHON1 + Broadcasting.SLOTS_USED_PER_LOCATION*i;
                      System.out.println("archon#" + activeArchonSlots[0] + " is active");
                  }
              }
            int myArchonFirstSlot = Broadcasting.ARCHON1 + (myID % numArchons) * Broadcasting.SLOTS_USED_PER_LOCATION;
            MapLocation previousArchon = Broadcasting.readBroadcastLocation(rc,myArchonFirstSlot);
            int roundsSinceSeen = roundNum-rc.readBroadcast(myArchonFirstSlot+Broadcasting.INDEX_FOR_ROUND);
            //if ()
            //use delta round number multiplied by degrees
            tryAction(ActionType.MOVE,hover(previousArchon,roundsSinceSeen)); //Randomly associate all scouts with exactly one archon
            System.out.println("I'm seeking Archon #" + rc.readBroadcast(myArchonFirstSlot + Broadcasting.INDEX_FOR_ID_OR_NUM) + " at location: " + previousArchon);
          }
          else
          {
              if (victoryRound == 0)
                  victoryRound = roundNum;
              MapLocation[] archons = rc.getInitialArchonLocations(them);
              tryAction(ActionType.MOVE,hover(archons[myID % archons.length],roundNum+100-victoryRound));
          }
      }
  }

    static void harass() throws GameActionException {

        // If there are some...
        if (nearbyEnemies.length > 0) {
            // And we have enough bullets, and haven't attacked yet this turn...
            if (rc.canFireSingleShot() )//&& random.nextFloat()*(rc.getType().sensorRadius-rc.getLocation().distanceTo(nearbyEnemies[0].getLocation())) > (rc.getType().bodyRadius*2))
            {
                // ...Then fire a bullet in the direction of the enemy.
                rc.fireSingleShot(rc.getLocation().directionTo(nearbyEnemies[0].location));
            }
        }
    }
    static void sendReceiveIntel() throws GameActionException
    {
      RobotInfo[] robots = rc.senseNearbyRobots(-1);
      for (RobotInfo robot : robots)
      {
          if(robot.getTeam() == them)
          {
              switch (robot.getType())
              {
                  case ARCHON:
                      Broadcasting.updateArchon(rc, robot.getLocation(), roundNum,robot.ID);
                      if (robot.health < 10) {
                        reportDead(robot.ID,roundNum,true);
                        System.out.println("An Archon is Dead");
                        }
                      break;
                  case GARDENER:
                      if (currentGardenerIndex != -1 && rc.readBroadcast(currentGardenerIndex+Broadcasting.INDEX_FOR_ID_OR_NUM) == currentGardenerID)
                        Broadcasting.broadcastLocation(rc,currentGardenerIndex,robot.getLocation(), roundNum, robot.ID);
                      else
                      {
                          currentGardenerIndex = Broadcasting.updateTargetRobot(rc,robot.getLocation(),roundNum,robot.ID, false);
                          currentGardenerID = robot.ID;
                      }
                      if (robot.health < 10) {
                          reportDead(robot.ID,roundNum,false);
                          System.out.println("A Gardener is Dead");
                          System.out.println("This brings the gardener count down to " + rc.readBroadcast(Broadcasting.ENEMY_GARDENER_NUMBER));
                      }
                      break;
                  default:
                      /*Broadcasting.reportEnemy(rc,robot.getLocation(),roundNum);
                      if (robot.health < 10)
                      {
                          Broadcasting.reportEnemyDead(rc,robot.getLocation(),roundNum);
                      }*/
                      //Not working
                      break;
              }
          }
      }
    }
    static void reportDead(int ID, int round, boolean isArchon)
    {
        boolean foundIt = false;
        int firstZero = -1;
        for (int i = 0;i<25;i++)
        {
            if (deathIDs[i] == ID)
            {
                deathRounds[i] = round;
                foundIt = true;
                break;
            }
            if (firstZero == -1 && deathIDs[i] == 0)
            {
                firstZero = i;
            }
        }
        if (!foundIt && firstZero != -1)
        {
            deathIDs[firstZero]=ID;
            deathRounds[firstZero]=round;
            deathIsArchon[firstZero]=isArchon;
        }

    }
    static void reportTheDead() throws GameActionException
    {
        for (int i = 0;i<25;i++)
        {
            if (roundNum - deathRounds[i] >= deathTime && deathIDs[i] != 0)
            {
                if (deathIsArchon[i])
                {
                    Broadcasting.deadArchon(rc,deathIDs[i]);
                }
                else
                {
                    System.out.println("Gardener #" + deathIDs[i] + " was reported dead 10 rounds ago");
                    Broadcasting.deadGardener(rc,deathIDs[i]);
                }
                deathIDs[i] = 0;
                deathRounds[i] = 0;
            }
        }
    }
    static MapLocation hover(MapLocation m, int inaccuracyInRounds)
    {
        //if r = k*t
        //then theta = a/k + c/t
        return m.add((float)(Math.sqrt(10*inaccuracyInRounds)*((myID%2)*2-1)),inaccuracyInRounds/20);
    }
}
