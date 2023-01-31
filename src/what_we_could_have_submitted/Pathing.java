package what_we_could_have_submitted;

import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import what_we_could_have_submitted.RobotPlayer;
import what_we_could_have_submitted.Sensing;

import static what_we_could_have_submitted.RobotPlayer.*;

public class Pathing {

    static int bugNavTurnsStalled = 0;
    public static void moveRandom(RobotController rc) throws GameActionException {
        Direction dir = directions[rng.nextInt(directions.length)];
        if (rc.canMove(dir)) {
            rc.move(dir);
        }
    }
    static Direction currentDirection = null;
    public static void moveWithBugNav(RobotController rc, MapLocation targetLoc) throws GameActionException {
        if (rc.getLocation().equals(targetLoc)){
            return;
        }
        if (!rc.isMovementReady()){
            return;
        }
        Direction d = rc.getLocation().directionTo(targetLoc);
        if (rc.canMove(d) && d != Direction.CENTER) {
            bugNavTurnsStalled = 0;
            trackedMove(rc, d);
            currentDirection = null; // no obstacle
        }
        else {
            // If bot is blocking, wait a turn
            if (rc.canSenseRobotAtLocation(rc.getLocation().add(d)) && bugNavTurnsStalled < 1){
                bugNavTurnsStalled += 1;
                return;
            }
            // Going around obstacle; cannot move towards d because of obstacle
            if (currentDirection == null){
                currentDirection = d;
            }

            // try to move in a way that keeps obstacle to one side
            Direction smartDir = Pathing.getSmartClosestValidMoveDirection(rc, currentDirection);
            if (rc.canMove(smartDir)){
                trackedMove(rc, smartDir);
            }
//            for(int i = 0; i < 8; i++){
//                if (rc.canMove(currentDirection)) {
//                    trackedMove(rc, currentDirection);
//                    if (prefersClockwise){
//                        currentDirection = currentDirection.rotateRight();
//                    }
//                    else {
//                        currentDirection = currentDirection.rotateLeft();
//                    }
//                    break;
//                }
//                else {
//                    if (prefersClockwise){
//                        currentDirection = currentDirection.rotateLeft();
//                    }
//                    else {
//                        currentDirection = currentDirection.rotateRight();
//                    }
//                }
//            }
        }
    }

    public static Direction rotate(RobotController rc, Direction startDirection, boolean rotateClockwise) {
        if (rotateClockwise) {
            return startDirection.rotateRight();
        }
        else {
            return startDirection.rotateLeft();
        }
    }

    public static Direction rotate(RobotController rc, Direction startDirection) {
        return rotate(rc, startDirection, RobotPlayer.prefersClockwise);
    }

    public static Direction getRotateValidMove(RobotController rc, Direction startDirection, boolean rotateClockwise) {
        if (rc.canMove(startDirection)){
            return startDirection;
        }
        else {
            Direction proposedDir = rotate(rc, startDirection, rotateClockwise);
            while (proposedDir != startDirection){
                if (rc.canMove(proposedDir)){
                    return proposedDir;
                }
                else{
                    proposedDir = rotate(rc, proposedDir, rotateClockwise);
                }
            }
            return Direction.CENTER;
        }
    }

    // find closest movable direction to desired direction
    public static Direction getClosestValidMoveDirection(RobotController rc, Direction desired_dir, boolean preferClockwise){
        if(rc.canMove(desired_dir)) return desired_dir;
        for (int rotation_offset = 1; rotation_offset <= 4; rotation_offset++){  // 4 is 1/2 of the 8 possible movement
            // directions
            Direction left_dir = directions[(desired_dir.ordinal() + 8 + rotation_offset) % 8];
            Direction right_dir = directions[(desired_dir.ordinal() + 8 - rotation_offset) % 8];
            if (preferClockwise) {
                if (rc.canMove(right_dir)) return right_dir;
            }
            if (rc.canMove(left_dir)) return left_dir;
            if (rc.canMove(right_dir)) return right_dir;
        }
        return Direction.CENTER;
    }

    public static Direction getClosestValidMoveDirection(RobotController rc, Direction desired_dir) {
        return getClosestValidMoveDirection(rc, desired_dir, RobotPlayer.prefersClockwise);
    }

    public static void trackedMove(RobotController rc, Direction dir) throws GameActionException {
        if (rc.canMove(dir) && dir != Direction.CENTER) {
            RobotPlayer.myLastLocation = rc.getLocation();
            rc.move(dir);
            RobotPlayer.lastMoved = dir;
            RobotPlayer.myCurrentLocation = rc.getLocation();
        }
        else {
            myLastLocation = rc.getLocation();
        }
    }

    static Direction best_right_turn(RobotController rc, Direction desired_dir) throws GameActionException {
        if(rc.canMove(desired_dir)) return desired_dir;
        for (int rotation_offset = 1; rotation_offset <= 7; rotation_offset++){  // 7 other directions
            Direction right_dir = Direction.values()[(desired_dir.ordinal() + 8 - rotation_offset) % 8];
            trackedMove(rc, desired_dir);
        }
        return Direction.CENTER;
    }


    //_______________Advanced Map Based Pathfinding____________________//

    // find closest movable direction to desired direction
    public static Direction getSmartClosestValidMoveDirection(RobotController rc, Direction desired_dir,
                                                              boolean preferClockwise) throws GameActionException {
        if(rc.canMove(desired_dir)) return desired_dir;
        for (int rotation_offset = 1; rotation_offset <= 4; rotation_offset++){  // 4 is 1/2 of the 8 possible movement
            // directions
            Direction left_dir = directions[(desired_dir.ordinal() + 8 + rotation_offset) % 8];
            Direction right_dir = directions[(desired_dir.ordinal() + 8 - rotation_offset) % 8];
            if (preferClockwise) {
                if (rc.canMove(right_dir)) {
                    if (!rc.getLocation().equals(getResultOfMove(rc, rc.getLocation(), right_dir))){
                        return right_dir;
                    }
                }
            }
            if (rc.canMove(left_dir)) {
                if (!rc.getLocation().equals(getResultOfMove(rc, rc.getLocation(), left_dir))){
                    return left_dir;
                }
            }
            if (rc.canMove(right_dir)) {
                if (!rc.getLocation().equals(getResultOfMove(rc, rc.getLocation(), right_dir))){
                    return right_dir;
                }
            }
        }
        return Direction.CENTER;
    }

    public static Direction getSmartClosestValidMoveDirection(RobotController rc, Direction desired_dir) throws GameActionException {
        return getSmartClosestValidMoveDirection(rc, desired_dir, prefersClockwise);
    }

    static Direction getCurrentTileDir(map_tiles currentTile) {
        switch (currentTile) {
            case CURRENT_N:
                return Direction.NORTH;
            case CURRENT_NE:
                return Direction.NORTHEAST;
            case CURRENT_E:
                return Direction.EAST;
            case CURRENT_SE:
                return Direction.SOUTHEAST;
            case CURRENT_S:
                return Direction.SOUTH;
            case CURRENT_SW:
                return Direction.SOUTHWEST;
            case CURRENT_W:
                return Direction.WEST;
            case CURRENT_NW:
                return Direction.NORTHWEST;
            default:
                return Direction.CENTER;
        }
    }

    static MapLocation getResultOfMove(RobotController rc, MapLocation startLocation, Direction dir) throws GameActionException {
        MapLocation targetMoveLocation = startLocation.add(dir);

        // If it is off the map, return startLocation
        if (!rc.onTheMap(targetMoveLocation)) {return startLocation;}

        // Get type of destination, either from map or sensing
        map_tiles knownTile = RobotPlayer.get_map_location_tile(targetMoveLocation);
        if (knownTile.equals(map_tiles.UNKNOWN)) {
            knownTile = Sensing.scanMapTileType(rc, targetMoveLocation);
            if (!knownTile.equals(map_tiles.UNKNOWN)) {
                RobotPlayer.set_map_location_tile(targetMoveLocation, knownTile);
            }
        }

        MapLocation returnLocation = startLocation;

        // If it is a current, handle logic. Otherwise, return appropriate response
        switch (knownTile) {
            case UNKNOWN:
            case PLAIN:
            case ADAMANTIUM:
            case MANA:
            case ELIXIR:
            case CLOUD:
            case ISLAND_NEUTRAL:
                returnLocation = targetMoveLocation;
                break;
            case WALL:
            case HQ_ENEMY:
            case HQ_FRIENDLY:
                returnLocation = startLocation;
                break;
            case CURRENT_N:
            case CURRENT_NE:
            case CURRENT_E:
            case CURRENT_SE:
            case CURRENT_S:
            case CURRENT_SW:
            case CURRENT_W:
            case CURRENT_NW:
                Direction currentPush = getCurrentTileDir(knownTile);
                MapLocation potentialPushLocation = targetMoveLocation.add(currentPush);

                // If it is off the map, return targetMoveLocation
                if (!rc.onTheMap(potentialPushLocation)) {return targetMoveLocation;}

                // Get type of destination, either from map or sensing
                map_tiles potentialPushTile = RobotPlayer.get_map_location_tile(potentialPushLocation);
                if (potentialPushTile.equals(map_tiles.UNKNOWN)) {
                    potentialPushTile = Sensing.scanMapTileType(rc, potentialPushLocation);
                    if (!potentialPushTile.equals(map_tiles.UNKNOWN)) {
                        RobotPlayer.set_map_location_tile(potentialPushLocation, potentialPushTile);
                    }
                }

                switch (potentialPushTile) {
                    case WALL:
                    case HQ_ENEMY:
                    case HQ_FRIENDLY:
                        returnLocation = targetMoveLocation;
                        break;
                    default:
                        returnLocation = potentialPushLocation;
                        break;
                }
                break;
            default:
                // Should never reach this.
                returnLocation = startLocation;
        }
        // If we somehow reach this point, just return startLocation
        return returnLocation;
    }
}
