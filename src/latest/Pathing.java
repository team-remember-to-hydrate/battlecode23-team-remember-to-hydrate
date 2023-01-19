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

    public static Direction rotate(RobotController rc, Direction startDirection, boolean rotateClockwise){
        if (rotateClockwise) {
            return startDirection.rotateRight();
        }
        else {
            return startDirection.rotateLeft();
        }
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

    public static void trackedMove(RobotController rc, Direction dir) throws GameActionException {
        if (rc.canMove(dir)) {
            rc.move(dir);
            RobotPlayer.lastMoved = dir;
        }
    }
}
