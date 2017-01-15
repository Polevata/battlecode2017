package firstplayer;

import battlecode.common.*;
import bcutils.Actions.*;
import bcutils.Broadcasting;
import bcutils.Utils;

public strictfp class BotScout extends Bot {
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
    TreeInfo[] neutralTrees = rc.senseNearbyTrees(myType.strideRadius, Team.NEUTRAL);
    int treeId;
    for (int i = 0; i < neutralTrees.length; i++) {
      treeId = neutralTrees[i].ID;
      if (rc.canShake(treeId)) {
        System.out.printf("Shaking tree %d", treeId);
        rc.shake(treeId);
      }
    }
  }

  static void explore() throws GameActionException {
      if (!evade())
      {
          int numArchons = rc.readBroadcast(Broadcasting.ARCHON_NUMBER);
          if (numArchons > 0)
          {
            int myArchonFirstSlot = (myID % numArchons) * Broadcasting.SLOTS_USED_PER_LOCATION;
            MapLocation previousArchon = Broadcasting.readBroadcastLocation(rc,myArchonFirstSlot);
            int roundsSinceSeen = roundNum-rc.readBroadcast(myArchonFirstSlot+2);
            //if ()
            //use delta round number multiplied by degrees
            MapLocation moveTo = previousArchon.add((float)(2*Math.PI/100*roundsSinceSeen),roundsSinceSeen/20);
            tryAction(ActionType.MOVE, rc,moveTo); //Randomly associate all scouts with exactly one archon
          }
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
        if (robot.getType() == RobotType.ARCHON && robot.getTeam() == them)
        {
          Broadcasting.updateArchon(rc,robot.getLocation(),roundNum);
          /*if (robot.health < 10)
          {
            Broadcasting.deadArchon(rc,robot.getLocation(),roundNum);
            System.out.println("An Archon is Dead");
          }*/
        }
      }
    }
}
