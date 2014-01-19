package team090;
import battlecode.common.Direction;
import battlecode.common.GameConstants;
import battlecode.common.RobotController;
import battlecode.common.RobotType;
import battlecode.common.*;
import java.util.*;

class Farmer extends Role{
    Direction[] directions = {
        Direction.NORTH, 
        Direction.NORTH_EAST, 
        Direction.EAST, 
        Direction.SOUTH_EAST, 
        Direction.SOUTH, 
        Direction.SOUTH_WEST, 
        Direction.WEST, 
        Direction.NORTH_WEST };
    Random rand;
    int lifeTurn = 0;
    int broadcastIn;
    int commandMode;
    // true = down, false = up
    boolean patrolDir;

    void execute(){
    }

    void run(RobotController rc){
        System.out.println("Cowgirl here, Howdy.");
        while(true){
            if(rc.isActive()){
                try {
                    if (rc.canMove(Direction.WEST) && (rc.getLocation().x+8)%8 != 0) {
                        rc.move(Direction.WEST);
                    } else if (rc.canMove(Direction.SOUTH) && (rc.getLocation().y+8)%8 != 0) {
                        rc.move(Direction.SOUTH);
                    } else {
                        rc.construct(RobotType.PASTR);
                    }
                } catch(Exception e) {
                    System.err.println(e + " CowGirl Exception");
                }
            }
        }
    }
}
