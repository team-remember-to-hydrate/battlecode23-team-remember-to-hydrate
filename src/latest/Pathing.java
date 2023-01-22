package latest;

import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;

import static latest.RobotPlayer.directions;
import static latest.RobotPlayer.rng;

public class Pathing {
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
    public static MapLocation getLocationInDirection(MapLocation l, Direction d) throws GameActionException {
        MapLocation result = null;
        switch(d){
            case NORTH: result = new MapLocation(l.x, l.y + 1); break;
            case EAST: result = new MapLocation(l.x + 1, l.y); break;
            case SOUTH: result = new MapLocation(l.x, l.y - 1); break;
            case WEST: result = new MapLocation(l.x - 1, l.y); break;
            case NORTHEAST: result = new MapLocation(l.x + 1, l.y + 1); break;
            case SOUTHEAST: result = new MapLocation(l.x + 1, l.y - 1); break;
            case SOUTHWEST: result = new MapLocation(l.x - 1, l.y - 1); break;
            case NORTHWEST: result = new MapLocation(l.x - 1, l.y + 1); break;
        }
        return result;
    }
}
