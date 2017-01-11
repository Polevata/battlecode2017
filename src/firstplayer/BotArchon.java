package firstplayer;

import battlecode.common.*;
import bcutils.*;

public strictfp class BotArchon extends Bot {
  public static void loop(RobotController rc_) {
    System.out.println("I'm an Archon!");
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
    Direction dir = Utils.randomDirection();

    // Randomly attempt to build a gardener in this direction
    if (rc.canHireGardener(dir) && Math.random() < .01) {
      rc.hireGardener(dir);
    }

    // Evade bullets but don't move otherwise (too likely to get entrapped in enemy team)
    evade();

    // Broadcast archon's location for other robots on the team to know
    MapLocation myLocation = rc.getLocation();
    rc.broadcast(0, (int) myLocation.x);
    rc.broadcast(1, (int) myLocation.y);
  }

  public static void trySpawn() throws GameActionException {
  }
}
