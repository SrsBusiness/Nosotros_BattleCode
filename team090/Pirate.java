package team090;
import battlecode.common.Direction;
import battlecode.common.GameConstants;
import battlecode.common.RobotController;
import battlecode.common.RobotType;
import battlecode.common.*;
import java.util.*;

class Pirate{
    static Direction[] directions = {
        Direction.NORTH, 
        Direction.NORTH_EAST, 
        Direction.EAST, 
        Direction.SOUTH_EAST, 
        Direction.SOUTH, 
        Direction.SOUTH_WEST, 
        Direction.WEST, 
        Direction.NORTH_WEST };
    static Random rand;
    static int lifeTurn = 0;
    static int broadcastIn;
    static int commandMode;
    // true = down, false = up
    static boolean patrolDir;
    static int width, height;
    static void run(RobotController rc){
        width = rc.getMapWidth();
        height = rc.getMapHeight();
        while(true){ 
            rc.yield();
        }
    }
    static MapLocation[] corners(final RobotController rc){
        class LocComparator implements Comparator<MapLocation>{
            public int compare(MapLocation l1, MapLocation l2){
                MapLocation enemy = rc.senseEnemyHQLocation();
                return l2.distanceSquaredTo(enemy) - l1.distanceSquaredTo(enemy);
            }
        }
        MapLocation[] result = new MapLocation[]{new MapLocation(3, 3), 
            new MapLocation(3, height - 4), 
            new MapLocation(width - 4, 3), 
            new MapLocation(width - 4, height - 4)};
        Arrays.sort(result, new LocComparator());
        return result;
    }
    static void moveToCorner(RobotController rc, MapLocation corner){
        while(!rc.getLocation().equals(corner)){
            try {
                if(rc.isActive())
                    rc.move(rc.getLocation().directionTo(corner));
            } catch(Exception e) {
                System.err.println(e + " Pirate Exception");
            }
            rc.yield();
        }
    }
}
