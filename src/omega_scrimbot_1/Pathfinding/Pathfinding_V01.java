package omega_scrimbot_1.Pathfinding;
import battlecode.common.*;

public class Pathfinding_V01 {
    // find closest movable direction to desired direction
    static Direction getClosestValidMove(RobotController rc, Direction desired_dir){
        if(rc.canMove(desired_dir)) return desired_dir;
        for (int rotation_offset = 1; rotation_offset <= 4; rotation_offset++){  // 4 is 1/2 of the 8 possible directions
            Direction left_dir = Direction.values()[(desired_dir.ordinal() +  rotation_offset) % 8];
            Direction right_dir = Direction.values()[(desired_dir.ordinal() + 8 - rotation_offset) % 8];
            if (rc.canMove(left_dir)) return left_dir;
            if (rc.canMove(right_dir)) return right_dir;
        }
        return Direction.CENTER;
    }

/*    // find closest movable direction to desired direction, intelligently select better of left/right
    static Direction getAlternateValidMove(RobotController rc, Direction desired_dir, MapLocation destination_location){
        if(rc.canMove(desired_dir)) return desired_dir;
        for (int rotation_offset = 1; rotation_offset <= 4; rotation_offset++){  // 4 is 1/2 of the 8 possible directions
            Direction left_dir = Direction.values()[(desired_dir.ordinal() +  rotation_offset) % 8];
            Direction right_dir = Direction.values()[(desired_dir.ordinal() + 8 - rotation_offset) % 8];
            if (rc.canMove(left_dir)) return left_dir;
            if (rc.canMove(right_dir)) return right_dir;
        }
        return Direction.CENTER;
    }*/

    /**
     * Returns the next valid move direction starting with the startDirection and rotating in the selected direction
     * until a valid move is found.
     * @param rc The robot controller.
     * @param startDirection The direction to start with/from.
     * @param rotateClockwise If true, will try clockwise. Else anti-clockwise.
     * @return Next valid move in the desired rotation direction, or Center direction if no other valid move found.
     */
    static Direction getRotateValidMove(RobotController rc, Direction startDirection, boolean rotateClockwise) {
        if (rc.canMove(startDirection)){
            return startDirection;
        }
        else {
            Direction[] directions = Direction.allDirections();
            int attempts = 1;
            int incrementVal = 1;

            if (!rotateClockwise){
                incrementVal = -1;
            }

            int curOffset = incrementVal;
            while (attempts < directions.length){
                Direction proposedDir = directions[curOffset % directions.length];
                if ((proposedDir.ordinal() != 0) && rc.canMove(proposedDir)){
                    return proposedDir;
                }
                else {
                    curOffset += incrementVal;
                    attempts++;
                }
            }
            return Direction.CENTER;
        }
    }
}
