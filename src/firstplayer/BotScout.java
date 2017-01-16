package firstplayer;

import battlecode.common.*;
import bcutils.Actions.*;
import bcutils.Broadcasting;
import bcutils.Utils;

public strictfp class BotScout extends Bot {
  public static final int deathTime = 20; //rounds
    public static int currentGardenerID = -1;
    public static int currentGardenerIndex = -1;

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
  }
  
  public static void tryShake() throws GameActionException {
      try {
          TreeInfo[] neutralTrees = rc.senseNearbyTrees(myType.strideRadius, Team.NEUTRAL);
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
          System.out.println("There are " + numArchons + " Enemy Archons, and " + numGardeners + " Enemy Gardeners");
          if(roundNum %100 == 0)
            System.out.println(numArchons);
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
            int myArchonFirstSlot = (myID % numArchons) * Broadcasting.SLOTS_USED_PER_LOCATION;
            MapLocation previousArchon = Broadcasting.readBroadcastLocation(rc,myArchonFirstSlot);
            int roundsSinceSeen = roundNum-rc.readBroadcast(myArchonFirstSlot+Broadcasting.INDEX_FOR_ROUND);
            //if ()
            //use delta round number multiplied by degrees
            tryAction(ActionType.MOVE,hover(previousArchon,roundsSinceSeen)); //Randomly associate all scouts with exactly one archon
            System.out.println("I'm seeking Archon #" + rc.readBroadcast(myArchonFirstSlot + Broadcasting.INDEX_FOR_ID_OR_NUM) + " at location: " + previousArchon);
          }
          else
              tryAction(ActionType.MOVE,hover(new MapLocation(0,0),roundNum-roundNumBirth));
      }
  }

    static void harass() throws GameActionException {
        RobotInfo[] robots = rc.senseNearbyRobots(-1, them);

        // If there are some...
        if (robots.length > 0) {
            // And we have enough bullets, and haven't attacked yet this turn...
            if (rc.canFireSingleShot()) {
                // ...Then fire a bullet in the direction of the enemy.
                rc.fireSingleShot(rc.getLocation().directionTo(robots[0].location));
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
                        Broadcasting.deadArchon(rc, robot.ID );
                        System.out.println("An Archon is Dead");
                        }
                      break;
                  case GARDENER:
                      if (currentGardenerIndex != -1 && rc.readBroadcast(currentGardenerIndex+Broadcasting.INDEX_FOR_ID_OR_NUM) == currentGardenerID)
                        Broadcasting.broadcastLocation(rc,currentGardenerIndex,robot.getLocation(), roundNum, robot.ID);
                      else
                      {
                          currentGardenerIndex = Broadcasting.updateGardener(rc,robot.getLocation(),roundNum,robot.ID);
                          currentGardenerID = robot.ID;
                      }
                      if (robot.health < 10) {
                          Broadcasting.deadGardener(rc, robot.ID );
                          System.out.println("A Gardener is Dead");
                          System.out.println("This brings the gardener count down to " + rc.readBroadcast(Broadcasting.ENEMY_GARDENER_NUMBER));
                      }
                      break;
              }
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
