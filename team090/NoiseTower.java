package team090;
import battlecode.common.Direction;
import battlecode.common.GameConstants;
import battlecode.common.RobotController;
import battlecode.common.RobotType;
import battlecode.common.*;
import java.util.*;

class NoiseTower extends Role{
    static Direction[] directions = {
        Direction.NORTH, 
        Direction.NORTH_EAST, 
        Direction.EAST, 
        Direction.SOUTH_EAST, 
        Direction.SOUTH, 
        Direction.SOUTH_WEST, 
        Direction.WEST, 
        Direction.NORTH_WEST };
    static MapLocation target;
 
    void execute(){
        target = rc.getLocation();
        target.add(6,0);
        while(true){
            try {
                rc.attackSquareLight(target);
            } catch (Exception e) {
                System.err.println(e.toString() + "Noisetower Exception");
            }
            rc.yield();
        }
    }
}
