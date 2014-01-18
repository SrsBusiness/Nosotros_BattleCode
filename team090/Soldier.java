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
    static double aggression;

    static MapLocation myLocation;
    static MapLocation target = null;

    static Queue<MapLocation> myTrail = new LinkedList<MapLocation>();
    //static MapLocation;

    static void run(RobotController rc) {
        rand = new Random();
        System.out.println(lifeTurn);
        try {
            if (rc.isActive()) {
                broadcastIn = rc.readBroadcast(1);
                if (broadcastIn == rc.getRobot().getID()) {
                    commandMode = rc.readBroadcast(2);
                    rc.broadcast(1, 0);
                    rc.broadcast(2, 0);
                    //System.out.println(commandMode);
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
                        while(true) {

                            broadcastIn = rc.readBroadcast(1);
                            if (broadcastIn == rc.getRobot().getID()) {
                                commandMode = rc.readBroadcast(2);
                                rc.broadcast(1, 0);
                                rc.broadcast(2, 0);
                                break;
                                //System.out.println(commandMode);
                            }
                            lifeTurn++;
                            if (lifeTurn == 2) {
                                rc.broadcast(0, rc.getRobot().getID());
                                lifeTurn++;
                            }

                            try {
                                if(rc.isActive()){
                                    Robot[] nearbyEnemies = rc.senseNearbyGameObjects(Robot.class, 10, rc.getTeam().opponent());
                                    Robot[] nearbyAllies = rc.senseNearbyGameObjects(Robot.class, 10, rc.getTeam());
                                    //TODO: Caluculate individual aggression.
                                    //TODO: Fix redundant scans
                                    if ((rc.senseNearbyGameObjects(Robot.class, 36, rc.getTeam().opponent()).length >=
                                           rc.senseNearbyGameObjects(Robot.class, 36, rc.getTeam()).length &&
                                         rc.getHealth() < 50) ||
                                         rc.getHealth() < 30) {
                                        aggression = 0;
                                    } else {
                                        aggression = 1;
                                    }
                                    if (nearbyEnemies.length > 0 && aggression > 0) {
                                        Robot bestTarget = RobotPlayer.getBestTarget(nearbyEnemies, rc);
                                        if (bestTarget != null) {
                                            RobotInfo targetInfo = rc.senseRobotInfo(bestTarget);
                                            rc.attackSquare(targetInfo.location);
                                            rc.yield();
                                            continue;
                                        }
                                    }

                                    myLocation = rc.getLocation();
                                    myTrail.offer(myLocation);
                                    if (myTrail.size() > 9) {
                                        myTrail.remove();
                                    }

                                    Direction moveDirection = RobotPlayer.chooseDir(rc, myTrail);
                                    if (rc.canMove(moveDirection)) {
                                        rc.move(moveDirection);
                                    } else {
                                        rc.yield();
                                        continue;
                                        /*
                                               No longer need such plebian local minima avoiders
                                        Direction randChoice = directions[rand.nextInt(8)];
                                        for (int i = 0; i < 8; i++) {
                                            if (rc.canMove(randChoice)) {
                                                rc.move(randChoice);
                                                break;
                                            } else {
                                                randChoice.rotateRight();
                                            }
                                        }*/
                                    }
                                }
                            } catch(Exception e) {
                                System.err.println(e + " Soldier Exception");
                            }
                            rc.yield();
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
        // returns corner locations in decending order of distance from enemy HQ
    }
}

