package firstplayer;

import battlecode.common.*;
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

    // A move never happened, so return false.
    return false;
  }

}
