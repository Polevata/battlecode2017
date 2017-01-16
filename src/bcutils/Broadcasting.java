package bcutils;

import battlecode.common.*;

import java.awt.*;
import java.util.Random;

public strictfp class Broadcasting {
  public static final int SLOTS_USED_PER_LOCATION = 4;
  public static final int INDEX_FOR_X = 0;
  public static final int INDEX_FOR_Y = 1;
  public static final int INDEX_FOR_ROUND = 2;
  public static final int INDEX_FOR_ID_OR_NUM = 3;
  public static final int ARCHON1 = 0;
  public static final int ARCHON2 = ARCHON1+SLOTS_USED_PER_LOCATION;
  public static final int ARCHON3 = ARCHON2+SLOTS_USED_PER_LOCATION;
  public static final int GARDENER1 = ARCHON3+SLOTS_USED_PER_LOCATION+1;
  public static final int ARCHON_NUMBER = 280;
  public static final int GARDENER_NUMBER = ARCHON_NUMBER+1;
  public static final int SOLDIER_NUMBER = ARCHON_NUMBER+2;
  public static final int TANK_NUMBER = ARCHON_NUMBER+3;
  public static final int SCOUT_NUMBER = ARCHON_NUMBER+4;
  public static final int LUMBERJACK_NUMBER = ARCHON_NUMBER+5;
  public static final int ENEMY_ARCHON_NUMBER = ARCHON_NUMBER+6;
  public static final int ENEMY_GARDENER_NUMBER = ENEMY_ARCHON_NUMBER+1;

  public static void updateArchon(RobotController rc,MapLocation archon, int roundNumber, int archonID) throws GameActionException
  {
    int numArchons = rc.readBroadcast(ENEMY_ARCHON_NUMBER);
    int archonNumber = -1;
    int firstZero = -1;
    for (int i = 0; i<numArchons; i++)
    {
      int currentID = rc.readBroadcast(ARCHON1 + i*SLOTS_USED_PER_LOCATION + INDEX_FOR_ID_OR_NUM);
      if (currentID == archonID)
        archonNumber = i;
      if (currentID == 0)
        firstZero = i;
    }
    if (archonNumber == -1)
    {
      /*
      If for example there are supposed to be 2 archons, and the
      first two slots have an archon, but a new one is found,
      this simply means that one archon was presumed dead, and has
      now been found again, so the count needs to be increased.

      Conversely, if there is an empty slot found where there should be archons,
      this probably means that the game just started,
      and their ID's are not known yet

      I think this needs work though. For instance, if there were 3 archons,
      but one was presumed dead, it would now only check the first 2 slots,
      and there might still be a zero in slot 3 that isn't being considered
       */
      if (firstZero == -1)
      {
        archonNumber = numArchons;
        rc.broadcast(ENEMY_ARCHON_NUMBER,numArchons+1);
      }
      else
        archonNumber = firstZero;
    }
    //if (roundNumber % 10 == 0)
    //  System.out.println("Found archon #" + (archonNumber + 1) + " in round " + roundNumber);
    broadcastLocation(rc,ARCHON1 + SLOTS_USED_PER_LOCATION*archonNumber,archon,roundNumber,archonID); //Update the archon that is closest to the updated value
  }
  public static int updateGardener(RobotController rc,MapLocation gardener, int roundNumber, int gardenerID) throws GameActionException {
    int gardenerNumber = -1;
    int firstZero = -1;
    System.out.println("A gardener has been spotted at:" + gardener);
    for (int i = 0; i < rc.readBroadcast(ENEMY_GARDENER_NUMBER); i++) {
      int currentGardenerID = rc.readBroadcast(GARDENER1 + i * SLOTS_USED_PER_LOCATION + INDEX_FOR_ID_OR_NUM);
      if (currentGardenerID == gardenerID)
        gardenerNumber = i;
      else if (currentGardenerID == 0 && firstZero == -1)
        firstZero = i;
    }
    if (gardenerNumber == -1)
    {
      System.out.println("This gardener is new or was presumed dead");
      int numGards = rc.readBroadcast(ENEMY_GARDENER_NUMBER);
      if (numGards != 20)
        rc.broadcast(ENEMY_GARDENER_NUMBER,numGards+1);
      if (firstZero != -1)
      {
        System.out.println("This gardener will fill a slot that was unoccupied");
        gardenerNumber = firstZero;
      }
      else
      {
        if (numGards < 20)
        {
          gardenerNumber = numGards;
          System.out.println("This gardener is new and will be added at the end");
        }
        else
        {
          gardenerNumber = 20;
          System.out.println("Here comes the 20th gardener!");
        }
      }
    }
    broadcastLocation(rc,GARDENER1 + SLOTS_USED_PER_LOCATION*gardenerNumber,gardener,roundNumber,gardenerID);
    return gardenerNumber;
  }

  public static void deadArchon(RobotController rc,int robotID) throws GameActionException
  {
    boolean foundIt = false;
    for (int i=0;i<rc.readBroadcast(ENEMY_ARCHON_NUMBER);i++)
    {
      int currentLoc = ARCHON1 + SLOTS_USED_PER_LOCATION*i;
      if (rc.readBroadcast(currentLoc + INDEX_FOR_ID_OR_NUM) == robotID)
      {
        foundIt = true;
        deadBot(rc,currentLoc);
      }
    }
    if (foundIt)
    {
      int numArchons = rc.readBroadcast(ENEMY_ARCHON_NUMBER);
      rc.broadcast(ENEMY_ARCHON_NUMBER,numArchons-1);
    }
  }
  public static void deadGardener(RobotController rc,int robotID) throws GameActionException
  {
    boolean foundIt = false;
    for (int i=0;i<rc.readBroadcast(ENEMY_GARDENER_NUMBER);i++)
    {
      int currentLoc = GARDENER1 + SLOTS_USED_PER_LOCATION*i;
      if (rc.readBroadcast(currentLoc + INDEX_FOR_ID_OR_NUM) == robotID)
      {
        foundIt = true;
        deadBot(rc,currentLoc);
      }
    }
    if (foundIt)
    {
      int numGardeners = rc.readBroadcast(ENEMY_GARDENER_NUMBER);
      rc.broadcast(ENEMY_GARDENER_NUMBER,numGardeners-1);
    }
  }
  public static void deadBot(RobotController rc,int index) throws GameActionException
  {
    rc.broadcast(index+INDEX_FOR_ID_OR_NUM,0);
  }

  public static void broadcastLocation(RobotController rc, int channel, MapLocation loc, int roundNumber, int botIDorNumBots) throws GameActionException
  {
    rc.broadcast(channel + INDEX_FOR_X,(int)loc.x);
    rc.broadcast(channel + INDEX_FOR_Y,(int)loc.y);
    rc.broadcast(channel + INDEX_FOR_ROUND,roundNumber);
    rc.broadcast(channel + INDEX_FOR_ID_OR_NUM,botIDorNumBots);
  }
  public static MapLocation readBroadcastLocation(RobotController rc, int channel) throws GameActionException
  {
    return new MapLocation(rc.readBroadcast(channel),rc.readBroadcast(channel+1));
  }

  public static int robotTypeBroadcastNum(RobotType type)
  {
    switch(type)
    {
      case GARDENER: return Broadcasting.GARDENER_NUMBER;
      case SOLDIER: return Broadcasting.SOLDIER_NUMBER;
      case TANK: return Broadcasting.TANK_NUMBER;
      case LUMBERJACK: return Broadcasting.LUMBERJACK_NUMBER;
      default: return Broadcasting.SCOUT_NUMBER;
    }
  }

  public static void incrementRobotType(RobotController rc, RobotType type)
  {
    try {
      int broadcastTypeNum = robotTypeBroadcastNum(type);
      rc.broadcast(broadcastTypeNum, rc.readBroadcast(broadcastTypeNum) + 1);
    } catch (GameActionException e){
      System.out.println(e.toString());
      e.printStackTrace();
    }
  }

  public static void decrementRobotType(RobotController rc, RobotType type)
  {
    try {
      int broadcastTypeNum = robotTypeBroadcastNum(type);
      rc.broadcast(broadcastTypeNum, rc.readBroadcast(broadcastTypeNum) - 1);
    } catch (GameActionException e){
      System.out.println(e.toString());
      e.printStackTrace();
    }
  }
}
/*
Channel handling
Locations will be in |x||y||round||ID/Grouping#| format
0-11: Last seen Archon Location
12-79: Relatively new enemy gardener locations
80-279: Enemy clumping
280-285: Number of each friendly unit
286-291: Rough Number of each enemy unit

 */

//Gardener + Archons

/*
Idea is to have units flock to clumps of enemies based on number of enemies in clump
 */