package latest;

import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.RobotController;

import java.util.Random;

public class Amplifier {
    static void runAmplifier(RobotController rc) throws GameActionException {
        final Direction[] directions = RobotPlayer.directions;
        final Random rng = RobotPlayer.rng;

        // Try to move with clockwise momentum
        Direction dir = Pathing.getRotateValidMove(rc, RobotPlayer.lastMoved, true);
        //Pathing.trackedMove(rc, dir);
        if (rc.canMove(dir)) {
            rc.move(dir);
            RobotPlayer.lastMoved = dir;
        }


    }
}
