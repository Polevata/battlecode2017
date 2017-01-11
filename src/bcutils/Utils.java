package bcutils;

import battlecode.common.*;
import java.util.Random;

public strictfp class Utils {

  /**
   * Returns a random Direction
   *
   * @return a random Direction
   */
  public static Direction randomDirection() {
    return new Direction((float) Math.random() * 2 * (float) Math.PI);
  }
  
  public static Direction randomDirection(Random random) {
    return new Direction(random.nextFloat() * 2 * (float) Math.PI);
  }

  /**
   * A slightly more complicated example function, this returns true if the given bullet is on a collision
   * course with the current robot. Doesn't take into account objects between the bullet and this robot.
   *
   * @param bullet The bullet in question
   * @return True if the line of the bullet's path intersects with this robot's current position.
   */
  public static boolean willCollideWithMe(RobotController rc, BulletInfo bullet) {
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
  public static Direction getRandomDirection()
  {
    return new Direction((float)(2*Math.random()*Math.PI));
  }
}
