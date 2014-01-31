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

    int nearbyAllyCount = 0;
    int nearbyEnemyCount = 0;
    double nearbyAllyHealth = 0;
    double nearbyEnemyHealth = 0;
    double tempRange;
    double tempOptima;
    double advantage = 0;
    boolean willAttack = false;

    MapLocation dst = null;
    MapLocation lastDst = null;
    MapLocation currWaypoint = null;
    int index = 0;

    //BENCHMARKING
    int bytecodes;
    
    Infantry(RobotController rc) {
        super(rc);
    }
   
    void execute(){
        try {
            if(rc.isActive()) {
                Robot[] nearbyRobots = rc.senseNearbyGameObjects(Robot.class, 36);
                ArrayList<RobotInfo> allySoldierInfo = new ArrayList<RobotInfo>();
                ArrayList<RobotInfo> enemySoldierInfo = new ArrayList<RobotInfo>();
                ArrayList<RobotInfo> enemyPastrInfo = new ArrayList<RobotInfo>();
                //Populate the knowledge of all nearby robots.
                RobotInfo info;
                for (Robot r: nearbyRobots) {
                    info = rc.senseRobotInfo(r);
                    if (info.team == myTeam &&
                        info.type == RobotType.SOLDIER) {
                        allySoldierInfo.add(info);
                    } else {
                        switch (info.type) {
                            case SOLDIER:
                                enemySoldierInfo.add(info);
                                break;
                            case PASTR:
                                enemyPastrInfo.add(info);
                                break;
                        }
                    }
                }
                //Bytecode cost -> 48+(360+89n)=408
                //System.out.println(allyInfo.size() + ", " + enemyInfo.size() + " : "+Clock.getBytecodeNum());*/
                currLoc = rc.getLocation();
                currHP = rc.getHealth();
                nearbyAllyCount = 0;
                nearbyAllyHealth = 0;
                nearbyEnemyCount = 0;
                nearbyEnemyHealth = 0;

                for (RobotInfo ri: allySoldierInfo) {
                    //GA TODO: paramize. (5)
                    if (currLoc.distanceSquaredTo(ri.location) < 16) {
                        nearbyAllyCount++;
                        nearbyAllyHealth += ri.health;
                    }
                }
                for (RobotInfo ri: enemySoldierInfo) {
                    tempRange = currLoc.distanceSquaredTo(ri.location);
                    if (tempRange < tempOptima) {
                        tempOptima = tempRange;
                    }
                    if (ri.type == RobotType.SOLDIER) {
                        nearbyEnemyCount++;
                        nearbyEnemyHealth += ri.health;
                    }
                }
                //
                rc.setIndicatorString(0, "Ally count: "+(nearbyAllyCount+1)+
                                         ", Enemy count: "+nearbyEnemyCount);
                advantage = nearbyAllyHealth+currHP - nearbyEnemyHealth;
                //Bytecode cost + (183+93n)=456+186n
                //Mode Selection
                if (currHP <= nearbyEnemyCount*10) {
                    //Retreat if could be one-shotted.
                    mode = -1;
                    willAttack = false;
                } else if (advantage == 0) {
                    //Cold war tactics
                    if (tempOptima <= 11.5) {
                        //Stand still.
                        mode = 0;
                        willAttack = true;
                    } else {
                        //Inch ahead.
                        mode = 1;
                        willAttack = true;
                    }
                } else if (advantage < 0) {
                    //Retreat.
                    mode = -1;
                } else if (advantage > 0) {
                    if (nearbyEnemyCount > 0) {
                        //A-move
                        mode = 3;
                        willAttack = true;
                    } else {
                        //Go for the enemy base when nobody is around.
                        mode = 2;
                        willAttack = true;
                    }
                } else {
                    //This never happens, but included for safety.
                    mode = 0;
                    willAttack = false;
                }
                if (willAttack) {
                    if (nearbyEnemyCount > 0) {
                        //Attacking, selecting the enemy with the lowest health.
                        RobotInfo attackTarget = bestTargetInRange(currLoc,
                                enemySoldierInfo);
                        if (attackTarget != null) {
                            rc.attackSquare(attackTarget.location);
                            return;
                        }
                    }
                }
                /*
                switch (mode) {
                    //Retreat
                    case -1:
                        ;
                        break;
                    //Stay put
                    case 0:
                        break;
                    //Inch forward
                    case 1:
                        ;
                        break;
                    //Charge to base
                    case 2:
                        ;
                        break;
                    //A-move
                    case 3:
                        ;
                        break;
                }
                */
                //BENCHMARKING
                System.out.println(Clock.getBytecodeNum());
                return;
            }
        } catch(Exception e) {
            e.printStackTrace();
            System.err.println(e + " Infantry Exception");
        }
    }
}

