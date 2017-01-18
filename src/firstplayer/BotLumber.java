package firstplayer;

import battlecode.common.*;
import bcutils.Actions.*;
import sun.reflect.generics.tree.Tree;

import java.awt.*;

public strictfp class BotLumber extends Bot {
  public static TreeInfo[] neutralTrees;
  public static TreeInfo[] enemyTrees;

  public static void loop(RobotController rc_) {
    System.out.println("I'm an Lumberjack!");
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

    // Move randomly
      evade();
      RobotInfo[] robotsThem = rc.senseNearbyRobots(RobotType.LUMBERJACK.bodyRadius+GameConstants.LUMBERJACK_STRIKE_RADIUS,them);
      RobotInfo[] robotsUs = rc.senseNearbyRobots(RobotType.LUMBERJACK.bodyRadius+GameConstants.LUMBERJACK_STRIKE_RADIUS,us);
      TreeInfo[] treesThem = rc.senseNearbyTrees(RobotType.LUMBERJACK.bodyRadius+GameConstants.LUMBERJACK_STRIKE_RADIUS);

      // If there are some...
      if (robotsThem.length+treesThem.length-robotsUs.length > 2 && rc.canStrike()) {
        // And we have enough bullets, and haven't attacked yet this turn...
        rc.strike();
      } else {
        // No close robots, so search for robots within sight radius
        // If there is a robot, move towards it
        robotsThem = rc.senseNearbyRobots(RobotType.LUMBERJACK.sensorRadius,them);
        neutralTrees = rc.senseNearbyTrees(RobotType.LUMBERJACK.sensorRadius, Team.NEUTRAL);
        enemyTrees = rc.senseNearbyTrees(RobotType.LUMBERJACK.sensorRadius, them);

        int chopTree = -1;
        int moveTree = -1;

        if(enemyTrees.length>0) {
          int nearestEnemyTree = nearestTree(enemyTrees);
          if (nearestEnemyTree != -1) {
            if (rc.canChop(nearestEnemyTree)) {
              chopTree = nearestEnemyTree;
            } else {
              moveTree = nearestEnemyTree;
            }
          }
        }

        if(chopTree==-1 && neutralTrees.length>0){
          int nearestNeutralTree = nearestTree(neutralTrees);
          if(nearestNeutralTree!=-1){
            if(rc.canShake(nearestNeutralTree)){
              rc.shake(nearestNeutralTree);
            }
            if(rc.canChop(nearestNeutralTree)) {
              chopTree = nearestNeutralTree;
              moveTree = -1;
            } else if(moveTree==-1){
              moveTree = nearestNeutralTree;
            }
          }
        }

        Direction dir;

        if(chopTree!=-1){
          rc.chop(chopTree);
        } else if (moveTree!=-1){
          dir = here.directionTo(rc.senseTree(moveTree).getLocation());
          tryAction(ActionType.MOVE, dir, 10, 6);
        } else if (robotsThem.length > 0) {
          MapLocation enemyLocation = robotsThem[0].getLocation();
          dir = here.directionTo(enemyLocation);
          tryAction(ActionType.MOVE, dir, 10, 6);
        } else{
          dir = BotGardener.approxAwayFromArchons(2);
          // Move Randomly
          tryAction(ActionType.MOVE, dir);
        }
      }
  }

  public static int nearestTree(TreeInfo[] trees){
    int closestTree = -1;
    float closestDist = RobotType.LUMBERJACK.sensorRadius+1;
    float dist;
    for(TreeInfo tree: trees){
      dist = tree.getLocation().distanceTo(here);
      if(dist<closestDist){
        closestDist = dist;
        closestTree = tree.getID();
      }
    }
    return closestTree;
  }

  public static void trySpawn() throws GameActionException {
  }
}
