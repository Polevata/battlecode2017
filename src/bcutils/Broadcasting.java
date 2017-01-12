package bcutils;

import battlecode.common.*;

import java.awt.*;
import java.util.Random;

public strictfp class Broadcasting {
    public static final int SLOTS_USED_PER_LOCATION = 3;
    public static final int ARCHON1 = 0;
    public static final int ARCHON2 = ARCHON1+SLOTS_USED_PER_LOCATION;
    public static final int ARCHON3 = ARCHON2+SLOTS_USED_PER_LOCATION;
    public static final int ARCHON_NUMBER = 101;
    public static void updateArchon(RobotController rc,MapLocation archon, int roundNumber) throws GameActionException
    {
        float distanceToNearest = 500;
        int archonNumber = 0;
        for (int i = 0; i<rc.readBroadcast(ARCHON_NUMBER); i++)
        {
            MapLocation previousArchon = new MapLocation(rc.readBroadcast((i*SLOTS_USED_PER_LOCATION)+ARCHON1),rc.readBroadcast((i*SLOTS_USED_PER_LOCATION)+ARCHON1+1));
            float newDistance = previousArchon.distanceTo(archon);
            if (roundNumber % 10 == 0)
                System.out.println(newDistance);
            if (newDistance < distanceToNearest)
            {
                archonNumber = i;
                distanceToNearest = newDistance;
            }
        }
        if (roundNumber % 10 == 0)
            System.out.println("Found archon #" + (archonNumber + 1) + " in round " + roundNumber);
        broadcastLocation(rc,ARCHON1 + SLOTS_USED_PER_LOCATION*archonNumber,archon,roundNumber); //Update the archon that is closest to the updated value
    }

    public static void deadArchon(RobotController rc,MapLocation archon, int roundNumber) throws GameActionException
    {
        float distanceToNearest = 500;
        int whichArchon = 0;
        int numArchons = rc.readBroadcast(ARCHON_NUMBER);
        for (int i = 0; i<numArchons; i++)
        {
            MapLocation previousArchon = new MapLocation(rc.readBroadcast(ARCHON1),rc.readBroadcast(ARCHON1+1));
            if (previousArchon.distanceTo(archon) < distanceToNearest)
                whichArchon = i;
        }
        switch (whichArchon)
        {
            case 0:
                if (numArchons == 3)
                    broadcastLocation(rc,ARCHON1,readBroadcastLocation(rc,ARCHON3),roundNumber);
                else if (numArchons == 2)
                    broadcastLocation(rc,ARCHON1,readBroadcastLocation(rc,ARCHON2),roundNumber);
                System.out.println("First Archon Died");
                break;
            case 1:
                if (numArchons == 3)
                    broadcastLocation(rc,ARCHON2,readBroadcastLocation(rc,ARCHON3),roundNumber);
                System.out.println("Second Archon Died");
                break;
            default:
                System.out.println("Third Archon Died");
                break;
        }
        rc.broadcast(ARCHON_NUMBER,numArchons-1);
    }

    public static void broadcastLocation(RobotController rc, int channel, MapLocation loc, int roundNumber) throws GameActionException
    {
        rc.broadcast(channel,(int)loc.x);
        rc.broadcast(channel+1,(int)loc.y);
        rc.broadcast(channel+2,roundNumber);
    }
    public static MapLocation readBroadcastLocation(RobotController rc, int channel) throws GameActionException
    {
        return new MapLocation(rc.readBroadcast(channel),rc.readBroadcast(channel+1));
    }
}
/*
Channel handling
Locations will be in |x||y||round| format
0-8: Last seen Archon Location
9-25: Relatively new enemy gardener locations
26-50: Friendly tree locations
51-75: Neutral tree locations
76-100: Enemy clumping
101-106: Number of each unit

 */