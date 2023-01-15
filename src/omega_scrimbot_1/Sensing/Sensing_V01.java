package omega_scrimbot_1.Sensing;

import battlecode.common.*;

public class Sensing_V01 {

    // Scan for HQ
    public static MapLocation scanHQ(RobotController rc) throws GameActionException {
        RobotInfo[] robots = rc.senseNearbyRobots();
        for (RobotInfo robot : robots) {
            if (robot.getTeam() == rc.getTeam() && robot.getType() == RobotType.HEADQUARTERS){
                return robot.getLocation();
            }
        }
        return null;
    }
}
