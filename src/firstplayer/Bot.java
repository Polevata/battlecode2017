package firstplayer;

import battlecode.common.*;

import java.util.ArrayList;
import java.util.Random;

public strictfp class Bot {
  public static RobotController rc;
  public static RobotType myType;
  public static int myID;
  public static Team us;
  public static Team them;
  public static Random random;
  
  public static MapLocation here;
  public static int roundNum;

  public static boolean plant = true;
  
  public static void init(RobotController rc_) {
    rc = rc_;
    myType = rc.getType();
    myID = rc.getID();
    us = rc.getTeam();
    them = us.opponent();
    random = new Random(myID);

    here = rc.getLocation();
    roundNum = rc.getRoundNum();
  }

  public static void update() {
    here = rc.getLocation();
    roundNum = rc.getRoundNum();
  }
  public static Direction randomDirection() {
    return new Direction(random.nextFloat() * 2 * (float) Math.PI);
  }

  /**
   * Attempts to move in a given direction, while avoiding small obstacles directly in the path.
   *
   * @param dir The intended direction of movement
   * @return true if a move was performed
   * @throws GameActionException
   */

  static boolean tryMove(Direction dir) throws GameActionException {
    return tryMove(dir,20,3);
  }

  static boolean tryMove(RobotController rc, MapLocation ml) throws GameActionException
  {
    return tryMove(rc.getLocation().directionTo(ml));
  }

  /**
   * Attempts to move in a given direction, while avoiding small obstacles direction in the path.
   *
   * @param dir The intended direction of movement
   * @param degreeOffset Spacing between checked directions (degrees)
   * @param checksPerSide Number of extra directions checked on each side, if intended direction was unavailable
   * @return true if a move was performed
   * @throws GameActionException
   */
  static boolean tryMove(Direction dir, float degreeOffset, int checksPerSide) throws GameActionException {

    if (!rc.hasMoved())
    {
      // First, try intended direction
      if (rc.canMove(dir)) {
        rc.move(dir);
        return true;
      }

      // Now try a bunch of similar angles
      boolean moved = false;
      int currentCheck = 1;

      while(currentCheck<=checksPerSide) {
        // Try the offset of the left side
        if(rc.canMove(dir.rotateLeftDegrees(degreeOffset*currentCheck))) {
          rc.move(dir.rotateLeftDegrees(degreeOffset*currentCheck));
          return true;
        }
        // Try the offset on the right side
        if(rc.canMove(dir.rotateRightDegrees(degreeOffset*currentCheck))) {
          rc.move(dir.rotateRightDegrees(degreeOffset*currentCheck));
          return true;
        }
        // No move performed, try slightly further
        currentCheck++;
      }
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
        netX += b.dir.getDeltaX(1) / b.getLocation().distanceTo(here);
        netY += b.dir.getDeltaY(1) / b.getLocation().distanceTo(here);
      }
    }
    if (noDangerousBullet) {
      return false;
    }
    Direction averageDirection = new Direction(netX,netY);
    return tryMove(averageDirection.rotateLeftDegrees(90)) || tryMove(averageDirection.rotateRightDegrees(90));
  }
}
