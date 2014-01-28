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

    //BENCHMARKING
    int bytecodes;
    
    Infantry(RobotController rc) {
        super(rc);
    }
   
    void execute(){
        try {
            if(rc.isActive()) {
                /*
                Robot[] nearbyRobots = rc.senseNearbyGameObjects(Robot.class, 36, myTeam);
                ArrayList<RobotInfo> nearbyAllyInfo = new ArrayList<RobotInfo>();
                ArrayList<RobotInfo> nearbyEnemyInfo = new ArrayList<RobotInfo>();
                RobotInfo info;
                for (Robot r: nearbyRobots) {
                    info = rc.senseRobotInfo(r);
                        if (info.type == RobotType.SOLDIER)
                            nearbyAllyInfo.add(info);
                }
                nearbyRobots = rc.senseNearbyGameObjects(Robot.class, 36, notMyTeam);
                for (Robot r: nearbyRobots) {
                    info = rc.senseRobotInfo(r);
                        if (info.type == RobotType.SOLDIER)
                    if (info.type == RobotType.SOLDIER)
                        nearbyEnemyInfo.add(info);
                }
                //Bytecode cost -> 304+(122+87n)=426+87n up to here (as of 64fca1348b7d7f60bb3d131a09b03389c85bdb6b)
                //Worst -> 1k (n~=6)
                System.out.println(nearbyAllyInfo.size() + ", " + nearbyEnemyInfo.size() + " : "+Clock.getBytecodeNum());*/
                ///*
                Robot[] nearbyRobots = rc.senseNearbyGameObjects(Robot.class, 36);
                ArrayList<RobotInfo> allyInfo = new ArrayList<RobotInfo>();
                ArrayList<RobotInfo> enemyInfo = new ArrayList<RobotInfo>();
                //Populate the knowledge of all nearby robots.
                RobotInfo info;
                for (Robot r: nearbyRobots) {
                    info = rc.senseRobotInfo(r);
                    if (info.team == myTeam) {
                        allyInfo.add(info);
                    } else {
                        enemyInfo.add(info);
                    }
                }
                //Bytecode cost -> 48+(360+89n)=408
                //BENCHMARKING
                bytecodes = Clock.getBytecodeNum();
                //System.out.println(allyInfo.size() + ", " + enemyInfo.size() + " : "+Clock.getBytecodeNum());*/
                ////////
                currLoc = rc.getLocation();
                currHP = rc.getHealth();
                advantage = calculateAdvantage(myLocation, allyRobotInfo, enemyRobotInfo);
                System.out.println(Clock.getBytecodeNum()-bytecodes);
                return;
            }
        } catch(Exception e) {
            e.printStackTrace();
            System.err.println(e + " Infantry Exception");
        }
    }
}

