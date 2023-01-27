package latest;

import battlecode.common.*;

import java.util.*;

public class CarrierStrategy {
    static MapLocation hqLoc;
    static MapLocation wellLoc;
    static boolean anchorMode = false;
    static int amountResourcesHeld = 0;

    public static void run(RobotController rc) throws GameActionException {
        if(hqLoc == null) {
            searchForHq(rc);
        }
        else if (Sensing.scanRelativeCombatStrength(rc) < 0){
            // Enemy combatants outnumber visible friendlies
            fightIfConvenient(rc);
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
            searchForWell(rc);
            Pathing.moveRandom(rc);
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

        Pathing.trackedMove(rc, retreatDirection);
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
        int[] islands = rc.senseNearbyIslands();
        Set<MapLocation> islandLocs = new HashSet<>();
        for (int id : islands) {
            MapLocation[] thisIslandLocs = rc.senseNearbyIslandLocations(id);
            islandLocs.addAll(Arrays.asList(thisIslandLocs));
        }
        if (islandLocs.size() > 0) {
            MapLocation islandLocation = islandLocs.iterator().next();
            if(!rc.getLocation().equals(islandLocation)) {
                Pathing.moveWithBugNav(rc, islandLocation);
            }
            if (rc.canPlaceAnchor()) {
                rc.setIndicatorString("Huzzah, placed anchor!");
                rc.placeAnchor();
                anchorMode = false;
            }
        }
        else {
            Pathing.moveRandom(rc);
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
