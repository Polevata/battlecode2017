package firstplayer;

import battlecode.common.*;
import battlecodeutils.Utils;

public strictfp class BotArchon extends Bot {
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
    // Generate a random direction
    Direction dir = randomDirection();

    // Randomly attempt to build a gardener in this direction
    if (rc.canHireGardener(dir) && Math.random() < .01) {
      rc.hireGardener(dir);
    }

    // Move randomly
    tryMove(randomDirection());

    // Broadcast archon's location for other robots on the team to know
    MapLocation myLocation = rc.getLocation();
    rc.broadcast(0, (int) myLocation.x);
    rc.broadcast(1, (int) myLocation.y);
  }

  public static void trySpawn() throws GameActionException {
  }
}
