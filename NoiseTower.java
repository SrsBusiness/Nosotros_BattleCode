package team090;
import battlecode.common.Direction;
import battlecode.common.GameConstants;
import battlecode.common.RobotController;
import battlecode.common.RobotType;
import battlecode.common.*;
import java.util.*;

class NoiseTower{
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
 
    static void NoiseTower_run(RobotController rc){
        target = rc.getLocation();
        target.add(6,0);
        while(true){
            try {
                if (rc.isActive()) {
                    /*
                    rotate += lookRate;
                    if(rotCount % 16 == 0) {
                        dist-=2;
                        if(dist < minDist) {
                            dist = maxDist;
                        }
                    }
                    MapLocation attack = null;
                    int localDist = dist;
                    while(attack == null && localDist > 0) {
                        attack = getAttackTile(rotate, localDist);
                        localDist--;
                    }
                    rotCount++;
                    rc.attackSquare(attack);
                    */
                    rc.attackSquare(target);
                    //rc.attackSquareLight(target);
                }
            } catch (Exception e) {
                System.err.println(e.toString() + "Noisetower Exception");
            }
            rc.yield();
        }
    }
}
