package fix_get_build_direction;

import battlecode.common.*;

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
        RobotInfo[] nearby_bots = rc.senseNearbyRobots();
        Team opponent = rc.getTeam().opponent();
        Direction dir = RobotPlayer.directions[RobotPlayer.rng.nextInt(RobotPlayer.directions.length)];

        int closest_bot = 99;

        if(my_HQ > 3){
            my_HQ = RobotPlayer.get_HQ_array_index(rc);
            my_HQ_location = RobotPlayer.unpackMapLocation(rc.readSharedArray(my_HQ));
            my_state = states.INITIAL;
        }

        // This is the HQ that we check for orders
        if(rc.getRoundNum() == 10){
            System.out.println("My HQ is set to " + my_HQ + " which is " + my_state);
        }



        // attack enemies within range visible bots gotta get them enemies
        for(RobotInfo bot : nearby_bots){
            if(bot.getTeam().equals(opponent)){
                if(rc.canAttack(bot.getLocation())){
                    rc.attack(bot.getLocation());
                    rc.setIndicatorString("Attacking " + bot.getLocation());
                }
            }
            // set a movement goal away from the closest friendly
            else {
                int this_bot_distance = rc.getLocation().distanceSquaredTo(bot.getLocation());
                if(this_bot_distance < closest_bot){
                    closest_bot = this_bot_distance;
                    dir = rc.getLocation().directionTo(bot.getLocation()).opposite();
                    if (!rc.onTheMap(rc.getLocation().add(dir))) {
                        dir = dir.opposite();
                    }
                }
            }
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
        else{
            dir = rc.getLocation().directionTo(target_location);
        }

        // if we are on an island lets stay
        if(rc.senseIsland(rc.getLocation()) >= 0){
            should_move = false;
        }


        // Also try to move randomly.
        if (rc.canMove(RobotPlayer.movable_direction(rc, dir)) & should_move) {
            MapLocation new_spot = rc.getLocation().add(dir);
            if(new_spot.distanceSquaredTo(my_HQ_location) < RobotType.HEADQUARTERS.visionRadiusSquared ||
                    my_state.equals(states.ATTACK) ||
                    my_state.equals(states.PLACE_ANCHOR) ||
                    my_state.equals(states.OCCUPY_ISLAND)){
                rc.move(RobotPlayer.movable_direction(rc, dir));
            }

        }

        rc.setIndicatorString("target " + rc.getLocation().add(dir) + " mh state is " + my_state);
    }
}
