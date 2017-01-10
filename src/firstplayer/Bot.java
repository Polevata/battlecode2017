package firstplayer;

import battlecode.common.*;

public strictfp class Bot {
  public static RobotController rc;
  public static RobotType myType;
  public static int myID;
  public static Team us;
  public static Team them;
  
  public static MapLocation here;
  public static int roundNum;
  
  public static void loop(RobotController rc_) {
    init(rc_);
    int endTurnRoundNum;
    while (true) {
      try {
        update();
        doTurn();
      } catch (GameActionException e) {
        System.out.printf("Bot %d (%s) threw %s", myID, myType.name(), e.toString());
        e.printStackTrace();
      }
      endTurnRoundNum = rc.getRoundNum();
      if (roundNum < endTurnRoundNum) {
        System.out.printf("Bot %d (%s) over bytecode limit: rounds %d %d", myID, myType.name(), roundNum, endTurnRoundNum);
      }
      Clock.yield();
    }
  }
  
  public static void init(RobotController rc_) {
    rc = rc_;
    myType = rc.getType();
    myID = rc.getID();
    us = rc.getTeam();
    them = us.opponent();
    
    here = rc.getLocation();
    roundNum = rc.getRoundNum();
  }
  
  public static void update() {
    here = rc.getLocation();
    roundNum = rc.getRoundNum();
  }
  
  public static void doTurn() throws GameActionException {
    Clock.yield();
  }
}
