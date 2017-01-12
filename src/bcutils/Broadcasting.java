package bcutils;

import battlecode.common.*;
import java.util.Random;

public strictfp class Broadcasting {
    public static final int ARCHON1 = 0;
    public static final int ARCHON2 = 2;
    public static final int ARCHON3 = 4;
    public static final int ARCHON_NUMBER = 101;
    public static void updateArchon(RobotController rc,MapLocation archon) throws GameActionException
    {
        float distanceToNearest = 500;
        int archonNumber = 0;
        for (int i = 0; i<rc.readBroadcast(ARCHON_NUMBER); i++)
        {
            MapLocation previousArchon = new MapLocation(rc.readBroadcast(ARCHON1),rc.readBroadcast(ARCHON1+1));
            System.out.println("Previous Archon:" + previousArchon);
            System.out.println("Distance to previous:" + previousArchon.distanceTo(archon));
            if (previousArchon.distanceTo(archon) < distanceToNearest)
                archonNumber = i;
        }
        System.out.println("X:" + rc.readBroadcast(ARCHON1 + archonNumber*2) + " Y:" + rc.readBroadcast(ARCHON1 + archonNumber*2 +1));
        broadcastLocation(rc,ARCHON1 + 2*archonNumber,archon); //Update the archon that is closest to the updated value
        System.out.println("X:" + rc.readBroadcast(ARCHON1 + archonNumber*2) + " Y:" + rc.readBroadcast(ARCHON1 + archonNumber*2 +1));
    }

    public static void broadcastLocation(RobotController rc, int channel, MapLocation loc) throws GameActionException
    {
        System.out.println(loc);
        rc.broadcast(channel,(int)loc.x);
        rc.broadcast(channel+1,(int)loc.y);
    }
    public static MapLocation readBroadcastLocation(RobotController rc, int channel) throws GameActionException
    {
        return new MapLocation(rc.readBroadcast(channel),rc.readBroadcast(channel+1));
    }
}
/*
Channel handling
Locations will be in 1x000y format
0-5: Last seen Archon Location
6-25: Relatively new enemy gardener locations
26-50: Friendly tree locations
51-75: Neutral tree locations
76-100: Enemy clumping
101-106: Number of each unit

 */