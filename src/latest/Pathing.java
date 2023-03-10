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

    static Direction closestPossibleDirection(RobotController rc, Direction desired_dir){
        if(rc.canMove(desired_dir)) return desired_dir;
        for (int rotation_offset = 1; rotation_offset <= 4; rotation_offset++){  // 4 is 1/2 of the 8 possible directions
            Direction left_dir = Direction.values()[(desired_dir.ordinal() +  rotation_offset) % 8];
            Direction right_dir = Direction.values()[(desired_dir.ordinal() + 8 - rotation_offset) % 8];
            if (rc.canMove(left_dir)) return left_dir;
            if (rc.canMove(right_dir)) return right_dir;
        }
        return Direction.CENTER;
    }

    public static void trackedMove(RobotController rc, Direction dir) throws GameActionException {
        if (rc.canMove(dir)) {
            RobotPlayer.myLastLocation = rc.getLocation();
            rc.move(dir);
            RobotPlayer.lastMoved = dir;
            RobotPlayer.myCurrentLocation = rc.getLocation();

        }
    }
}
