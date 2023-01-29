package latest;

import battlecode.common.*;

import java.util.Random;

public class Amplifier {

    final Direction[] directions = RobotPlayer.directions;
    final Random rng = RobotPlayer.rng;
    static RobotPlayer.states my_state = RobotPlayer.my_state;
    static int my_HQ = 99;
    static MapLocation my_HQ_location;
    static MapLocation[] island_locations = RobotPlayer.island_locations;
    static MapLocation target_location;
    static String indicator_string = "";
    static MapLocation myLocation;
    static MapLocation map_center;

    static void runAmplifier(RobotController rc) throws GameActionException {


        // This runs on my first turn only
        if(RobotPlayer.turnCount <= 1){
            my_HQ = RobotPlayer.get_HQ_array_index(rc);
            my_HQ_location = RobotPlayer.unpackMapLocation(rc.readSharedArray(my_HQ));
            map_center = new MapLocation(rc.getMapWidth() / 2, rc.getMapHeight() / 2);
        }

        // Each turn starts with these tasks before dealing with primary task:

        // Read Comms and update worldview


        // Update High Value Map Info
        //Sensing.scanAndUpdateIslands(rc); // very high bytecode cost

        // Scan wells

        // Read Orders and change state and goals accordingly

        // Sense nearby enemies, possibly respond accordingly




        switch(my_state){
            case INITIAL:
                my_state = RobotPlayer.states.SCOUT;
                break;
            case SCOUT:
                // Refresh MapInfo

                if (target_location != null) {
                    // If we reached it, no need for the target location any more.
                    if (rc.getLocation().isWithinDistanceSquared(target_location, 5)) {
                        target_location = null;
                    }
                }
                else {
                    // Try to move according to clockwise preference
                    Direction dir = Pathing.getRotateValidMove(rc, RobotPlayer.lastMoved, RobotPlayer.prefersClockwise);
                    Pathing.trackedMove(rc, dir);
                }

                break;

            case DEFAULT:
                my_state = RobotPlayer.states.SCOUT;
                break;
        }

        // Communicate High Value Info

        // Update Low Value Map Info
        myLocation = rc.getLocation();
        if (RobotPlayer.scannedMapInfos == null || (myLocation != RobotPlayer.lastLocationScannedMapInfos)) {
            RobotPlayer.scannedMapInfos = rc.senseNearbyMapInfos();
            RobotPlayer.lastLocationScannedMapInfos = myLocation;
        }

        // Communicate Low Value Map Info

    }

}
