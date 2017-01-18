package firstplayer;

import battlecode.common.*;
import bcutils.*;

public strictfp class BotArchon extends Bot {
  public static void loop(RobotController rc_) throws GameActionException {
    System.out.println("I'm an Archon!");
    Bot.init(rc_);
    int endTurnRoundNum;
    rc_.broadcast(Broadcasting.ENEMY_ARCHON_NUMBER,rc.getInitialArchonLocations(us).length);
    System.out.println("Total number of Archons:" + rc.getInitialArchonLocations(us).length);
    MapLocation[] archons = rc_.getInitialArchonLocations(them);
    Broadcasting.broadcastLocation(rc_,Broadcasting.ARCHON1,archons[0],0,0);
    if (archons.length > 1)
      Broadcasting.broadcastLocation(rc_,Broadcasting.ARCHON2,archons[1],0,0);
    if (archons.length > 2)
      Broadcasting.broadcastLocation(rc_,Broadcasting.ARCHON3,archons[2],0,0);
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
    //Direction dir = Utils.randomDirection();



    // Randomly attempt to build a gardener in this direction
    if (plant && rc.isBuildReady() && (rc.getRobotCount()-rc.readBroadcast(Broadcasting.ARCHON_NUMBER)-rc.readBroadcast(Broadcasting.GARDENER_NUMBER)>rc.getTreeCount() || Math.random()<0.01*rc.getTeamBullets()) ){
        System.out.println("comparison");
        System.out.println(rc.readBroadcast(Broadcasting.ARCHON_NUMBER));
        System.out.println(rc.readBroadcast(Broadcasting.GARDENER_NUMBER));
        System.out.println(rc.getTreeCount());

        tryAction(Actions.ActionType.BUILD_GARDENER, BotGardener.awayFromArchons(), 15, 8);
    }

    // Evade bullets but don't move otherwise (too likely to get entrapped in enemy team)
    evade();

    // Broadcast archon's location for other robots on the team to know
    //MapLocation myLocation = rc.getLocation();
    //rc.broadcast(0, (int) myLocation.x);
    //rc.broadcast(1, (int) myLocation.y);
  }

  public static void trySpawn() throws GameActionException {
  }
}
