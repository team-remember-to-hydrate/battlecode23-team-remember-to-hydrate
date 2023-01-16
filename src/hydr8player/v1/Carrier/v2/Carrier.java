package hydr8player.v1.Carrier.v2;

import battlecode.common.*;
import java.util.Random;

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

    public void collectResources(RobotController rc, MapLocation wellLoc) throws GameActionException {
        int totalCarrying = this.getTotalCarrying(rc);
        if(totalCarrying < MAX_RESOURCE_CAPACITY && rc.getAnchor() == null) {
            if (rc.canCollectResource(wellLoc, -1)) {
                rc.collectResource(wellLoc, -1);
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
}
