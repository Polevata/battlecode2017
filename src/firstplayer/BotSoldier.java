package firstplayer;

import battlecode.common.*;

public strictfp class BotSoldier extends Bot {

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
    // See if there are any nearby enemy robots
    RobotInfo[] robots = rc.senseNearbyRobots(-1, them);

    // If there are some...
    if (robots.length > 0) {
      // And we have enough bullets, and haven't attacked yet this turn...
      if (rc.canFireSingleShot()) {
        // ...Then fire a bullet in the direction of the enemy.
        rc.fireSingleShot(rc.getLocation().directionTo(robots[0].location));
      }
    }

    // Move randomly
    tryMove(randomDirection());
  }

}