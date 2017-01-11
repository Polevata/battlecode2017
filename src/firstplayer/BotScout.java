package firstplayer;

import battlecode.common.*;

public strictfp class BotScout extends Bot {

  public static void loop(RobotController rc_) {
    System.out.println("I'm a bot!");
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
    explore();
    harrass();
  }

  static void explore() throws GameActionException {
    tryMove(randomDirection());
  }

  static void harrass() {
  }

}
