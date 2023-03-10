package josh_001;

import battlecode.common.*;

import java.util.Map;

public class Headquarters {
    static int my_array_address;

    static WellInfo[] wells;
    static RobotPlayer.hq_states current_state;
    static MapLocation next_island;
    /**
     * Run a single turn for a Headquarters.
     * This code is wrapped inside the infinite loop in run(), so it is called once per turn.
     */
    static void runHeadquarters(RobotController rc) throws GameActionException {
        // generate info to make decisions
        RobotInfo[] nearby_bots = rc.senseNearbyRobots();
        Team us = rc.getTeam();
        int num_launchers = 0;

        // first round save our location to the array in the first available spot. 0-3
        // also track visible wells since HQ don't move
        if(rc.getRoundNum() == 1) {
            // use first available shared array slot
            for (int i = 0; i < 4; i++) {
                int array_int = rc.readSharedArray(i);
                if (array_int == 0) {
                    my_array_address = i;
                    rc.writeSharedArray(i, RobotPlayer.packMapLocationExtra(rc.getLocation(), 0));
                    // System.out.println(rc.getLocation());
                    // System.out.println(RobotPlayer.packMapLocationExtra(rc.getLocation(), 0));
                    break;
                }
            }

            // sense visible wells mark them on array, change state to RESOURCE
            wells = rc.senseNearbyWells();
            if(wells.length > 0){
                current_state = RobotPlayer.hq_states.RESOURCE;
                for(WellInfo well : wells){
                    for(int i = 4; i < 12; i++){
                        int array_int = rc.readSharedArray(i);
                        if(array_int == 0){
                            rc.writeSharedArray(i, RobotPlayer.packMapLocationExtra(well.getMapLocation(), well.getResourceType().ordinal()));
                            break;
                        }
                    }
                }
            }
            // still in the code for the first round only
            // look for islands build anchor is there is at least one
            int[] island_indexes = rc.senseNearbyIslands();
            for(int island_index : island_indexes){
                MapLocation[] island_locations = rc.senseNearbyIslandLocations(island_index);
                if(rc.canBuildAnchor(Anchor.STANDARD)){
                    rc.buildAnchor(Anchor.STANDARD);
                }
            }
        }

        //round 10 print the array to console
        if(rc.getRoundNum() == 10){
            MapLocation mine = RobotPlayer.unpackMapLocation(rc.readSharedArray(my_array_address));
            System.out.println("advertising my location" + my_array_address + " as " + mine + " from " + rc.readSharedArray(my_array_address));
            if(my_array_address == 0){
                for(int i = 4; i < 12; i++){
                    int this_well = rc.readSharedArray(i);
                    if(this_well != 0){
                        System.out.println("Well at " + RobotPlayer.unpackMapLocation(this_well) + " of type " + RobotPlayer.unpackResource(this_well));
                    }
                }
            }
        }

        // if there are 6 launchers send them on a raid to spot.
        for(RobotInfo bot : nearby_bots){
            if(bot.getTeam().equals(us) & bot.getType().equals(RobotType.LAUNCHER)){
                num_launchers++;
            }
        }
        // Minimum Viable Product for group attack
        if(num_launchers >= 6){
            int my_task_array_location = 12 + my_array_address;
            MapLocation target = new MapLocation(rc.getMapWidth() / 2,rc.getMapHeight()/2);
            int mission = 0;
            int target_array = RobotPlayer.packMapLocationExtra(target,mission);
            rc.writeSharedArray(my_task_array_location,target_array);
            int packed_info = RobotPlayer.packMapLocationExtra(rc.getLocation(),RobotPlayer.hq_states.TASK.ordinal());
            rc.writeSharedArray(my_array_address,packed_info);
        }

        // if we are holding an anchor we saw an island, lets build a carrier.
        // Pick a direction to build in.
        Direction dir = RobotPlayer.directions[RobotPlayer.rng.nextInt(RobotPlayer.directions.length)];
        MapLocation newLoc = rc.getLocation().add(dir);
        if(rc.getNumAnchors(Anchor.STANDARD) > 0){
            // Let's try to build a carrier.
            rc.setIndicatorString("Trying to build a carrier");
            if (rc.canBuildRobot(RobotType.CARRIER, newLoc)) {
                rc.buildRobot(RobotType.CARRIER, newLoc);
            }
        }



        if (num_launchers < 6) {


            // Let's try to build a launcher.
            rc.setIndicatorString("Trying to build a launcher");
            if (rc.canBuildRobot(RobotType.LAUNCHER, newLoc)) {
                rc.buildRobot(RobotType.LAUNCHER, newLoc);
            }
        }
    }
}
