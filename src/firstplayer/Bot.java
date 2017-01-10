package firstplayer;

import battlecode.common.*;

public strictfp class Bot {
  public static RobotController rc;
  
  public static void loop(RobotController rc_) {
    init(rc_);
    while (true) {
      try {
        doTurn();
      } catch (GameActionException e) {
        System.out.printf("Bot %d (%s) threw %s", rc.getID(), rc.getType().name(), e.toString());
        e.printStackTrace();
      }
    }
  }
  
  public static void init(RobotController rc_) {
    rc = rc_;
  }
  
  public static void doTurn() throws GameActionException {
    Clock.yield();
  }
}
