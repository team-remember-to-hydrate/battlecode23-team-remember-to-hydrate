package anchor_decay;

import battlecode.common.*;
import anchor_decay.Pathing;
import anchor_decay.RobotPlayer;
import anchor_decay.Sensing;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Amplifier {

    final Direction[] directions = anchor_decay.RobotPlayer.directions;
    final Random rng = anchor_decay.RobotPlayer.rng;
    static anchor_decay.RobotPlayer.states my_state = anchor_decay.RobotPlayer.my_state;
    static int my_HQ = 99;
    static MapLocation my_HQ_location;
    static MapLocation[] island_locations = anchor_decay.RobotPlayer.island_locations;
    static MapLocation target_location;
    static String indicator_string = "";
    static MapLocation myLocation;
    static MapLocation map_center;
    static List<Integer> island_ids = new ArrayList<>();


    static void runAmplifier(RobotController rc) throws GameActionException {


        // This runs on my first turn only
        if(anchor_decay.RobotPlayer.turnCount <= 1){
            my_HQ = anchor_decay.RobotPlayer.get_HQ_array_index(rc);
            my_HQ_location = anchor_decay.RobotPlayer.unpackMapLocation(rc.readSharedArray(my_HQ));
            map_center = new MapLocation(rc.getMapWidth() / 2, rc.getMapHeight() / 2);
        }

        // Each turn starts with these tasks before dealing with primary task:

        // Read Comms and update worldview
        // get islands from array
      /*  List<Integer> island_indexes = Comms.get_array_islands(rc);

        for(int island : island_indexes){
            int this_island_id;
            MapLocation this_island_location = null;
            int islandDetails = 0;
            if (Comms.is_location(island)){
                islandDetails = rc.readSharedArray(island + 1);
                this_island_id = Comms.get_island_id(islandDetails);
                this_island_location = Comms.get_MapLocation(rc.readSharedArray(island));
            }
            else {
                islandDetails = rc.readSharedArray(island);
                this_island_id = Comms.get_island_id(islandDetails);
            }

            // remove them from array if we already know about them
            //System.out.println("array location " + island + " id " + this_island_id + " location " + this_island_location + " raw data " + rc.readSharedArray(island) + " " + rc.readSharedArray(island + 1));
            if(island_ids.contains(this_island_id)){
                RobotPlayer.teamKnownIslandDetails[this_island_id] = islandDetails;
            }else{
                //This is a new map id, lets store it, and it's location
                island_ids.add(this_island_id);
                island_locations[this_island_id] = this_island_location;
                RobotPlayer.teamKnownIslandDetails[this_island_id] = islandDetails;
            }
        }*/

        //    ***   Check Comms for island updates   ***
        // get islands from array
        anchor_decay.RobotPlayer.process_array_islands(rc);

        // Update High Value Map Info
        Sensing.scanAndUpdateIslands(rc); // very high bytecode cost

        // Scan wells

        // Read Orders and change state and goals accordingly

        // Sense nearby enemies, possibly respond accordingly




        switch(my_state){
            case INITIAL:
                my_state = anchor_decay.RobotPlayer.states.SCOUT;
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
                    Direction dir = anchor_decay.Pathing.getRotateValidMove(rc, anchor_decay.RobotPlayer.lastMoved, anchor_decay.RobotPlayer.prefersClockwise);
                    Pathing.trackedMove(rc, dir);
                }

                break;

            case DEFAULT:
                my_state = anchor_decay.RobotPlayer.states.SCOUT;
                break;
        }

        // Communicate High Value Info

        // Update Low Value Map Info
        myLocation = rc.getLocation();
        if (anchor_decay.RobotPlayer.scannedMapInfos == null || (myLocation != anchor_decay.RobotPlayer.lastLocationScannedMapInfos)) {
            anchor_decay.RobotPlayer.scannedMapInfos = rc.senseNearbyMapInfos();
            RobotPlayer.lastLocationScannedMapInfos = myLocation;
        }

        // Communicate Low Value Map Info

    }

}
