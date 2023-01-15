package josh_001;

import battlecode.common.*;

public class Headquarters {
    static int my_array_address;
    static String[] states = {"initial","scout_building"};
    /**
     * Run a single turn for a Headquarters.
     * This code is wrapped inside the infinite loop in run(), so it is called once per turn.
     */
    static void runHeadquarters(RobotController rc) throws GameActionException {
        // first round save our location to the array in the first available spot. 0-3
        if(rc.getRoundNum() == 1) {
            for (int i = 0; i < 4; i++) {
                int array_i = rc.readSharedArray(i);
                if (array_i == 0) {
                    my_array_address = i;
                    rc.writeSharedArray(i, RobotPlayer.packMapLocationExtra(rc.getLocation(), 0));
                    System.out.println(rc.getLocation());
                    System.out.println(RobotPlayer.packMapLocationExtra(rc.getLocation(), 0));
                    break;
                }
            }
        }

        //round 10 print the array to console
        if(rc.getRoundNum() == 10){
            MapLocation mine = RobotPlayer.unpackMapLocation(rc.readSharedArray(my_array_address));
            System.out.println("advertising my location" + my_array_address + " as " + mine + " from " + rc.readSharedArray(my_array_address));
        }
        // Pick a direction to build in.
        Direction dir = RobotPlayer.directions[RobotPlayer.rng.nextInt(RobotPlayer.directions.length)];
        MapLocation newLoc = rc.getLocation().add(dir);
        if (RobotPlayer.rng.nextBoolean()) {
            // Let's try to build a carrier.
            rc.setIndicatorString("Trying to build a carrier");
            if (rc.canBuildRobot(RobotType.CARRIER, newLoc)) {
                rc.buildRobot(RobotType.CARRIER, newLoc);
            }
        } else {
            // Let's try to build a launcher.
            rc.setIndicatorString("Trying to build a launcher");
            if (rc.canBuildRobot(RobotType.LAUNCHER, newLoc)) {
                rc.buildRobot(RobotType.LAUNCHER, newLoc);
            }
        }
    }
}
