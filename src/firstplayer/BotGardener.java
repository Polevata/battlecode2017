package firstplayer;

import battlecode.common.*;

public strictfp class BotGardener extends Bot {

  public static void loop(RobotController rc_) {
    System.out.println("I'm a bot!");
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
    // Listen for home archon's location
    int xPos = rc.readBroadcast(0);
    int yPos = rc.readBroadcast(1);
    MapLocation archonLoc = new MapLocation(xPos,yPos);

    // Generate a random direction
    Direction dir = randomDirection();

    // Randomly attempt to build a soldier or lumberjack in this direction
    if (rc.canBuildRobot(RobotType.SOLDIER, dir) && Math.random() < .01) {
      rc.buildRobot(RobotType.SOLDIER, dir);
//    } else if (rc.canBuildRobot(RobotType.LUMBERJACK, dir) && Math.random() < .01 && rc.isBuildReady()) {
//      rc.buildRobot(RobotType.LUMBERJACK, dir);
    } else if (rc.canBuildRobot(RobotType.SCOUT, dir) && Math.random() < .01) {
      rc.buildRobot(RobotType.SCOUT, dir);
    }

    // Move randomly
    tryMove(randomDirection());
  }

}
