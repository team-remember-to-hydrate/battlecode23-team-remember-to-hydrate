package josh_002;

import battlecode.common.*;
import java.util.Random;

public class Launcher {

    static byte[][] map;
    static int my_HQ = 99;  // 0-3 are valid starts in not valid state
    static MapLocation my_HQ_location;
    static enum states {
        ATTACK,
        PLACE_ANCHOR,
        OCCUPY_ISLAND,
        GROUP,
        INITIAL};
    static states my_state;
    static MapLocation target_location;

    /**
     * Run a single turn for a Launcher.
     * This code is wrapped inside the infinite loop in run(), so it is called once per turn.
     */
    static void runLauncher(RobotController rc) throws GameActionException {
        // generate info to make decisions
        boolean should_move = true;
        boolean blocking_carrier = false;
        RobotInfo[] nearby_bots = rc.senseNearbyRobots();
        Team opponent = rc.getTeam().opponent();
        Direction dir = RobotPlayer.directions[RobotPlayer.rng.nextInt(RobotPlayer.directions.length)];
        int closest_bot = 99;
        MapLocation attack_location = new MapLocation(0,0);
        String indicator_string = "";
        Direction blocked_carrier_dir = Direction.CENTER;

        if(my_HQ > 3){
            my_HQ = RobotPlayer.get_HQ_array_index(rc);
            my_HQ_location = RobotPlayer.unpackMapLocation(rc.readSharedArray(my_HQ));
            my_state = states.INITIAL;
        }
        // if we have reached our task destination we should switch to occupy island
        if(target_location != null){
            if(rc.getLocation().distanceSquaredTo(target_location) < 1)  my_state = states.OCCUPY_ISLAND;
        }


        // attack enemies within range visible bots gotta get them enemies
        int lowest_health = 100;
        for(RobotInfo bot : nearby_bots){
            if(bot.getTeam().equals(opponent)) {
                if (bot.health < lowest_health) {
                    lowest_health = bot.health;
                    attack_location = bot.getLocation();
                }
            }

            // set a movement goal away from the closest friendly
            else {
                int this_bot_distance = rc.getLocation().distanceSquaredTo(bot.getLocation());
                if(this_bot_distance == 1 & bot.getType().equals(RobotType.CARRIER)){
                    blocking_carrier = true;
                    blocked_carrier_dir = rc.getLocation().directionTo(bot.getLocation());
                }
                if(this_bot_distance < closest_bot){
                    closest_bot = this_bot_distance;
                    dir = rc.getLocation().directionTo(bot.getLocation()).opposite();
                }
            }
        }
        if(rc.canAttack(attack_location)){
            rc.attack(attack_location);
            rc.setIndicatorString("Attacking " + attack_location);
        }

        // if we are in a holding state check for orders
        if(my_state.equals(states.INITIAL) || my_state.equals(states.GROUP)){
            RobotPlayer.hq_states current_HQ_state = RobotPlayer.hq_states.values()[RobotPlayer.unpackExtra(rc.readSharedArray(my_HQ))];
            // if we have a task lets get to it
            if(current_HQ_state.equals(RobotPlayer.hq_states.TASK)){
                int task_info = rc.readSharedArray(12 +my_HQ);
                target_location = RobotPlayer.unpackMapLocation(task_info);
                my_state = states.values()[RobotPlayer.unpackExtra(task_info)];
            }
        }
        // then we have a task to do, let get to it
        else
        {
            dir = rc.getLocation().directionTo(target_location);
        }


        // if we are on an island lets stay unless we are blocking a carrier
        if(rc.senseIsland(rc.getLocation()) >= 0 & !blocking_carrier){
            should_move = false;
        }


        // Stay near spawning headquarters
        if (rc.canMove(RobotPlayer.movable_direction(rc, dir)) & should_move) {
            MapLocation new_spot = rc.getLocation().add(dir);
            if(new_spot.distanceSquaredTo(my_HQ_location) < RobotType.HEADQUARTERS.visionRadiusSquared ||
                    my_state.equals(states.ATTACK) ||
                    my_state.equals(states.PLACE_ANCHOR) ||
                    my_state.equals(states.OCCUPY_ISLAND)){
                indicator_string += "not grouping ";
            }
            else{
                dir = dir.opposite();
            }

        }

        //actually move away from blocked carrier or desired direction.
        if(blocking_carrier & rc.canMove(RobotPlayer.movable_direction(rc, blocked_carrier_dir.opposite()))){
            rc.move(RobotPlayer.movable_direction(rc, blocked_carrier_dir.opposite()));
        } else if(rc.canMove(RobotPlayer.movable_direction(rc, dir)) & !blocking_carrier){
            rc.move(RobotPlayer.movable_direction(rc, dir));
        }

        indicator_string += "target: " + rc.getLocation().add(dir);
        indicator_string += " state: " + my_state + " blocking:  " + blocking_carrier;
        rc.setIndicatorString(indicator_string);
    }
}
