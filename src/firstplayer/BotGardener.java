package firstplayer;

import battlecode.common.*;

public strictfp class BotGardener extends Bot {
  public static void loop(RobotController rc_) {
    System.out.println("I'm a Gardener!");
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

    TreeInfo[] adjacentTrees = rc.senseNearbyTrees(2, us);

    if(adjacentTrees.length>0) rc.water(weakestAdjacentTree(adjacentTrees));

    if(plant && rc.getTreeCount()<rc.getRobotCount() && rc.canPlantTree(dir)){
      rc.plantTree(dir);
    }

    // Randomly attempt to build a soldier or lumberjack in this direction
    else if (rc.canBuildRobot(RobotType.SOLDIER, dir) && Math.random() < .2) {
      rc.buildRobot(RobotType.SOLDIER, dir);
//    } else if (rc.canBuildRobot(RobotType.LUMBERJACK, dir) && Math.random() < .01 && rc.isBuildReady()) {
//      rc.buildRobot(RobotType.LUMBERJACK, dir);
    } else if (rc.canBuildRobot(RobotType.SCOUT, dir) && Math.random() < .2) {
      rc.buildRobot(RobotType.SCOUT, dir);
    } else if (rc.canBuildRobot(RobotType.LUMBERJACK, dir) && Math.random() < .01) {
      rc.buildRobot(RobotType.LUMBERJACK, dir);
    }
    // Move randomly
    if (!evade() && adjacentTrees.length<3) tryMove(randomDirection());
  }


  static int weakestAdjacentTree(TreeInfo[] trees) throws GameActionException {
    try {
      int weakestTree = -1;
      float minHealth = Float.POSITIVE_INFINITY;
      float health;

      for (int i=0; i<trees.length; i++) {
        health = trees[i].getHealth();
        if (health < minHealth) {
          minHealth = health;
          weakestTree = trees[i].getID();
        }
      }
    return weakestTree;
    } catch (Exception e) {
      System.out.println("Weakest Tree Exception");
      return -1;
    }
  }
}
