package latest;

import battlecode.common.*;

import java.util.*;

public class CarrierStrategy {
    static MapLocation hqLoc;
    static ResourceType targetResourceType;
    static MapLocation wellLoc;
    static boolean anchorMode = false;
    static int amountResourcesHeld = 0;
    static MapLocation currentTargetIslandLocation = null;

    public static void run(RobotController rc) throws GameActionException {
        if(hqLoc == null) {
            // Complete First Turn Actions
            searchForHq(rc);
            targetResourceType = ResourceType.values()[(rc.getID() % 2) + 1];
        }

        // Every turn do this:
        // Read Comms:
        RobotPlayer.process_array_islands(rc);
        RobotPlayer.process_array_wells(rc);


        if (Sensing.scanRelativeCombatStrength(rc) < 0){
            // Enemy combatants outnumber visible friendlies
            //fightIfConvenient(rc);
        }
        else if (rc.getHealth() < RobotPlayer.myHealthLastTurn){
            // We have been hit!
            fightBackAndRun(rc);
        }
        else if(rc.canTakeAnchor(hqLoc, Anchor.ACCELERATING) || rc.canTakeAnchor(hqLoc, Anchor.STANDARD)) {
            tryPickUpAnchor(rc, hqLoc);
        }
        else if(anchorMode){
            deliverAnchor(rc);
        }
        else if(wellLoc == null){
            //searchForWell(rc);
            searchForWellOfType(rc, targetResourceType);
            int movesTaken = 0;
            while (movesTaken < 3) {
                Pathing.trackedMove(rc, Pathing.getRotateValidMove(rc, RobotPlayer.lastMoved, RobotPlayer.prefersClockwise));
                movesTaken++;
            }
        }
        else if(amountResourcesHeld < GameConstants.CARRIER_CAPACITY){
            if(rc.getLocation().distanceSquaredTo(wellLoc) <= 2){
                tryCollectResources(rc, wellLoc);
            }
            else {
                Pathing.moveWithBugNav(rc, wellLoc);
            }
        }
        else if(amountResourcesHeld == GameConstants.CARRIER_CAPACITY){
            Pathing.moveWithBugNav(rc, hqLoc);
            tryDropAllResources(rc, hqLoc);
        }
        else {
            throw new GameActionException(GameActionExceptionType.INTERNAL_ERROR, "Carrier internal error");
        }
    }

    private static void fightIfConvenient(RobotController rc) throws GameActionException {
        // If I have resources, consider fighting.
        if (amountResourcesHeld > 3){
            // Find weakest hostile enemy in range, or nearest econ enemy in range
            ArrayList<RobotInfo> nearbyEnemies = Sensing.scanCombatUnitsOfTeamInRange(rc, rc.getTeam().opponent(), 9);
            // Carrier action radius not in game constants
            if (nearbyEnemies.size() < 1) {
                // Pick an enemy carrier in range
                nearbyEnemies = Sensing.scanAnyUnitsOfTeamInRange(rc, rc.getTeam().opponent(), 9);
            }

            // Pick weakest target
            RobotInfo targetBot = Sensing.scanWeakestBotInGroup(rc, nearbyEnemies);
            // Hit them
            if (rc.canAttack(targetBot.getLocation())){
                rc.attack(rc.getLocation());
                amountResourcesHeld = getTotalCarrying(rc);
            }
        }
    }

    private static void fightBackAndRun(RobotController rc) throws GameActionException {
        // If I have resources, consider fighting.
        // Find weakest hostile enemy in range, or nearest econ enemy in range
        ArrayList<RobotInfo> nearbyEnemies = Sensing.scanCombatUnitsOfTeamInRange(rc, rc.getTeam().opponent(), 9);
        // Carrier action radius not in game constants
        if (nearbyEnemies.size() < 1) {
            // Pick an enemy carrier in range
            nearbyEnemies = Sensing.scanAnyUnitsOfTeamInRange(rc, rc.getTeam().opponent(), 9);
        }

        // Pick weakest target
        RobotInfo targetBot = Sensing.scanWeakestBotInGroup(rc, nearbyEnemies);
        // Hit them
        if (targetBot != null && rc.canAttack(targetBot.getLocation())){
            rc.attack(rc.getLocation());
            amountResourcesHeld = getTotalCarrying(rc);
        }

        Direction retreatDirection = rc.getLocation().directionTo(hqLoc);

        if (targetBot != null){
            Pathing.getClosestValidMoveDirection(rc, rc.getLocation().directionTo(targetBot.getLocation()).opposite());
        }

        int movesTaken = 0;
        while (movesTaken < 3){
            Pathing.trackedMove(rc, retreatDirection);
            movesTaken++;
        }
        // TODO Switch to return to HQ mode
    }

    static void searchForHq(RobotController rc) throws GameActionException {
        RobotInfo[] bots = rc.senseNearbyRobots(2);
        for (RobotInfo bot : bots) {
            if (bot.getType() == RobotType.HEADQUARTERS) {
                hqLoc = bot.getLocation();
            }
        }
    }
    static void searchForWell(RobotController rc) {
        if(wellLoc == null) {
            WellInfo[] wells = rc.senseNearbyWells();
            if((wells != null) && (wells.length > 0)) {
                wellLoc = wells[0].getMapLocation();
            }
        }
    }

    static void searchForWellOfType(RobotController rc, ResourceType type) {
        if(wellLoc == null) {
            WellInfo[] wells = rc.senseNearbyWells();
            if((wells != null)) {
                for (int i = 0; i < wells.length; i++){
                    if (wells[i].getResourceType().equals(type)){
                        wellLoc = wells[i].getMapLocation();
                    }
                }
            }
        }
    }

    static void tryPickUpAnchor(RobotController rc, MapLocation loc) throws GameActionException {
        if(rc.canTakeAnchor(loc, Anchor.ACCELERATING)){
            rc.takeAnchor(loc, Anchor.ACCELERATING);
            anchorMode = true;
        }
        else if(rc.canTakeAnchor(loc, Anchor.STANDARD)){
            rc.takeAnchor(loc, Anchor.STANDARD);
            anchorMode = true;
        }
    }
    static void deliverAnchor(RobotController rc) throws GameActionException {
        rc.setIndicatorString("deliverAnchor");
        Set<MapLocation> islandLocs = new HashSet<>();

        // Add in Comms islands
        for (int i = 0; i < RobotPlayer.island_ids.size(); i++){
            int tempID = RobotPlayer.island_ids.get(i);
            if (tempID != 0){
                MapLocation tempLocation = RobotPlayer.island_locations[tempID];
                if (!tempLocation.equals(null)){
                    currentTargetIslandLocation = tempLocation;
                    break;
                }
            }
        }

        if (currentTargetIslandLocation == null || !rc.canSenseLocation(currentTargetIslandLocation)) {
            currentTargetIslandLocation = null;
            int[] islands = rc.senseNearbyIslands();

        // End last minute anchor island edits


            for (int id : islands) {
                // If we own it, skip it unless we can upgrade it.
                if (rc.senseTeamOccupyingIsland(id).equals(rc.getTeam())
                    && !(rc.getAnchor().equals(Anchor.ACCELERATING) && rc.senseAnchor(id).equals(Anchor.STANDARD))) {
                    // It is an upgrade if we are holding an Accelerating and island has a Standard anchor.
                    continue;
                }
                MapLocation[] thisIslandLocs = rc.senseNearbyIslandLocations(id);
                // Get closest island location
                currentTargetIslandLocation = thisIslandLocs[0];
                int shortestDistance = rc.getLocation().distanceSquaredTo(currentTargetIslandLocation);
                MapLocation myLocation = rc.getLocation();
                for (int i = 1; i < thisIslandLocs.length; i++){
                    int possibleDistance = myLocation.distanceSquaredTo(thisIslandLocs[i]);
                    if (possibleDistance < shortestDistance) {
                        currentTargetIslandLocation = thisIslandLocs[i];
                        shortestDistance = possibleDistance;
                    }
                }
            }
        }

        int movesTaken = 0;
        if (currentTargetIslandLocation != null) {
            while (!rc.getLocation().equals(currentTargetIslandLocation) && movesTaken < 3) {
                Pathing.moveWithBugNav(rc, currentTargetIslandLocation);
                movesTaken++;
            }
            if (rc.canPlaceAnchor()
                    && !rc.senseTeamOccupyingIsland(rc.senseIsland(currentTargetIslandLocation)).equals(rc.getTeam())) { //prevents accel anchors though TODO
                rc.setIndicatorString("Huzzah, placed anchor!");
                rc.placeAnchor();
                anchorMode = false;
            }
            else if (rc.getLocation().equals(currentTargetIslandLocation)) {
                currentTargetIslandLocation = null;
            }
        }
        else {
            while (!rc.getLocation().equals(currentTargetIslandLocation) && movesTaken < 3){
                Pathing.trackedMove(rc, Pathing.getRotateValidMove(rc, RobotPlayer.lastMoved, RobotPlayer.prefersClockwise));
                movesTaken++;
            }
        }
    }
    static void tryCollectResources(RobotController rc, MapLocation loc) throws GameActionException {
        int totalCarrying = getTotalCarrying(rc);
        if(totalCarrying < GameConstants.CARRIER_CAPACITY && rc.getAnchor() == null) {
            if (rc.canCollectResource(loc, -1)) {
                rc.collectResource(loc, -1);
                amountResourcesHeld = getTotalCarrying(rc);
            }
        }
    }
    static int getTotalCarrying(RobotController rc) {
        return rc.getResourceAmount(ResourceType.ADAMANTIUM) +
                rc.getResourceAmount(ResourceType.MANA) +
                rc.getResourceAmount(ResourceType.ELIXIR);
    }
    static void tryDropAllResources(RobotController rc, MapLocation hqLoc) throws GameActionException {
        tryDropResource(rc, ResourceType.ADAMANTIUM, hqLoc);
        tryDropResource(rc, ResourceType.MANA, hqLoc);
        tryDropResource(rc, ResourceType.ELIXIR, hqLoc);
    }
    static void tryDropResource(RobotController rc, ResourceType rt, MapLocation loc) throws GameActionException {
        int total = rc.getResourceAmount(rt);
        if(rc.canTransferResource(loc, rt, rc.getResourceAmount(rt))){
            rc.transferResource(loc, rt, total);
            amountResourcesHeld = getTotalCarrying(rc);
        }
    }
}
