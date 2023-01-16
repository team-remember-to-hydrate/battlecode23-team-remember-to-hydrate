package sprint_1.CarrierV2;

import battlecode.common.*;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

public class Carrier {
    public static final Direction[] directions = {
            Direction.NORTH,
            Direction.NORTHEAST,
            Direction.EAST,
            Direction.SOUTHEAST,
            Direction.SOUTH,
            Direction.SOUTHWEST,
            Direction.WEST,
            Direction.NORTHWEST
    };
    public static final int MAX_RESOURCE_CAPACITY = GameConstants.CARRIER_CAPACITY;
    public MapLocation wellLoc;
    public int amountResourcesHeld = 0;
    public MapLocation hqLoc;
    public boolean hasAnchor;

    public void searchForWell(RobotController rc) {
        if(this.wellLoc == null) {
            WellInfo[] wells = rc.senseNearbyWells();
            if((wells != null) && (wells.length > 0)) {
                this.wellLoc = wells[0].getMapLocation();
            }
        }
    }

    static Random rng = new Random(6147);
    public void moveRandom(RobotController rc) throws GameActionException {
        Direction dir = directions[rng.nextInt(directions.length)];
        if (rc.canMove(dir)) {
            rc.move(dir);
        }
    }

    static Direction currentDirection = null;
    public void moveWithBugNav(RobotController rc, MapLocation targetLoc) throws GameActionException {
        if (rc.getLocation().equals(targetLoc)){
            return;
        }
        if (!rc.isActionReady()){
            return;
        }
        Direction d = rc.getLocation().directionTo(targetLoc);
        if (rc.canMove(d) && d != Direction.CENTER) {
            rc.move(d);
            currentDirection = null; // no obstacle
        }
        else {
            // Going around obstacle; cannot move towards d because of obstacle
            if (currentDirection == null){
                currentDirection = d;
            }
            // try to move in a way that keeps obstacle on our right
            for(int i = 0; i < 8; i++){
                if (rc.canMove(currentDirection)) {
                    rc.move(currentDirection);
                    break;
                }
                else {
                    currentDirection = currentDirection.rotateRight();
                }
            }
        }
    }

    public void tryCollectResources(RobotController rc, MapLocation loc) throws GameActionException {
        int totalCarrying = this.getTotalCarrying(rc);
        if(totalCarrying < MAX_RESOURCE_CAPACITY && rc.getAnchor() == null) {
            if (rc.canCollectResource(loc, -1)) {
                rc.collectResource(loc, -1);
                this.amountResourcesHeld = this.getTotalCarrying(rc);
            }
        }
    }

    private int getTotalCarrying(RobotController rc) throws GameActionException {
        return rc.getResourceAmount(ResourceType.ADAMANTIUM) +
                rc.getResourceAmount(ResourceType.MANA) +
                rc.getResourceAmount(ResourceType.ELIXIR);
    }

    private void tryDropAllOfResourceToHq(RobotController rc, ResourceType rt, MapLocation loc) throws GameActionException {
        int total = rc.getResourceAmount(rt);
        if(rc.canTransferResource(loc, rt, rc.getResourceAmount(rt))){
            rc.transferResource(loc, rt, total);
            this.amountResourcesHeld = this.getTotalCarrying(rc);
        }
    }

    public void tryTransferAllResources(RobotController rc, MapLocation hqLoc) throws GameActionException {
        tryDropAllOfResourceToHq(rc, ResourceType.ADAMANTIUM, hqLoc);
        tryDropAllOfResourceToHq(rc, ResourceType.MANA, hqLoc);
        tryDropAllOfResourceToHq(rc, ResourceType.ELIXIR, hqLoc);
    }

    public void tryPickUpAnchor(RobotController rc, MapLocation loc) throws GameActionException {
        if(rc.canTakeAnchor(loc, Anchor.ACCELERATING)){
            rc.takeAnchor(loc, Anchor.ACCELERATING);
            this.hasAnchor = true;
        }
        else if(rc.canTakeAnchor(loc, Anchor.STANDARD)){
            rc.takeAnchor(loc, Anchor.STANDARD);
            this.hasAnchor = true;
        }
    }

    public void searchForHq(RobotController rc) throws GameActionException {
        RobotInfo[] bots = rc.senseNearbyRobots(2);
        for (RobotInfo bot : bots) {
            if (bot.getType() == RobotType.HEADQUARTERS) {
                this.hqLoc = bot.getLocation();
            }
        }
    }

    public void deliverAnchor(RobotController rc) throws GameActionException {
        // If I have an anchor singularly focus on getting it to the first island I see
        int[] islands = rc.senseNearbyIslands();
        Set<MapLocation> islandLocs = new HashSet<>();
        for (int id : islands) {
            MapLocation[] thisIslandLocs = rc.senseNearbyIslandLocations(id);
            islandLocs.addAll(Arrays.asList(thisIslandLocs));
        }
        if (islandLocs.size() > 0) {
            MapLocation islandLocation = islandLocs.iterator().next();
            rc.setIndicatorString("Moving my anchor towards " + islandLocation);
            while (!rc.getLocation().equals(islandLocation)) {
                Direction dir = rc.getLocation().directionTo(islandLocation);
                if (rc.canMove(dir)) {
                    rc.move(dir);
                }
            }
            if (rc.canPlaceAnchor()) {
                rc.setIndicatorString("Huzzah, placed anchor!");
                rc.placeAnchor();
                this.hasAnchor = false;
            }
        }
    }
}
