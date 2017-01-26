package firstplayer;

import battlecode.common.*;
import bcutils.Actions.*;
import bcutils.Actions;
import bcutils.Broadcasting;

import java.util.ArrayList;
import java.util.Random;

public strictfp class Bot {
  public static RobotController rc;
  public static RobotType myType;
  public static int myTypeBroadcastNum;
  public static int myID;
  public static Team us;
  public static Team them;
  public static Random random;
  public static boolean reportAlive;

  // static variables that may change each round; modified in update()
  public static MapLocation here;
  public static float health;
  public static int roundNum;
  public static int roundNumBirth;
  public static RobotInfo[] nearbyFriends;
  public static RobotInfo[] nearbyEnemies;
  public static TreeInfo[] nearbyTrees;

  public static boolean plant = true;
  
  static void init(RobotController rc_) {
    rc = rc_;
    myType = rc.getType();
    myID = rc.getID();
    us = rc.getTeam();
    them = us.opponent();
    health = rc.getHealth();
    reportAlive = true;
    random = new Random(myID);

    nearbyFriends = rc.senseNearbyRobots(-1, us);
    nearbyEnemies = rc.senseNearbyRobots(-1, them);
    nearbyTrees = rc.senseNearbyTrees();

    here = rc.getLocation();
    roundNum = rc.getRoundNum();
    roundNumBirth = roundNum;
    Broadcasting.incrementRobotType(rc, myType);
  }


  static void update() throws GameActionException {
    here = rc.getLocation();
    roundNum = rc.getRoundNum();
    health = rc.getHealth();
    nearbyFriends = rc.senseNearbyRobots(-1, us);
    nearbyEnemies = rc.senseNearbyRobots(-1, them);
    nearbyTrees = rc.senseNearbyTrees();

    if(reportAlive && health < 10) {
      Broadcasting.decrementRobotType(rc, myType);
    }
    if (rc.getTeamBullets() >= 10000 && !RobotPlayer.DEBUGGING)
      rc.donate(10000);
  }
  public static Direction randomDirection() {
    return new Direction(random.nextFloat() * 2 * (float) Math.PI);
  }

  /**
   * Attempts to do an action in a direction
   *
   * @param dir The intended direction of action
   * @return true if an action was performed
   * @throws GameActionException
   */

  static boolean tryAction(ActionType action, Direction dir) throws GameActionException {
    return tryAction(action, dir,20,3);
  }

  static boolean tryAction(ActionType action, MapLocation ml) throws GameActionException
  {
    return tryAction(action, rc.getLocation().directionTo(ml));
  }

  /**
   * Attempts to do an action in a general direction, starting in the exact direction and trying nearby directions if the exact direction fails
   *
   * @param dir The intended direction of action
   * @param degreeOffset Spacing between checked directions (degrees)
   * @param checksPerSide Number of extra directions checked on each side, if intended direction was unavailable
   * @return true if an action was performed
   * @throws GameActionException
   */

  static boolean tryAction(ActionType action, Direction dir, float degreeOffset, int checksPerSide) throws GameActionException {


    if( (action==ActionType.MOVE && rc.hasMoved()) || (action==ActionType.FIRE && rc.hasAttacked()) ){
      return false;
    }

    BulletInfo[] walkableBullets = rc.senseNearbyBullets(myType.strideRadius + myType.bodyRadius); //We need to have something like this to prevent walking into bullets
    // First, try intended direction


    if(RobotPlayer.DEBUGGING) {
      System.out.println(RobotPlayer.DEBUGGING);
      System.out.println("ACTION DISPATCH:");
      System.out.println(action);
    }

    if (Actions.dispatchAction(rc, action, dir)) {
      return true;
    }

    // Now try a bunch of similar angles
    boolean moved = false;
    int currentCheck = 1;

    while(currentCheck<=checksPerSide) {
      // Try the offset of the left side
      if(Actions.dispatchAction(rc, action, dir.rotateLeftDegrees(degreeOffset*currentCheck))) {
        return true;
      }
      // Try the offset on the right side
      if(Actions.dispatchAction(rc, action, dir.rotateRightDegrees(degreeOffset*currentCheck))) {
        return true;
      }
      // No move performed, try slightly further
      currentCheck++;
    }

   // A move never happened, so return false.
   return false;
  }

  public static boolean willCollideWithMe(BulletInfo bullet) {
    MapLocation myLocation = rc.getLocation();

    // Get relevant bullet information
    Direction propagationDirection = bullet.dir;
    MapLocation bulletLocation = bullet.location;

    // Calculate bullet relations to this robot
    Direction directionToRobot = bulletLocation.directionTo(myLocation);
    float distToRobot = bulletLocation.distanceTo(myLocation);
    float theta = propagationDirection.radiansBetween(directionToRobot);

    // If theta > 90 degrees, then the bullet is traveling away from us and we can break early
    if (Math.abs(theta) > Math.PI/2) {
      return false;
    }

    // distToRobot is our hypotenuse, theta is our angle, and we want to know this length of the opposite leg.
    // This is the distance of a line that goes from myLocation and intersects perpendicularly with propagationDirection.
    // This corresponds to the smallest radius circle centered at our location that would intersect with the
    // line that is the path of the bullet.
    float perpendicularDist = (float)Math.abs(distToRobot * Math.sin(theta)); // soh cah toa :)

    return (perpendicularDist <= rc.getType().bodyRadius);
  }

  static boolean evade() throws GameActionException {
    BulletInfo[] nearbyBullets = rc.senseNearbyBullets();
    float netX = 0;
    float netY = 0;
    BulletInfo b;
    boolean noDangerousBullet = true;
    for (int i = 0; i < nearbyBullets.length; i++) {
      b = nearbyBullets[i];
      if (willCollideWithMe(b)) {
        noDangerousBullet = false;
        netX += b.dir.getDeltaX(1) / Math.pow(b.getLocation().distanceTo(here),2);
        netY += b.dir.getDeltaY(1) / Math.pow(b.getLocation().distanceTo(here),2);
      }
    }
    if (noDangerousBullet) {
      RobotInfo[] nearbyBots = rc.senseNearbyRobots(-1,them);
      for (RobotInfo r : nearbyBots)
      {
        if (r.getType() == RobotType.LUMBERJACK && r.getLocation().distanceTo(here) < RobotType.LUMBERJACK.bodyRadius + GameConstants.LUMBERJACK_STRIKE_RADIUS*5)
          return tryAction(ActionType.MOVE, r.getLocation().directionTo(here),30,3);
      }
      return false;
    }
    Direction averageDirection = new Direction(netX,netY);
    return tryAction(ActionType.MOVE, averageDirection.rotateLeftDegrees(90)) || tryAction(ActionType.MOVE, averageDirection.rotateRightDegrees(90));
  }
}
