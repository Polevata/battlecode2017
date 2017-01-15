package firstplayer;

import battlecode.common.*;

public strictfp class RobotPlayer {

  public static final boolean DEBUGGING = true;

  @SuppressWarnings("unused")
  public static void run(RobotController rc_) throws Exception {
    //System.out.println(rc_.getRoundNum());
    switch (rc_.getType()) {
      case ARCHON:
        BotArchon.loop(rc_);
        break;
      case GARDENER:
        BotGardener.loop(rc_);
        break;
      case SCOUT:
        BotScout.loop(rc_);
        break;
      case SOLDIER:
        BotSoldier.loop(rc_);
      case LUMBERJACK:
        BotLumber.loop(rc_);
      default:
        System.out.println("I don't know what this type of bot is");
    }
  }
}
