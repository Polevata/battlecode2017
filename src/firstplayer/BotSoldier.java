package firstplayer;

import battlecode.common.*;
import bcutils.Actions;
import bcutils.Actions.*;
import bcutils.Broadcasting;
import sun.reflect.generics.tree.Tree;

public strictfp class BotSoldier extends Bot {

  public static final double protectDistance = RobotType.SOLDIER.bodyRadius + RobotType.GARDENER.bodyRadius*1.05 + GameConstants.BULLET_TREE_RADIUS + RobotType.SOLDIER.strideRadius;

  public static void loop(RobotController rc_) {
    System.out.println("I'm a Soldier!");
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

    BotScout.tryShake();

    // If there are some...
    if (nearbyEnemies.length > 0) {
      Direction dir = here.directionTo(nearbyEnemies[0].location);
      // And we have enough bullets, and haven't attacked yet this turn...
      // ...Then fire a bullet in the direction of the enemy.
      if (rc.canFireSingleShot()) {
        float distance = nearbyEnemies[0].getLocation().distanceTo(here);
        if (distance < 3 && rc.canFireTriadShot()) {
          rc.fireTriadShot(dir);
        } else if (distance < 3.5) {
          rc.fireSingleShot(dir);
        }
      }
      // Move towards the enemy
      evade();
      tryAction(ActionType.MOVE, dir, 6, 15);
    } else {
      MapLocation distress = Broadcasting.closestDistress(rc);
      if (distress.x != -1) {
        tryAction(ActionType.MOVE, here.directionTo(distress), 6, 14);
      } else if (nearbyFriends.length > 0) {
        for (RobotInfo friend : nearbyFriends) {
          if ((friend.getType() == RobotType.GARDENER || friend.getType() == RobotType.ARCHON) && here.distanceTo(friend.getLocation()) < protectDistance) {
            tryAction(ActionType.MOVE, here.directionTo(friend.getLocation()), 6, 10);
          }
        }
      }
    }

    if (!rc.hasMoved()) {
      tryAction(ActionType.MOVE, randomDirection(), 10, 17);
    }
  }
}
