package omega_scrimbot_1;

import battlecode.common.*;

import java.util.Random;

public class Launcher_V01 {
    /**
     * Run a single turn for a Launcher.
     * This code is wrapped inside the infinite loop in run(), so it is called once per turn.
     */
    static void runLauncher(RobotController rc) throws GameActionException {
        final Direction[] directions = RobotPlayer.directions;
        final Random rng = RobotPlayer.rng;
        int turnCount = RobotPlayer.turnCount;

        // Try to attack someone
        int radius = rc.getType().actionRadiusSquared;
        Team opponent = rc.getTeam().opponent();
        RobotInfo[] enemies = rc.senseNearbyRobots(radius, opponent);
        if (enemies.length > 0) {
            for (int i = 0; i < enemies.length; i++){
                if (enemies[i].getType().compareTo(RobotType.HEADQUARTERS) != 0) { //TODO: method to pick best target
                    MapLocation toAttack = enemies[0].location;

                    if (rc.canAttack(toAttack)) {
                        rc.setIndicatorString("Attacking");
                        rc.attack(toAttack);
                    }
                    // TODO: Else, move towards enemy
                }
            }

        }

        // Also try to move randomly.
        Direction dir = directions[rng.nextInt(directions.length)];
        if (rc.canMove(dir)) {
            rc.move(dir);
        }
    }
}
