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
    static RobotController rc;
    static int lifeTurn = 0;
    static int broadcastIn;
    static int commandMode;
    static MapLocation myLocation;
    static MapLocation target = null;

    static void Soldier_run() {
        rand = new Random();
        while(true){
            System.out.println(lifeTurn);
            try {
                if (rc.isActive()) {
                    broadcastIn = rc.readBroadcast(1);
                    if (broadcastIn == rc.getRobot().getID()) {
                        commandMode = rc.readBroadcast(2);
                        rc.broadcast(1, 0);
                        rc.broadcast(2, 0);
                        System.out.println(commandMode);
                    }
                    lifeTurn++;
                    if (lifeTurn == 2) {
                        rc.broadcast(0, rc.getRobot().getID());
                        lifeTurn++;
                    }
                    //Execute unit-specific duties
                    switch (commandMode) {
                        //Default behavior
                        case 0:
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
                            break;
                        //PASTR cowgirl
                        case 1:
                            System.out.println("Cowgirl's turn.");
                            if (target == null) {
                                System.out.println("Setting PASTR location.");
                                target = rc.senseHQLocation().add(2, 2);
                            }
                            myLocation = rc.getLocation();
                            //Move to target, then make PASTR.
                            if (myLocation.equals(target)) {
                                rc.construct(RobotType.PASTR);
                            } else {
                                //TODO: implement real pathfinding to target.
                                Direction bestChoice;
                                if (myLocation.y < target.y) {
                                    bestChoice = Direction.SOUTH;
                                } else if (myLocation.y > target.y) {
                                    bestChoice = Direction.NORTH;
                                } else if (myLocation.x > target.x) {
                                    bestChoice = Direction.WEST;
                                } else if (myLocation.x < target.x) {
                                    bestChoice = Direction.EAST;
                                } else {
                                    bestChoice = Direction.NONE;
                                }
                                if (bestChoice != Direction.NONE && rc.canMove(bestChoice)) {
                                    rc.move(bestChoice);
                                } else {
                                    //TODO: try each dir
                                    Direction randChoice = directions[rand.nextInt(8)];
                                    if (rc.canMove(randChoice)) {
                                        rc.move(randChoice);
                                    }
                                }
                            }
                            break;
                        //Noisetower cowboy
                        case 2:
                            if (target == null) {
                                System.out.println("Setting Noisetower location.");
                                target = rc.senseHQLocation().add(2, 1);
                            }
                            myLocation = rc.getLocation();
                            if (myLocation.equals(target)) {
                                rc.construct(RobotType.NOISETOWER);
                                return;
                            } else {
                                //TODO: implement real pathfinding to target.
                                Direction bestChoice;
                                if (myLocation.y < target.y) {
                                    bestChoice = Direction.SOUTH;
                                } else if (myLocation.y > target.y) {
                                    bestChoice = Direction.NORTH;
                                } else if (myLocation.x > target.x) {
                                    bestChoice = Direction.WEST;
                                } else if (myLocation.x < target.x) {
                                    bestChoice = Direction.EAST;
                                } else {
                                    bestChoice = Direction.NONE;
                                }
                                if (bestChoice != Direction.NONE && rc.canMove(bestChoice)) {
                                    rc.move(bestChoice);
                                } else {
                                    //TODO: try each dir
                                    Direction randChoice = directions[rand.nextInt(8)];
                                    if (rc.canMove(randChoice)) {
                                        rc.move(randChoice);
                                    }
                                } 
                            }
                            break;
                    }
                }
            } catch (Exception e) {
                System.err.println(e.toString() + "Soldier Exception");
            }
            rc.yield();
        }
    }
}
