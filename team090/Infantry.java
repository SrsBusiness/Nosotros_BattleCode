package team090;
import battlecode.common.Direction;
import battlecode.common.GameConstants;
import battlecode.common.RobotController;
import battlecode.common.RobotType;
import battlecode.common.*;
import java.util.*;

class Infantry extends Role{
    int mode = 0;    
    int range = 4;
    double advantage = 0;
    MapLocation myLocation;
    boolean offensive = false;
    
    Infantry(RobotController rc) {
        super(rc);
    }
   
    void execute(){
        try {
            if(rc.isActive()) {
                if (Clock.getRoundNum() == 1999) {
                    double myMilk = rc.senseTeamMilkQuantity(myTeam);
                    double enemyMilk = rc.senseTeamMilkQuantity(notMyTeam);
                    if ((myMilk - enemyMilk) > GameConstants.HAT_MILK_COST + 5000) {
                        rc.wearHat();
                    }
                }
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
                if (health <= 30) {
                    //Always flee if on low health.
                    mode = 2;
                    range = 0;
                    target = allyHQLocation.subtract(enemyDir).subtract(enemyDir);
                } else if (allyRobotInfo.size() < 3) {
                    //Never solo. Wait at the base to regroup if stranded.
                    mode = 1;
                    range = 0;
                    target = allyHQLocation.subtract(enemyDir).subtract(enemyDir);
                } else if (advantage < 0) {
                    //In a disadvantage, run back to base.
                    //mode = 0;
                    //range = 3;
                    //In a disadvantage, flee back to base.
                    mode = 2;
                    range = 0;
                    target = allyHQLocation.subtract(enemyDir).subtract(enemyDir);
                }/* else if (advantage > 20000) {
                        mode = 3;
                        range = 0;
                        target = allyHQLocation;
                    }*/
                else {
                    //By default, charge to their base.
                    mode = 0;
                    range = 7;
                    target = enemyHQLocation;
                }
//                System.out.printf("%d: Mode: %d, advantage: %f, HP: %f\n",
//                        Clock.getRoundNum(), mode, advantage, health);
                /*
                if (fear == 0 && rc.getHealth() > 70) {
                    mode = 0;
                } else if (fear == 10) {
                    target = allyHQLocation.subtract(enemyDir).subtract(enemyDir);
                    mode = 3;
                } else if (fear > 0) {
                    mode = 1;
                } 
                if (fear > 4) {
                    myLocation = rc.getLocation(); //JUST IN CASE PLS
                    tryToWalk(myLocation, allyRobotInfo, enemyRobotInfo, mode);
                    return;
                } else if (enemyRobotInfo.size() > 0) {
                    //Attacking, selecting the enemy with the lowest health.
                    RobotInfo attackTarget = getWeakestTargetInRange(myLocation,
                                                                     enemyRobotInfo);
                    if (attackTarget != null) {
                        rc.attackSquare(attackTarget.location);
                        return;
                    }
                }
                if (allyRobotInfo.size() < 3) {
                    mode = 1;
                }
                */
                if (mode == 0 || mode == 1) {
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
                //Moving, using vector field.
                tryMove(myLocation, allyRobotInfo, enemyRobotInfo, mode, range);
            }
        } catch(Exception e) {
            e.printStackTrace();
            System.err.println(e + " Infantry Exception");
        }
    }
}

