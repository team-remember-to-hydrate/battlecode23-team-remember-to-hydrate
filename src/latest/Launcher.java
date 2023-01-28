package latest;

import battlecode.common.*;

import java.util.*;

public class Launcher {


    static int my_HQ = 99;  // 0-3 are valid starts in not valid state
    static MapLocation my_HQ_location;

    static RobotPlayer.states my_state;
    static MapLocation target_location;
    static String indicator_string = "";
    static MapLocation me;
    static int myLeaderID = Integer.MAX_VALUE;

    /**
     * Run a single turn for a Launcher.
     * This code is wrapped inside the infinite loop in run(), so it is called once per turn.
     */
    static void runLauncher(RobotController rc) throws GameActionException {
        // initialize variables
        indicator_string = "";
        boolean should_move = true;
        boolean blocking_carrier = false;
        RobotInfo[] nearby_bots = rc.senseNearbyRobots();
        Team opponent = rc.getTeam().opponent();
        me = rc.getLocation();
        Direction blocked_carrier_dir = Direction.CENTER;

        // define variable for desired direction to move.
        Direction dir = RobotPlayer.lastMoved; //RobotPlayer.directions[RobotPlayer.rng.nextInt(RobotPlayer.directions.length)];
        MapLocation map_center = new MapLocation(rc.getMapWidth() / 2, rc.getMapHeight() / 2);
        MapLocation attack_location = new MapLocation(0,0);

        // This runs on my first turn only
        if(RobotPlayer.turnCount <= 1){
            my_HQ = RobotPlayer.get_HQ_array_index(rc);
            my_HQ_location = RobotPlayer.unpackMapLocation(rc.readSharedArray(my_HQ));
            my_state = RobotPlayer.states.INITIAL;
        }

        // Always set an attack target if we can attack
        ArrayList<RobotInfo> target_bots = Sensing.scanCombatUnitsOfTeam(rc, rc.getTeam().opponent());
        if (target_bots.size() < 1) {
            target_bots = Sensing.scanAnyUnitsOfTeamInRange(rc, rc.getTeam().opponent(), -1);
        }

        RobotInfo weakestBot = Sensing.scanWeakestBotInGroup(rc, target_bots);
        if (weakestBot != null) {
            attack_location = Sensing.scanWeakestBotInGroup(rc, target_bots).getLocation();
        }

        int my_task = Comms.get_command_for_me(rc);

        if(my_task > 0){
            target_location = Comms.get_MapLocation(my_task);
            my_state = RobotPlayer.states.values()[Comms.get_task_type(rc.readSharedArray(my_task))];
            indicator_string += ("My state is: " + my_state.toString());
        }

        // Attack before moving, in case we move out of range
        if(rc.canAttack(attack_location)){
            rc.attack(attack_location);
            indicator_string += ("Attacking " + attack_location);
        }

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
                dir = me.directionTo(target_location);
                break;

            case OCCUPY:
                indicator_string += "OCCUPY ";
                dir = occupy_dir(rc,dir,nearby_bots);
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
        Direction adjacent_carrier =  adjacent_carrier(rc, Sensing.smartScanMembersOfTeam(rc, rc.getTeam()));
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

    static Direction group_dir(RobotController rc, Direction dir) throws GameActionException {
        // Stay near spawning headquarters but move towards center
        MapLocation center = new MapLocation(rc.getMapWidth() / 2, rc.getMapHeight()/2);
        dir = rc.getLocation().directionTo(center);

        // Check for a leader, and follow them if we find one.
        checkAndReplaceLeader(rc);
        indicator_string += ("LeaderID: " + myLeaderID);
        if (myLeaderID < Integer.MAX_VALUE && !(myLeaderID > rc.getID())) { //If I have a leader candidate better than myself.
            dir = rc.getLocation().directionTo(rc.senseRobot(myLeaderID).getLocation());
        }

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

    private static void checkAndReplaceLeader(RobotController rc) throws GameActionException {
        if (!rc.canSenseRobot(myLeaderID)){
            myLeaderID = Integer.MAX_VALUE;
            ArrayList<RobotInfo> mySquad =  Sensing.scanCombatUnitsOfTeamInRange(rc, rc.getTeam(), 13);
            if (mySquad.size() > 0) {
                myLeaderID = mySquad.get(0).getID();
            }
            for (int i = 1; i < mySquad.size(); i++){
                int id = mySquad.get(i).getID();
                if (id < myLeaderID){
                    myLeaderID = id;
                }
            }
        }
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
