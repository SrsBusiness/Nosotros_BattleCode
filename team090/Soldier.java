package team090;
import battlecode.common.Direction;
import battlecode.common.GameConstants;
import battlecode.common.RobotController;
import battlecode.common.RobotType;
import battlecode.common.*;
import java.util.*;

class Soldier {
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
    
    static void run(RobotController rc) {
            rand = new Random();
            try {
                if (rc.isActive()) {
                    broadcastIn = rc.readBroadcast(1);
                    lifeTurn++;
                    if (lifeTurn == 2) {
                        rc.broadcast(0, rc.getRobot().getID());
                        lifeTurn++;
                    }
                    //Execute unit-specific duties
                    if (broadcastIn == rc.getRobot().getID()) {
                        commandMode = rc.readBroadcast(2);
                    }
                    switch (commandMode) {
                        //Default behavior
                        case 0:
                            while(true) {
                                try {
                                    if(rc.isActive()){
                                        Robot[] nearbyEnemies = rc.senseNearbyGameObjects(Robot.class, 10, rc.getTeam().opponent());
                                        if (nearbyEnemies.length > 0) {
                                            rc.attackSquare(rc.senseRobotInfo(RobotPlayer.getBestTarget(nearbyEnemies, rc)).location);
                                        } else {
                                            Direction moveDirection = RobotPlayer.chooseDir(rc);
                                            if (rc.canMove(moveDirection)) {
                                                rc.move(moveDirection);
                                            } else {
                                                //Try a random direction
                                                //TODO: try each dir
                                                Direction randChoice = directions[rand.nextInt(8)];
                                                if (rc.canMove(randChoice)) {
                                                    rc.move(randChoice);
                                                }
                                            }
                                        }
                                    }
                                } catch(Exception e) {
                                    System.err.println(e + " Soldier Exception");
                                }
                            }
                        //PASTR cowgirl
                        case 1:
                            CowGirl.run(rc);  
                            break;
                        //Noisetower cowboy
                        case 2:
                            CowBoy.run(rc); 
                            break;
                        // pirate #1 - furthest corner from enemy
                        case 3:
                            Pirate.run(rc);
                            break;
                        // pirate #2 - 2nd furthest corner from enemy
                        case 4:
                            Pirate.run(rc);
                            break;
                    }
                }
            } catch (Exception e) {
                System.err.println(e.toString() + "Soldier Exception");
            }
            rc.yield();
    }

    // returns corner locations in decending order of distance from enemy HQ
    
}
