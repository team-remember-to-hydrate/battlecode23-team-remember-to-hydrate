package hydr8player.v1.Carrier.v2.capabilities;

import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.RobotController;
import battlecode.common.WellInfo;
import hydr8player.v1.Carrier.v2.CarrierState;

import java.util.Random;

public class RunPathingToWell { // Bug nav
    static final Random rng = new Random(6147);
    static final Direction[] directions = {
            Direction.NORTH,
            Direction.NORTHEAST,
            Direction.EAST,
            Direction.SOUTHEAST,
            Direction.SOUTH,
            Direction.SOUTHWEST,
            Direction.WEST,
            Direction.NORTHWEST
    };
    static Direction currentDirection = null;
    public void run(RobotController rc, CarrierState state) throws GameActionException {
        if (state.getWell() == null) {
            rc.setIndicatorString("RunPathingToWell > No well found, moving randomly.");
            Direction dir = directions[rng.nextInt(directions.length)];
            if (rc.canMove(dir)) {
                rc.move(dir);
            }
        }
        else {
            rc.setIndicatorString("RunPathingToWell > Pathing to well.");
        }
        if (rc.getLocation().equals(state.getWell().getMapLocation())){ // Do I need to be adjacent instead?
            return;
        }
        if (!rc.isActionReady()){
            return;
        }
        Direction d = rc.getLocation().directionTo(state.getWell().getMapLocation());
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
                    currentDirection = currentDirection.rotateRight();
                }
            }
        }
    }
}
