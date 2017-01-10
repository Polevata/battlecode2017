package firstplayer;

import battlecode.common.*;

public strictfp class Bot {
  public static RobotController rc;
  public static RobotType myType;
  public static Team us;
  public static Team them;
  
  public static MapLocation here;
  
  public static void loop(RobotController rc_) {
    init(rc_);
    while (true) {
      try {
        update();
        doTurn();
      } catch (GameActionException e) {
        System.out.printf("Bot %d (%s) threw %s", rc.getID(), rc.getType().name(), e.toString());
        e.printStackTrace();
      }
    }
  }
  
  public static void init(RobotController rc_) {
    rc = rc_;
    myType = rc.getType();
    us = rc.getTeam();
    them = us.opponent();
    
    here = rc.getLocation();
  }
  
  public static void update() {
    here = rc.getLocation();
  }
  
  public static void doTurn() throws GameActionException {
    Clock.yield();
  }
}
