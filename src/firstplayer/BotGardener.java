package firstplayer;

import battlecode.common.*;
import bcutils.Actions.*;
import bcutils.Broadcasting;

import javax.sound.midi.SysexMessage;
import java.awt.*;

public strictfp class BotGardener extends Bot {

  //ideal number of friendly trees around gardener
  public final static int DENSITY = 5;
  //number of rounds to move and do other things before building trees
  public final static int INITIAL_MOVES = 30;

  public static int trees=0;
  public static int[] myTrees;

  //approximate build ratio for robot types
  public final static float LUMBERJACK_BUILD_RATIO=5;
  public final static float SCOUT_BUILD_RATIO=7;
  public final static float SOLDIER_BUILD_RATIO=3;
  public final static float SUM_BUILD_RATIO=LUMBERJACK_BUILD_RATIO+SCOUT_BUILD_RATIO+SOLDIER_BUILD_RATIO;

  public static float SCOUT_BUILD_THRESHOLD_ = SCOUT_BUILD_RATIO/SUM_BUILD_RATIO;
  public static float LUMBERJACK_BUILD_THRESHOLD = (SCOUT_BUILD_RATIO+LUMBERJACK_BUILD_RATIO)/SUM_BUILD_RATIO;

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

    //Generate a random direction
    //Direction dir = randomDirection();

    BotScout.tryShake();

    //report dangerous situations
    if (inDanger) {
      Broadcasting.updateTargetRobot(rc, rc.getLocation(), roundNum, rc.getID(), true);
    } else if (wasInDanger) {
      Broadcasting.removeTargetRobot(rc, myID, true);
    }

    //build more soldiers if environment is very dangerous
    if (rc.isBuildReady() && inHighDanger) {
      if (rc.getTeamBullets() >= RobotType.SOLDIER.bulletCost) {
        tryAction(ActionType.BUILD_SOLDIER, nearbyEnemies[0].getLocation());
        //System.out.println("DANGER BUILD");
      } else {
        rc.broadcast(Broadcasting.DANGER_BUILD_ROUND, roundNum);
        //System.out.println("REPORT DANGER ROUND");
      }
    }

    // initial moves away from archon and other gardeners when this gardener was just built.
    // Also produces a scout if there aren't enough scouts
    if (roundNum-roundNumBirth < INITIAL_MOVES) {
      Direction dir = approxAwayFromArchons(4);
      tryAction(ActionType.MOVE, dir);
      if (!inHighDanger && rc.isBuildReady() && rc.readBroadcast(Broadcasting.SOLDIER_NUMBER) + rc.readBroadcast(Broadcasting.SCOUT_NUMBER) < rc.readBroadcast(Broadcasting.GARDENER_NUMBER)) {
          tryAction(ActionType.BUILD_SCOUT, dir);
      }
    }

    else {
      Direction dir = randomDirection();
      TreeInfo[] adjacentTrees = rc.senseNearbyTrees(RobotType.GARDENER.bodyRadius + GameConstants.INTERACTION_DIST_FROM_EDGE);

      // take care of weakest tree in surroundings
      int patient = weakestTree(adjacentTrees);
      if (patient != -1) {
        rc.water(patient);
        //System.out.println("WATER_TREE");
        //System.out.println(patient);
      } else {
        //System.out.println("CANT_WATER");
        //System.out.println(patient);
      }

      if (!inHighDanger && rc.isBuildReady()) {
          // if there aren't enough trees around, plant one
          if (plant && adjacentTrees.length < DENSITY && rc.canPlantTree(dir)) {
            tryAction(ActionType.PLANT, awayFromArchons().opposite());
          }

          // Randomly attempt to build a soldier, scout or lumberjack around the gardener.
          // Likelihood to build each based on ratio at top.

          if (Math.random() < 0.008 * rc.getTeamBullets()) {
            //build scout if that is all we can afford
            if (rc.getTeamBullets() < RobotType.SOLDIER.bulletCost) {
              tryAction(ActionType.BUILD_SCOUT, dir);
            } else {
              //build robots according to build ratio
              double randomNum = Math.random();
              if (randomNum < SCOUT_BUILD_THRESHOLD_) {
                tryAction(ActionType.BUILD_SCOUT, dir);
              } else if (randomNum < LUMBERJACK_BUILD_THRESHOLD) {
                tryAction(ActionType.BUILD_LUMBERJACK, dir);
              } else {
                tryAction(ActionType.BUILD_SOLDIER, dir);
              }
            }
          }
        }
      }
      // Move randomly
      //if (!evade() && adjacentTrees.length < DENSITY) tryMove(randomDirection());
  }

  /**
   * Returns weakest tree from array of trees
   *
   * @param trees - trees to consider
   * @return tree ID for weakest (lowest health tree among trees considered)
   * @throws GameActionException
   */
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

  /**
   * Finds a direction away from nearby archons and gardeners.
   * Archon force proportional to distance^2.
   * Gardener force proportional to distance.
   *
   * @return Direction away from archons and gardeners resulting from sum of forces
   * @throws GameActionException
   */
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

  /**
   * Approximate direction away from nearby archons and gardeners
   * Uses awayFromArchons weighted by ratio for vector and adds some randomness with unit distance weight to create new direction
   *
   * @param ratio ratio to weight awayFromArchons direction by
   * @return approximate Direction away from archons and gardeners
   * @throws GameActionException
   */
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
