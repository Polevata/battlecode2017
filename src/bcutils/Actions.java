package bcutils;

import battlecode.common.*;
import scala.tools.nsc.transform.patmat.Logic;

/**
 * Created by bgheneti on 1/14/17.
 */
public class Actions{
  public enum ActionType {
    PLANT, MOVE, BUILD_GARDENER, BUILD_SCOUT, BUILD_SOLDIER, BUILD_LUMBERJACK, BUILD_TANK
  }

  public static boolean dispatchAction(RobotController rc, ActionType action, Direction dir) throws GameActionException {
    switch(action) {
      case PLANT:
        if(rc.canPlantTree(dir)) {
          rc.plantTree(dir);
          return true;
        }
        return false;
      case MOVE:
        if(rc.canMove(dir)) {
          rc.move(dir);
          return true;
        }
        return false;
      default:
        RobotType buildType;

        switch(action){
          case BUILD_GARDENER: buildType = RobotType.GARDENER;
          case BUILD_LUMBERJACK: buildType = RobotType.LUMBERJACK;
          case BUILD_SCOUT: buildType = RobotType.SCOUT;
          case BUILD_SOLDIER: buildType = RobotType.SOLDIER;
          default: buildType = RobotType.TANK;
        }

        if(rc.canBuildRobot(buildType, dir)) {
          rc.buildRobot(buildType, dir);
          return true;
        }
        return false;
    }
  }
}
