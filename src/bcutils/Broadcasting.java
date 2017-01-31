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
  public static final int GARDENER1 = 12;
  public static final int ENEMY_CLUMP1 = 80;
  public static final int DISTRESS1 = 200;
  public static final int ARCHON_NUMBER = 420;

  //FRIENDLY COUNT
  public static final int GARDENER_NUMBER = ARCHON_NUMBER+1;
  public static final int SOLDIER_NUMBER = ARCHON_NUMBER+2;
  public static final int TANK_NUMBER = ARCHON_NUMBER+3;
  public static final int SCOUT_NUMBER = ARCHON_NUMBER+4;
  public static final int LUMBERJACK_NUMBER = ARCHON_NUMBER+5;

  //ENEMY COUNT
  public static final int ENEMY_ARCHON_NUMBER = LUMBERJACK_NUMBER+1;
  public static final int ENEMY_GARDENER_NUMBER = ENEMY_ARCHON_NUMBER+1;
  public static final int ENEMY_CLUMP_NUMBER = ENEMY_ARCHON_NUMBER+2;

  //DISTRESS MODE
  public static final int DANGER_BUILD_ROUND = ENEMY_CLUMP_NUMBER+1;
  public static final int DISTRESS_NUMBER = DANGER_BUILD_ROUND+1;

  public static void updateArchon(RobotController rc,MapLocation archon, int roundNumber, int archonID) throws GameActionException
  {
    int numArchons = rc.readBroadcast(ENEMY_ARCHON_NUMBER);
    System.out.println("There are " + numArchons + "Enemy Archons at the moment");
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
       */
      if (firstZero == -1)
      {
        archonNumber = numArchons;
        rc.broadcast(ENEMY_ARCHON_NUMBER,numArchons+1);
        System.out.println("This brings the new Archon count up to " + numArchons+1);
      }
      else
        archonNumber = firstZero;
    }
    //if (roundNumber % 10 == 0)
    //  System.out.println("Found archon #" + (archonNumber + 1) + " in round " + roundNumber);
    broadcastLocation(rc,ARCHON1 + SLOTS_USED_PER_LOCATION*archonNumber,archon,roundNumber,archonID); //Update the archon that is closest to the updated value
  }
  public static int updateTargetRobot(RobotController rc,MapLocation location, int roundNumber, int robotID, Boolean friendly) throws GameActionException {

    int robotNumber = -1;
    int firstZero = -1;
    int oldest = 0;
    int oldestAge = 0;
    int ROBOT1 = GARDENER1;
    int ROBOT_NUMBER = ENEMY_GARDENER_NUMBER;

    if(friendly) {
      ROBOT1 = DISTRESS1;
      ROBOT_NUMBER = DISTRESS_NUMBER;
    }

    int numBots = rc.readBroadcast(ROBOT_NUMBER);

    for (int i = 0; i < numBots; i++) {
      int age = roundNumber - rc.readBroadcast(ROBOT1 + i * SLOTS_USED_PER_LOCATION + INDEX_FOR_ROUND);
      if (friendly && age>10) {
        rc.broadcast(ROBOT1 + i * SLOTS_USED_PER_LOCATION + INDEX_FOR_ID_OR_NUM, 0);
      }
      int currentRobotID = rc.readBroadcast(ROBOT1 + i * SLOTS_USED_PER_LOCATION + INDEX_FOR_ID_OR_NUM);
      if (currentRobotID == robotID)
        robotNumber = i;
      else if (currentRobotID == 0 && firstZero == -1)
        firstZero = i;
      else if (age > oldestAge) {
          oldest = i;
          oldestAge = age;
      }
    }

    System.out.println("REPORT DISTRESS");
    System.out.println(robotNumber);
    System.out.println(numBots);

    if (robotNumber == -1)
    {
      System.out.println("This gardener is new or was presumed dead");
      if (numBots != 20)
        rc.broadcast(ROBOT_NUMBER,numBots+1);
      if (firstZero != -1)
      {
        System.out.println("This gardener will fill a slot that was unoccupied");
        robotNumber = firstZero;
      }
      else
      {
        if (numBots < 20)
        {
          robotNumber = numBots;
          System.out.println("This gardener is new and will be added at the end");
        }
        else
        {
          robotNumber = oldest;
          System.out.println("Here comes the 20th gardener!");
        }
      }
    }
    broadcastLocation(rc,ROBOT1 + SLOTS_USED_PER_LOCATION*robotNumber,location,roundNumber,robotID);
    return robotNumber;
  }

  public static void removeTargetRobot(RobotController rc, int robotID, Boolean friendly) throws GameActionException{
    System.out.println("REMOVE TARGET");
    int robotNumber = -1;
    int firstZero = -1;
    int ROBOT1 = GARDENER1;
    int ROBOT_NUMBER = ENEMY_GARDENER_NUMBER;
    if(friendly) {
      ROBOT1 = DISTRESS1;
      ROBOT_NUMBER = DISTRESS_NUMBER;
    }
    for (int i = 0; i < rc.readBroadcast(ROBOT_NUMBER); i++) {
      int currentRobotID = rc.readBroadcast(ROBOT1 + i * SLOTS_USED_PER_LOCATION + INDEX_FOR_ID_OR_NUM);
      if (currentRobotID == robotID) {
        rc.broadcast(ROBOT1 + i * SLOTS_USED_PER_LOCATION + INDEX_FOR_ID_OR_NUM, 0);
        rc.broadcast(ROBOT_NUMBER, rc.readBroadcast(ROBOT_NUMBER)-1);
      }
    }
  }

  public static MapLocation closestDistress(RobotController rc) throws  GameActionException{
    MapLocation loc = rc.getLocation();
    MapLocation distress;
    MapLocation goal = new MapLocation(-1, -1);
    float distressX;
    float distressY;
    float minDistance = Float.MAX_VALUE;
    float distance;
    for (int i = 0; i < rc.readBroadcast(DISTRESS_NUMBER); i++){
      if(rc.readBroadcast(DISTRESS1 + i * SLOTS_USED_PER_LOCATION + INDEX_FOR_ID_OR_NUM) != 0) {
        distressX = rc.readBroadcast(DISTRESS1 + i * SLOTS_USED_PER_LOCATION + INDEX_FOR_X);
        distressY = rc.readBroadcast(DISTRESS1 + i * SLOTS_USED_PER_LOCATION + INDEX_FOR_Y);
        distress = new MapLocation(distressX, distressY);
        distance = loc.distanceTo(distress);
        if (distance < minDistance) {
          minDistance = distance;
          goal = distress;
        }
      }
    }
    System.out.println("READ DISTRESS");
    if(goal.x!=-1) {
      System.out.println(goal.x);
      System.out.println(goal.y);
    }
    return goal;
  }

/*
  Changes the Archon number to the round number???
  public static void reportEnemy(RobotController rc, MapLocation enemy, int round) throws GameActionException
  {
    int enemyClumps = rc.readBroadcast(ENEMY_CLUMP_NUMBER);
    for (int i=0; i<enemyClumps;i++)
    {
      MapLocation currentClump = new MapLocation(rc.readBroadcast(ENEMY_CLUMP1 + SLOTS_USED_PER_LOCATION*i + INDEX_FOR_X),rc.readBroadcast(ENEMY_CLUMP1 + SLOTS_USED_PER_LOCATION*i + INDEX_FOR_Y));
      int numEnemies = rc.readBroadcast(ENEMY_CLUMP1 + SLOTS_USED_PER_LOCATION*i + INDEX_FOR_ID_OR_NUM);
      if (currentClump.distanceTo(enemy) < 5)
      {
        broadcastLocation(rc,ENEMY_CLUMP1 + SLOTS_USED_PER_LOCATION*i,new MapLocation(currentClump.x*(numEnemies-1)/numEnemies + enemy.x/numEnemies,currentClump.y*(numEnemies-1)/numEnemies + enemy.y/numEnemies),round,++numEnemies);
        return;
      }
    }
    broadcastLocation(rc,ENEMY_CLUMP1 + SLOTS_USED_PER_LOCATION*enemyClumps,enemy,round,1);
    rc.broadcast(ENEMY_CLUMP_NUMBER,enemyClumps+1);
  }
  public static void reportEnemyDead(RobotController rc, MapLocation enemy, int round) throws GameActionException
  {
    int enemyClumps = rc.readBroadcast(ENEMY_CLUMP_NUMBER);
    for (int i=0; i<enemyClumps;i++)
    {
      MapLocation currentClump = new MapLocation(rc.readBroadcast(ENEMY_CLUMP1 + SLOTS_USED_PER_LOCATION*i + INDEX_FOR_X),rc.readBroadcast(ENEMY_CLUMP1 + SLOTS_USED_PER_LOCATION*i + INDEX_FOR_Y));
      int numEnemies = rc.readBroadcast(ENEMY_CLUMP1 + SLOTS_USED_PER_LOCATION*i + INDEX_FOR_ID_OR_NUM);
      if (currentClump.distanceTo(enemy) < 5)
      {
        broadcastLocation(rc,ENEMY_CLUMP1 + SLOTS_USED_PER_LOCATION*i,new MapLocation(currentClump.x*(numEnemies-1)/numEnemies - enemy.x/numEnemies,currentClump.y*(numEnemies-1)/numEnemies - enemy.y/numEnemies),round,numEnemies-1);
        return;
      }
    }
  }*/

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
    return new MapLocation(rc.readBroadcast(channel+INDEX_FOR_X),rc.readBroadcast(channel+INDEX_FOR_Y));
  }

  public static int robotTypeBroadcastNum(RobotType type)
  {
    switch(type)
    {
      case GARDENER: return Broadcasting.GARDENER_NUMBER;
      case SOLDIER: return Broadcasting.SOLDIER_NUMBER;
      case TANK: return Broadcasting.TANK_NUMBER;
      case LUMBERJACK: return Broadcasting.LUMBERJACK_NUMBER;
      case ARCHON: return Broadcasting.ARCHON_NUMBER;
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
292: Number of enemy clumps

 */

//Gardener + Archons

/*
Idea is to have units flock to clumps of enemies based on number of enemies in clump
 */