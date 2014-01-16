
package team090;
import battlecode.common.Direction;
import battlecode.common.GameConstants;
import battlecode.common.RobotController;
import battlecode.common.RobotType;
import battlecode.common.*;
import java.util.*;

class CowBoy{
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

    static void run(RobotController rc){
        while(true){
            if(rc.isActive()){
                try {
                    if (rc.canMove(Direction.WEST) && (rc.getLocation().x+8)%8 != 1) {
                        rc.move(Direction.WEST);
                    } else {
                        rc.construct(RobotType.NOISETOWER);
                    }
                } catch(Exception e) {
                    System.err.println(e + " CowBoy Exception");
                }
            }
        }
    }
}
