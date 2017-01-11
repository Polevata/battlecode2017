package firstplayer;

import battlecode.common.*;

public strictfp class BotLumber extends Bot {
  public static void loop(RobotController rc_) {
    System.out.println("I'm an Lumberjack!");
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



    // Move randomly
      evade();
      RobotInfo[] robots = rc.senseNearbyRobots(GameConstants.LUMBERJACK_STRIKE_RADIUS,them);
      // If there are some...
    System.out.println(robots.length);
      if (robots.length > 0 && rc.canStrike()) {
        // And we have enough bullets, and haven't attacked yet this turn...
        rc.strike();
      } else {
        // No close robots, so search for robots within sight radius
        // If there is a robot, move towards it
        robots = rc.senseNearbyRobots(RobotType.LUMBERJACK.sensorRadius,them);
        if (robots.length > 0) {
          MapLocation enemyLocation = robots[0].getLocation();
          Direction toEnemy = here.directionTo(enemyLocation);

          tryMove(toEnemy);
        } else {
          // Move Randomly
          tryMove(randomDirection());
        }
      }

  }

  public static void trySpawn() throws GameActionException {
  }
}
