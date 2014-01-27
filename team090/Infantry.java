package team090;
import battlecode.common.Direction;
import battlecode.common.GameConstants;
import battlecode.common.RobotController;
import battlecode.common.RobotType;
import battlecode.common.*;
import java.util.*;

class Infantry extends Role{
    int mode = 0;    
    double range = 4;
    double advantage = 0;

    MapLocation dst = null;
    MapLocation lastDst = null;
    MapLocation currWaypoint = null;
    int index = 0;
    
    Infantry(RobotController rc) {
        super(rc);
    }
   
    void execute(){
        try {
            if(rc.isActive()) {
                // if (Clock.getRoundNum() == 1999) {
                //     double myMilk = rc.senseTeamMilkQuantity(myTeam);
                //     double enemyMilk = rc.senseTeamMilkQuantity(notMyTeam);
                //     if ((myMilk - enemyMilk) > GameConstants.HAT_MILK_COST + 5000) {
                //         rc.wearHat();
                //     }
                // }
                Robot[] nearbyRobots = rc.senseNearbyGameObjects(Robot.class);
                ArrayList<RobotInfo> allyRobotInfo = new ArrayList<RobotInfo>();
                ArrayList<RobotInfo> enemyRobotInfo = new ArrayList<RobotInfo>();
                //Populate the knowledge of all nearby robots.
                RobotInfo info;
                for (Robot r: nearbyRobots) {
                    info = rc.senseRobotInfo(r);
                    if (info.team == myTeam) {
                        allyRobotInfo.add(info);
                    } else if (info.team == notMyTeam) {
                        enemyRobotInfo.add(info);
                    }
                }
                //Set current location
                myLocation = rc.getLocation();
                health = rc.getHealth();
                advantage = calculateAdvantage(myLocation, allyRobotInfo, enemyRobotInfo);
                int nearbyAllyCount = 0;
                for (RobotInfo i: allyRobotInfo) {
                    //GA TODO: parameterize the distance.
                    if (i.location.distanceSquaredTo(myLocation) < 16) {
                        nearbyAllyCount++;
                        if (nearbyAllyCount >= 3)
                            break;
                    }
                }
                //Mode selection
                if (health <= 30 &&
                    advantage < 0 &&
                    enemyRobotInfo.size() > 1) {
                    mode = 2;
                    range = 0;
                    dst = allyHQLocation.subtract(enemyDir).subtract(enemyDir);
                } else if (nearbyAllyCount < 3) {
                    //Never solo. Wait at the base to regroup if stranded.
                    mode = 1;
                    range = 0;
                    dst = allyHQLocation.subtract(enemyDir).subtract(enemyDir);
                } else if (advantage == 0 && enemyRobotInfo.size() > 0) {
                    //Cold war tactics:
                    //Don't move.
                    mode = 5;
                    range = 0;//3.163;
                    dst = myLocation;
                } else if (advantage < 0) {
                    //In a disadvantage, run back to base.
                    //mode = 0;
                    //range = 3;
                    //In a disadvantage, flee back to base.
                    mode = 2;
                    range = 0;
                    dst = allyHQLocation.subtract(enemyDir).subtract(enemyDir);
                } else if (advantage > 20000 &&
                           enemyRobotInfo.size() > 1) {
                    mode = 3;
                    range = 0;
                    dst = enemyHQLocation;
                    //dst = new MapLocation(mapWidth/2, mapHeight/2);
                } else {
                    //By default, charge to their base.
                    mode = 0;
                    range = 0;//7;
                    dst = enemyHQLocation;
                }
                /*
                dst = enemyHQLocation;
                if (dst.distanceSquaredTo(myLocation) > 64) {
                    //Switching destinations
                    if (dst != null &&
                        lastDst != dst) {
                        findPath(myLocation, dst);
                        lastDst = dst;
                        index = 12;
                        currWaypoint = wayPoints.get(index);
                        target = currWaypoint;
                        System.out.println("Initial waypoint:"+currWaypoint.x+", "+currWaypoint.y);
                    }
                    //If at the next waypoint
                    if (currWaypoint.distanceSquaredTo(myLocation) < 4) {
                        currWaypoint = wayPoints.get(index);
                        index += 8;
                        target = currWaypoint;
                        System.out.println("new waypoint:"+currWaypoint.x+", "+currWaypoint.y);
                    }
                } else {
                    target = dst;
                }
                range = 0;
                mode = 0;
                */
                //
                range = 0; mode = 0;
                dst = enemyHQLocation;
                if (lastDst == null) {
                    findPath(myLocation, dst);
                    lastDst = dst;
                    index = 12;
                    target = wayPoints.get(index);
                } 
                if (target.distanceSquaredTo(rc.getLocation()) <= 4) {
                    index += 8;
                    target = wayPoints.get(index);
                }
                //
                rc.setIndicatorString(1, "Mode: "+mode+", Advantage: "+advantage);
                if (mode == 0 || mode == 1 || mode == 5) {
                    //If in an aggressive mode, attack if possible.
                    if (enemyRobotInfo.size() > 0) {
                        //Attacking, selecting the enemy with the lowest health.
                        RobotInfo attackTarget = bestTargetInRange(myLocation,
                                enemyRobotInfo);
                        if (attackTarget != null) {
                            rc.attackSquare(attackTarget.location);
                            return;
                        }
                    }
                }
                if (mode != 5) {
                    //Moving, using vector field.
                    tryMove(myLocation, allyRobotInfo, enemyRobotInfo, mode, range);
                }
            }
        } catch(Exception e) {
            e.printStackTrace();
            System.err.println(e + " Infantry Exception");
        }
    }
}

