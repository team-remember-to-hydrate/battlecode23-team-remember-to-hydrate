package latest;

import battlecode.common.*;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Launcher {


    static int my_HQ = 99;  // 0-3 are valid starts in not valid state
    static MapLocation my_HQ_location;

    static RobotPlayer.states my_state;
    static MapLocation target_location;
    static String indicator_string = "";
    static MapLocation me;

    /**
     * Run a single turn for a Launcher.
     * This code is wrapped inside the infinite loop in run(), so it is called once per turn.
     */
    static void runLauncher(RobotController rc) throws GameActionException {
        // initialize variables
        boolean should_move = true;
        boolean blocking_carrier = false;
        RobotInfo[] nearby_bots = rc.senseNearbyRobots();
        Team opponent = rc.getTeam().opponent();
        me = rc.getLocation();
        Direction blocked_carrier_dir = Direction.CENTER;

        // define variable for desired direction to move.
        Direction dir = RobotPlayer.directions[RobotPlayer.rng.nextInt(RobotPlayer.directions.length)];
        MapLocation map_center = new MapLocation(rc.getMapWidth() / 2, rc.getMapHeight() / 2);
        MapLocation attack_location = new MapLocation(0,0);

        // This runs on my first turn only
        if(my_HQ > 3){
            my_HQ = RobotPlayer.get_HQ_array_index(rc);
            my_HQ_location = RobotPlayer.unpackMapLocation(rc.readSharedArray(my_HQ));
            my_state = RobotPlayer.states.INITIAL;
        }

        // Always set an attack target if we can attack
        attack_location = target_lowest_health(rc,nearby_bots,opponent,attack_location);

        switch(my_state){
            case INITIAL:
                my_state = RobotPlayer.states.GROUP;
                break;

            case GROUP:
                indicator_string += "GROUP ";
                dir = group_dir(rc, dir);

                break;

            case ATTACK:
                indicator_string += "ATTACK ";
                break;

            case OCCUPY:
                dir = occupy_dir(rc,dir,nearby_bots);
                indicator_string += "OCCUPY ";

                break;

            case ANCHOR:
                // if we have reached our task destination we should switch to occupy island

                indicator_string += "ANCHOR ";
                break;
        }


        if(rc.canAttack(attack_location)){
            rc.attack(attack_location);
            rc.setIndicatorString("Attacking " + attack_location);
        }

        // if we are in a holding state check for orders
        if(my_state.equals(RobotPlayer.states.INITIAL) || my_state.equals(RobotPlayer.states.GROUP)){
            RobotPlayer.hq_states current_HQ_state = RobotPlayer.hq_states.values()[RobotPlayer.unpackExtra(rc.readSharedArray(my_HQ))];
            // if we have a task lets get to it
            if(current_HQ_state.equals(RobotPlayer.hq_states.TASK)){
                int task_info = rc.readSharedArray(12 +my_HQ);
                target_location = RobotPlayer.unpackMapLocation(task_info);
                my_state = RobotPlayer.states.values()[RobotPlayer.unpackExtra(task_info)];
                System.out.println("got at task " + my_state + " at " + target_location);
            }
        }
        // then we have a task to do, let get to it
        else
        {
            dir = me.directionTo(target_location);
        }

        // check for adjacent carrier, so we can move
        Direction adjacent_carrier =  adjacent_carrier(rc, nearby_bots);
        if(!adjacent_carrier.equals(Direction.CENTER)){
            blocking_carrier = true;
            blocked_carrier_dir = adjacent_carrier;
        }

        // if we are on an island lets stay unless we are blocking a carrier
        if(rc.senseIsland(me) >= 0 & !blocking_carrier){
            should_move = false;
        }

        //actually move away from blocked carrier or desired direction.
        if(blocking_carrier & rc.canMove(RobotPlayer.movable_direction(rc, blocked_carrier_dir.opposite()))){
            rc.move(RobotPlayer.movable_direction(rc, blocked_carrier_dir.opposite()));
        } else if(rc.canMove(RobotPlayer.movable_direction(rc, dir)) & !blocking_carrier){
            rc.move(RobotPlayer.movable_direction(rc, dir));
        }

        indicator_string += "dir:" + dir;
        indicator_string += " " + my_state + " : " + blocking_carrier;
        rc.setIndicatorString(indicator_string);
    }

    static MapLocation target_lowest_health(RobotController rc, RobotInfo[] nearby_bots, Team opponent,MapLocation attack_location){
        // attack enemies within range visible bots gotta get them enemies
        int lowest_health = 10000;
        for(RobotInfo bot : nearby_bots){
            if(bot.getTeam().equals(opponent)) {
                if (bot.health < lowest_health & !bot.getType().equals(RobotType.HEADQUARTERS)) {
                    lowest_health = bot.health;
                    attack_location = bot.getLocation();
                }
            }
        }
        return attack_location;
    }

    static Direction group_dir(RobotController rc, Direction dir) {
        // Stay near spawning headquarters but move towards center
        MapLocation center = new MapLocation(rc.getMapWidth() / 2, rc.getMapHeight()/2);
        dir = rc.getLocation().directionTo(center);
        if (rc.canMove(RobotPlayer.movable_direction(rc, dir))) {
            MapLocation new_spot = rc.getLocation().add(dir);
            if (new_spot.distanceSquaredTo(my_HQ_location) > RobotType.HEADQUARTERS.visionRadiusSquared &
                    my_state.equals(RobotPlayer.states.GROUP)) {
                // if our direction carries us too far move the other direction.
                dir = rc.getLocation().directionTo(my_HQ_location);
            }
        }
        return dir;
    }

    static Direction occupy_dir(RobotController rc, Direction dir, RobotInfo[] nearbyBots) throws GameActionException {
        // move towards the closest empty island square
        // **** reduce bytecode by getting island id inside orders
        int[] islands = rc.senseNearbyIslands();
        int closest = 1000;
        MapLocation myIsland = target_location;
        for (int id : islands) {
            MapLocation[] thisIslandLocs = rc.senseNearbyIslandLocations(id);
            for(MapLocation location : thisIslandLocs){
                if(me.distanceSquaredTo(location) < closest & notOccupied(location,nearbyBots)){
                    closest = me.distanceSquaredTo(location);
                    myIsland = location;
                }
            }
        }
        return me.directionTo(myIsland);
    }

    static boolean notOccupied(MapLocation location, RobotInfo[] nearbyBots){
        for(RobotInfo bot : nearbyBots){
            if(location.equals(bot.location)) return false;
        }
        return true;
    }

    static Direction adjacent_carrier(RobotController rc, RobotInfo[] nearby_bots){
        for(RobotInfo bot : nearby_bots){
            if(bot.getType().equals(RobotType.CARRIER) & bot.location.isAdjacentTo(rc.getLocation())) {
                return rc.getLocation().directionTo(bot.getLocation());
            }
        }
        return Direction.CENTER;
    }

}
