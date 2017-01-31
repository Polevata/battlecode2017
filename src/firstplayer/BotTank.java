package firstplayer;

import battlecode.common.*;
import bcutils.Actions.*;
import bcutils.Broadcasting;

/**
 * Created by polevata on 1/31/17.
 */
public class BotTank extends Bot {
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
            MapLocation enemyLoc = nearbyEnemies[0].location;
            Direction dir = here.directionTo(enemyLoc);
            // And we have enough bullets, and haven't attacked yet this turn...
            // ...Then fire a bullet in the direction of the enemy.
            if (rc.canFireSingleShot()) {
                float distance = enemyLoc.distanceTo(here);
                if (distance < 3 && rc.canFirePentadShot()) {
                    rc.firePentadShot(dir);
                }
                else if (distance < 5 && rc.canFireTriadShot()) {
                    rc.fireTriadShot(dir);
                } else if (rc.canFireSingleShot()) {
                    rc.fireSingleShot(dir);
                }
            }
            // Move towards the enemy
            evade();
            tryAction(ActionType.MOVE, dir);
        } else {
            if (myID % 3 == 0)
            {
                int[] activeArchonSlots = new int[4]; //First is how many active archons have been found other 3 are potential slots for them
                int numArchons = rc.readBroadcast(Broadcasting.ENEMY_ARCHON_NUMBER);
                for (int i = 0; i<numArchons;i++)
                {
                    if (rc.readBroadcast(Broadcasting.ARCHON1 + Broadcasting.SLOTS_USED_PER_LOCATION*i + Broadcasting.INDEX_FOR_ID_OR_NUM) != 0) {
                        activeArchonSlots[0]++;
                        activeArchonSlots[activeArchonSlots[0]] = Broadcasting.ARCHON1 + Broadcasting.SLOTS_USED_PER_LOCATION*i;
                        System.out.println("archon#" + activeArchonSlots[0] + " is active");
                    }
                }
                int myArchonFirstSlot = Broadcasting.ARCHON1 + (myID % numArchons) * Broadcasting.SLOTS_USED_PER_LOCATION;
                MapLocation previousArchon = Broadcasting.readBroadcastLocation(rc,myArchonFirstSlot);
                int roundsSinceSeen = roundNum-rc.readBroadcast(myArchonFirstSlot+Broadcasting.INDEX_FOR_ROUND);
                //if ()
                //use delta round number multiplied by degrees
                tryAction(ActionType.MOVE,previousArchon); //Randomly associate all scouts with exactly one archon

            }
            else
            {
                MapLocation distress = Broadcasting.closestDistress(rc);
                if (distress.x != -1) {
                    tryAction(ActionType.MOVE, distress);
                } else if (nearbyFriends.length > 0) {
                    for (RobotInfo friend : nearbyFriends) {
                        if ((friend.getType() == RobotType.GARDENER || friend.getType() == RobotType.ARCHON) && here.distanceTo(friend.getLocation()) < protectDistance) {
                            tryAction(ActionType.MOVE, here.directionTo(friend.getLocation()));
                        }
                    }
                }
            }

        }

        if (!rc.hasMoved()) {
            tryAction(ActionType.MOVE, randomDirection());
        }
    }
}
