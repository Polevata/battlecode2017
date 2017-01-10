package firstplayer;

import battlecode.common.*;

public strictfp class RobotPlayer {

  @SuppressWarnings("unused")
  public static void run(RobotController rc_) throws Exception {
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
      default:
        throw new Exception("Unknown robot type");
    }
  }
}
