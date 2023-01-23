package latest;

import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;

import java.util.Random;

public class Amplifier {

    final Direction[] directions = RobotPlayer.directions;
    final Random rng = RobotPlayer.rng;
    static RobotPlayer.states my_state;
    static int my_HQ = 99;
    static MapLocation my_HQ_location;

    static MapLocation target_location;
    static String indicator_string = "";
    static MapLocation myLocation;
    static MapLocation map_center;

    static void runAmplifier(RobotController rc) throws GameActionException {


        // This runs on my first turn only
        if(my_HQ > 3){
            my_HQ = RobotPlayer.get_HQ_array_index(rc);
            my_HQ_location = RobotPlayer.unpackMapLocation(rc.readSharedArray(my_HQ));
            my_state = RobotPlayer.states.INITIAL;
            map_center = new MapLocation(rc.getMapWidth() / 2, rc.getMapHeight() / 2);
        }

        // Each turn starts with these tasks before dealing with primary task:

        // Read Comms and update worldview


        // Update High Value Map Info
        myLocation = rc.getLocation();
        if (RobotPlayer.scannedMapInfos == null || (myLocation != RobotPlayer.myLastLocation)) {
            RobotPlayer.scannedMapInfos = rc.senseNearbyMapInfos();
        }
        // Read Orders and change state and goals accordingly

        // Sense nearby enemies, possibly respond accordingly




        switch(my_state){
            case SCOUT:
                // Refresh MapInfo
            case DEFAULT:
                my_state = RobotPlayer.states.SCOUT;
        }

        // Update Low Value Map Info
        myLocation = rc.getLocation();
        if (RobotPlayer.scannedMapInfos == null || (myLocation != RobotPlayer.myLastLocation)) {
            RobotPlayer.scannedMapInfos = rc.senseNearbyMapInfos();
        }


        // Try to move with clockwise momentum
        Direction dir = Pathing.getRotateValidMove(rc, RobotPlayer.lastMoved, true);
        //Pathing.trackedMove(rc, dir);
        if (rc.canMove(dir)) {
            rc.move(dir);
            RobotPlayer.lastMoved = dir;
        }


    }
}