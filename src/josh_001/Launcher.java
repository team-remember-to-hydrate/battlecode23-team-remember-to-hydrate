package josh_001;

import battlecode.common.*;
import java.util.Random;

public class Launcher {

    static byte[][] map;
    static int myHQ = 99;  // 0-3 are valid starts in not valid state
    static enum states {
        INITIAL,
        SCOUT,
        ESCORT,
        GROUP,
        ATTACk};

    /**
     * Run a single turn for a Launcher.
     * This code is wrapped inside the infinite loop in run(), so it is called once per turn.
     */
    static void runLauncher(RobotController rc) throws GameActionException {
        if(myHQ > 3){
            myHQ = RobotPlayer.get_HQ_array_index(rc);
        }

        // TESTING
        if(rc.getRoundNum() == 10){
            System.out.println("My HQ is set to " + myHQ);
        }

        // Try to attack someone
        int radius = rc.getType().actionRadiusSquared;
        Team opponent = rc.getTeam().opponent();
        RobotInfo[] enemies = rc.senseNearbyRobots(radius, opponent);
        if (enemies.length > 0) {
            // MapLocation toAttack = enemies[0].location;
            MapLocation toAttack = rc.getLocation().add(Direction.EAST);

            if (rc.canAttack(toAttack)) {
                rc.setIndicatorString("Attacking");
                rc.attack(toAttack);
            }
        }

        // Also try to move randomly.
        Direction dir = RobotPlayer.directions[RobotPlayer.rng.nextInt(RobotPlayer.directions.length)];
        if (rc.canMove(dir)) {
            rc.move(dir);
        }
    }
}
