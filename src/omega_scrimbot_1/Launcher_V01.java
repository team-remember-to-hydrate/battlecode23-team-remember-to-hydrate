package omega_scrimbot_1;

import battlecode.common.*;
import omega_scrimbot_1.Pathfinding.Pathfinding_V01;

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

        RobotInfo[] visibleEnemies = rc.senseNearbyRobots(-1, opponent);
        for (RobotInfo enemy : visibleEnemies){
            if (enemy.getType() != RobotType.HEADQUARTERS){
                Direction dir = rc.getLocation().directionTo(enemy.getLocation());
                if (rc.canMove(dir)){
                    rc.move(dir);
                    RobotPlayer.lastMoved = dir;
                    break;
                }
            }
        }
        // Also try to move with clockwise momentum.
        Direction dir = Pathfinding_V01.getRotateValidMove(rc, RobotPlayer.lastMoved, true);
        if (rc.canMove(dir)) {
            rc.move(dir);
            RobotPlayer.lastMoved = dir;
        }
    }
}
