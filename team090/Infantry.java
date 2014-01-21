package team090;
import battlecode.common.Direction;
import battlecode.common.GameConstants;
import battlecode.common.RobotController;
import battlecode.common.RobotType;
import battlecode.common.*;
import java.util.*;

class Infantry extends Role{
    
    Infantry(RobotController rc) {
        super(rc);
    }

    void execute(){
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
                    Robot bestTarget = getBestTarget(nearbyEnemies, rc);
                    if (bestTarget != null) {
                        RobotInfo targetInfo = rc.senseRobotInfo(bestTarget);
                        rc.attackSquare(targetInfo.location);
                        rc.yield();
                        return;
                    }
                }

                MapLocation myLocation = rc.getLocation();
                myTrail.offer(myLocation);
                if (myTrail.size() > 9) {
                    myTrail.remove();
                }

                Direction moveDirection = chooseDir(rc, myTrail);
                if (rc.canMove(moveDirection)) {
                    rc.move(moveDirection);
                } else {
                    rc.yield();
                    return;
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
            System.err.println(e + " Infantry Exception");
        }
    }

}

