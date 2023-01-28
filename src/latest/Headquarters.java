package latest;

import battlecode.common.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;

import static latest.RobotPlayer.*;

public class Headquarters {
    static int my_array_address;
    static int amplifiers_built = 0;

    static WellInfo[] wells;
    static HashSet<MapLocation> validBuildLocations = new HashSet<>(40);
    static RobotPlayer.hq_states current_state;
    static MapLocation next_island;
    /**
     * Run a single turn for a Headquarters.
     * This code is wrapped inside the infinite loop in run(), so it is called once per turn.
     */
    static void runHeadquarters(RobotController rc) throws GameActionException {
        // generate info to make decisions
        RobotInfo[] nearby_bots = rc.senseNearbyRobots();
        RobotInfo[] nearby_enemies = rc.senseNearbyRobots(-1, rc.getTeam().opponent());
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

            // populate valid build locations
            populateValidAccessibleBuildLocations(rc);
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
            int mission = RobotPlayer.states.ATTACK.ordinal();
            int target_array = RobotPlayer.packMapLocationExtra(target,mission);
            rc.writeSharedArray(my_task_array_location,target_array);
            int packed_info = RobotPlayer.packMapLocationExtra(rc.getLocation(), RobotPlayer.hq_states.TASK.ordinal());
            rc.writeSharedArray(my_array_address,packed_info);
        }

//        // if we are holding an anchor we saw an island, lets build a carrier.
//        // Pick a direction to build in.
//        Direction dir = RobotPlayer.directions[RobotPlayer.rng.nextInt(RobotPlayer.directions.length)];
//        MapLocation newLoc = rc.getLocation().add(dir);
//        if(rc.getNumAnchors(Anchor.STANDARD) > 0){
//            // Let's try to build a carrier.
//            rc.setIndicatorString("Trying to build a carrier");
//            if (rc.canBuildRobot(RobotType.CARRIER, newLoc)) {
//                rc.buildRobot(RobotType.CARRIER, newLoc);
//            }
//        }

        // Count enemy launchers in launcher attack distance,  carriers, to decide to build a carrier
        RobotInfo[] enemyInLauncherRange = rc.senseNearbyRobots(16, rc.getTeam().opponent());
        RobotInfo[] friendlyInSight = rc.senseNearbyRobots(-1, rc.getTeam());

        int enemyLauncherCount = 0;
        for (int i = 0; i < enemyInLauncherRange.length; i++) {
            if (enemyInLauncherRange[0].getType() == RobotType.LAUNCHER){
                enemyLauncherCount++;
            }
        }
        int carrierCount = 0;
        for (int i = 0; i < friendlyInSight.length; i++) {
            if (friendlyInSight[0].getType() == RobotType.CARRIER){
                carrierCount++;
            }
        }
        // If we see no enemy launchers in launcher attack distance, and see less than 6 carriers, build a carrier
        if ((enemyLauncherCount == 0) && (carrierCount < 6)){
            Direction dir = getBuildDirection(rc, wells);
            MapLocation newLoc = rc.getLocation().add(dir);
            //rc.setIndicatorString("Trying to build a carrier");
            if (rc.canBuildRobot(RobotType.CARRIER, newLoc)) {
                rc.buildRobot(RobotType.CARRIER, newLoc);
                carrierCount++; //TODO: Carrier mass spawn up to 6 visible
            }
        }

        if (num_launchers < 6) {
            Direction dir = directions[rng.nextInt(directions.length)];
            MapLocation newLoc = rc.getLocation().add(dir);

            // Let's try to build a launcher.
            //rc.setIndicatorString("Trying to build a launcher");
            if (rc.canBuildRobot(RobotType.LAUNCHER, newLoc)) {
                rc.buildRobot(RobotType.LAUNCHER, newLoc);
            }
        }

        // build an amplifier if nothing else to do
        boolean buildAmplifiers = true;
        if (buildAmplifiers) {
            Direction dir = RobotPlayer.directions[RobotPlayer.rng.nextInt(RobotPlayer.directions.length)];
            MapLocation newLoc = rc.getLocation().add(dir);

            // Let's try to build a launcher.
            rc.setIndicatorString("Trying to build an amplifier");
            if (rc.getRoundNum() / 100 > amplifiers_built && rc.canBuildRobot(RobotType.AMPLIFIER, newLoc)) {
                rc.buildRobot(RobotType.AMPLIFIER, newLoc);
                amplifiers_built++;
            }
        }
    }
    static Direction getBuildDirection(RobotController rc, WellInfo[] wells) throws GameActionException {
        Direction dir = directions[rng.nextInt(directions.length)];
        if(wells.length > 0){
            dir = rc.getLocation().directionTo(wells[0].getMapLocation());
        }
        return dir;
    }

    /**
     * Updates Headquarters.validBuildLocations to contain list of proven accessible spawn locations from HQ.
     * Uses ~7300 bytecode
     * @param rc
     * @throws GameActionException
     */
    static void populateValidAccessibleBuildLocations(RobotController rc) throws GameActionException {
//        System.out.println("Start popValidBuild: " + Clock.getBytecodesLeft());
        // get list of all visible spaces
        MapLocation[] close = rc.getAllLocationsWithinRadiusSquared(rc.getLocation(), 2); // 9 is HQ action range, not in GAMECONSTANTS
        // check if space is impassable or a HQ or unknown (use scanMapTileType)
        MapLocation testLocation;
        for (int i = 0; i < close.length; i++){
            testLocation = close[i];
            if (rc.sensePassability(testLocation)
                    && (!rc.canSenseRobotAtLocation(testLocation)
                    || !rc.senseRobotAtLocation(testLocation).getType().equals(RobotType.HEADQUARTERS))) {
                validBuildLocations.add(testLocation);
            }
        }

        // Now check a level out from close locations
//        System.out.println("Start make array: " + Clock.getBytecodesLeft());
//        MapLocation[] closeLocations = validBuildLocations.toArray(new MapLocation[validBuildLocations.size()]);
//        System.out.println("Stop make array and start iterator: " + Clock.getBytecodesLeft());
        Iterator<MapLocation> i = validBuildLocations.iterator();
//        System.out.println("Stop make iterator, do setup: " + Clock.getBytecodesLeft());
        ArrayList<MapLocation> level2Locations = new ArrayList<MapLocation>(16);
        MapLocation level1Loc;
        Direction outwards;
        MapLocation test1;
        MapLocation test2;
//        MapLocation test3;
//        System.out.println("Stop setup, start layer 2 loop: " + Clock.getBytecodesLeft());
        //System.out.println("Set contains: " + validBuildLocations);

        while (i.hasNext()){
            level1Loc = i.next();
            outwards = level1Loc.directionTo(rc.getLocation()).opposite();
            test1 = level1Loc.add(outwards);
            test2 = level1Loc.add(outwards.rotateLeft());
//            test3 = level1Loc.add(outwards.rotateRight());
            if (rc.onTheMap(test1)
                    && rc.sensePassability(test1)
                    && (!rc.canSenseRobotAtLocation(test1)
                    // || !rc.senseRobotAtLocation(test1).getType().equals(RobotType.HEADQUARTERS)
            )) {
                level2Locations.add(test1);
            }
            if (rc.onTheMap(test2)
                    && rc.sensePassability(test2)
                    && (!rc.canSenseRobotAtLocation(test2)
//                    || !rc.senseRobotAtLocation(test2).getType().equals(RobotType.HEADQUARTERS)
            )) {
                level2Locations.add(test2);
            }
//            if (rc.sensePassability(test3)
//                    && (!rc.canSenseRobotAtLocation(test3)
////                    || !rc.senseRobotAtLocation(test3).getType().equals(RobotType.HEADQUARTERS)
//            )) {
//                level2Locations.add(test3);
//            }
        }
//        System.out.println("stop layer 2 loop, start add layer2: " + Clock.getBytecodesLeft());
        //System.out.println("Set contains: " + validBuildLocations);
        //System.out.println("Set 2 contains: " + level2Locations);

        // Add layer2 to hashset
        for (int j = 0; j < level2Locations.size(); j++){
            validBuildLocations.add(level2Locations.get(j));
        }
//        System.out.println("stop add layer 2, start add final 4: " + Clock.getBytecodesLeft());

        // Add last 4 spaces if they are valid
        MapLocation startLocation = rc.getLocation();
        MapLocation farLocation = startLocation.translate(-3,0);
        if (rc.onTheMap(farLocation)
                && rc.sensePassability(farLocation)
                && !rc.canSenseRobotAtLocation(farLocation)
                && validBuildLocations.contains(farLocation.add(farLocation.directionTo(startLocation)))) {
            validBuildLocations.add(farLocation);
        }
        farLocation = startLocation.translate(3,0);
        if (rc.onTheMap(farLocation)
                && rc.sensePassability(farLocation)
                && !rc.canSenseRobotAtLocation(farLocation)
                && validBuildLocations.contains(farLocation.add(farLocation.directionTo(startLocation)))) {
            validBuildLocations.add(farLocation);
        }
        farLocation = startLocation.translate(0,3);
        if (rc.onTheMap(farLocation)
                && rc.sensePassability(farLocation)
                && !rc.canSenseRobotAtLocation(farLocation)
                && validBuildLocations.contains(farLocation.add(farLocation.directionTo(startLocation)))) {
            validBuildLocations.add(farLocation);
        }
        farLocation = startLocation.translate(0,-3);
        if (rc.onTheMap(farLocation)
                && rc.sensePassability(farLocation)
                && !rc.canSenseRobotAtLocation(farLocation)
                && validBuildLocations.contains(farLocation.add(farLocation.directionTo(startLocation)))) {
            validBuildLocations.add(farLocation);
        }

        System.out.println("Stop popValidBuild: " + Clock.getBytecodesLeft());
        System.out.println("Valid Spawns: " + validBuildLocations);
    }
}
