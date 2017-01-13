package firstplayer;

import battlecode.common.*;
import bcutils.Broadcasting;
import sun.reflect.generics.tree.Tree;

import javax.sound.midi.SysexMessage;

public strictfp class BotGardener extends Bot {

  public final static int DENSITY = 4;
  public final static int INITIAL_MOVES = 30;
  public static int trees=0;
  public static int[] myTrees;


  public static void loop(RobotController rc_) {
    System.out.println("I'm a Gardener!");
    init(rc_);
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
    //Direction dir = randomDirection();


    if(roundNum-roundNumBirth < INITIAL_MOVES) {
      System.out.println("round");
      System.out.println(roundNum-roundNumBirth);
      tryMove(approxAwayFromArchons(4), 10, 9);
    }
    else {
      Direction dir = randomDirection();
      TreeInfo[] adjacentTrees = rc.senseNearbyTrees(2);
      TreeInfo[] nearbyTrees = rc.senseNearbyTrees(RobotType.GARDENER.bodyRadius+RobotType.GARDENER.strideRadius);

      int patient = weakestTree(nearbyTrees);
      if (patient!=-1) rc.water(patient);

      if (plant && adjacentTrees.length < DENSITY && rc.canPlantTree(dir)) {
        rc.plantTree(dir);
      }

      // Randomly attempt to build a soldier or lumberjack in this direction
      if (rc.canBuildRobot(RobotType.SCOUT, dir) && Math.random() < .001 * rc.getTeamBullets()) {
        rc.buildRobot(RobotType.SCOUT, dir);
//    } else if (rc.canBuildRobot(RobotType.LUMBERJACK, dir) && Math.random() < .01 && rc.isBuildReady()) {
//      rc.buildRobot(RobotType.LUMBERJACK, dir);
      } else if (rc.canBuildRobot(RobotType.SOLDIER, dir) && Math.random() < .001 * rc.getTeamBullets()) {
        rc.buildRobot(RobotType.SOLDIER, dir);
      } else if (rc.canBuildRobot(RobotType.LUMBERJACK, dir) && Math.random() < .001 * rc.getTeamBullets()) {
        rc.buildRobot(RobotType.LUMBERJACK, dir);
      }
      // Move randomly
      //if (!evade() && adjacentTrees.length < DENSITY) tryMove(randomDirection());
    }
  }


  static int weakestTree(TreeInfo[] trees) throws GameActionException {
    try {
      int patient = -1;
      float minHealth = Float.POSITIVE_INFINITY;
      float health;

      for (TreeInfo tree: trees) {
        health = tree.getHealth();
        if(tree.getTeam()==us && health < minHealth) {
          minHealth = health;
          patient = tree.getID();
        }
      }
    return patient;
    } catch (Exception e) {
      System.out.println("Weakest Tree Exception");
      return -1;
    }
  }
  static Direction awayFromArchons() throws GameActionException {
    Direction robotDirection;
    float robotDistance;
    RobotType robotType;
    float netX=0;
    float netY=0;

    RobotInfo[] nearbyRobots = rc.senseNearbyRobots(RobotType.GARDENER.sensorRadius, us);

    for (RobotInfo sensedRobot : nearbyRobots) {
      robotType = sensedRobot.getType();
      if (robotType == RobotType.ARCHON || robotType == RobotType.GARDENER) {
        robotDirection = sensedRobot.getLocation().directionTo(here);
        if (robotType == RobotType.ARCHON)
          robotDistance = here.distanceSquaredTo(sensedRobot.getLocation())/5;
        else
          robotDistance = here.distanceTo(sensedRobot.getLocation());
        netX += robotDirection.getDeltaX(1) / robotDistance;
        netY += robotDirection.getDeltaY(1) / robotDistance;
      }
    }
    return new Direction(netX, netY);
  }

  static Direction approxAwayFromArchons(int ratio) throws GameActionException{
    Direction away = awayFromArchons();
    float x = away.getDeltaX(ratio);
    float y = away.getDeltaY(ratio);
    Direction randomDir = randomDirection();
    x += randomDir.getDeltaX(1);
    y += randomDir.getDeltaY(1);
    return new Direction(x, y);
  }
}
