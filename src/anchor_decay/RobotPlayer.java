package anchor_decay;

import battlecode.common.*;
import anchor_decay.Amplifier;
import anchor_decay.CarrierStrategy;
import anchor_decay.Comms;
import anchor_decay.Headquarters;
import anchor_decay.Launcher;
import anchor_decay.Pathing;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;

/**
 * RobotPlayer is the class that describes your main robot strategy.
 * The run() method inside this class is like your main function: this is what we'll call once your robot
 * is created!
 */
public strictfp class RobotPlayer {

    /**
     * We will use this variable to count the number of turns this robot has been alive.
     * You can use static variables like this to save any information you want. Keep in mind that even though
     * these variables are static, in Battlecode they aren't actually shared between your robots.
     */
    static int turnCount = 0;
    static int my_group = 0;
    static Direction lastMoved = Direction.NORTH;
    static boolean didMoveLastTurn = false;
    static boolean prefersClockwise = true;
    static int myHealthLastTurn;
    static int lastRoundScannedEnemies = -1;
    static RobotInfo[] scannedEnemies = null;
    static int lastRoundScannedAllies = -1;
    static RobotInfo[] scannedAllies = null;
    static MapLocation myLastLocation = null;
    public static MapLocation myCurrentLocation;
    static MapLocation lastLocationScannedMapInfos = null;
    static MapInfo[] scannedMapInfos = null;
    static MapLocation lastLocationScannedIslands = null;
    static int[] scannedIslandIDs = null;
    static MapLocation[] scannedIslandLocations = null;
    static MapLocation lastLocationScannedWells = null;
    static WellInfo[] scannedWellInfos = null;
    static int[][] map = new int[GameConstants.MAP_MAX_WIDTH][GameConstants.MAP_MAX_HEIGHT];
    static int[] teamKnownIslandDetails = new int[GameConstants.MAX_NUMBER_ISLANDS + 1];

    // Too much bytecode, and we only really need 1 location per island
//    static ArrayList<HashSet<MapLocation>> teamKnownIslandLocations =
//            new ArrayList<HashSet<MapLocation>>(GameConstants.MAX_NUMBER_ISLANDS + 1);


    static List<Integer> island_ids = new ArrayList<>();
    static MapLocation[] island_locations = new MapLocation[GameConstants.MAX_NUMBER_ISLANDS + 1];
    static List<MapLocation> well_locations =  new ArrayList<>();
    static ArrayList<Integer> myIslandFullInfoBroadcastQueue = new ArrayList<>();
    static ArrayList<Integer> myIslandDetailsBroadcastQueue = new ArrayList<>();
    static ArrayList<Short> myWellBroadcastQueue = new ArrayList<>();
    public static states my_state;



    /**
     * A random number generator.
     * We will use this RNG to make some random moves. The Random class is provided by the java.util.Random
     * import at the top of this file. Here, we *seed* the RNG with a constant number (6147); this makes sure
     * we get the same sequence of numbers every time this code is run. This is very useful for debugging!
     */
    public static final Random rng = new Random(6147);

    /** Array containing all the possible movement directions. */
    public static final Direction[] directions = {
        Direction.NORTH,
        Direction.NORTHEAST,
        Direction.EAST,
        Direction.SOUTHEAST,
        Direction.SOUTH,
        Direction.SOUTHWEST,
        Direction.WEST,
        Direction.NORTHWEST,
    };

    enum hq_states {
        INITIAL,
        SCOUT,
        RESOURCE,
        TASK
    }

    enum states {
        INITIAL,
        ATTACK,
        ANCHOR,
        OCCUPY,
        GROUP,
        SCOUT,
        ADAMANTIUM,
        MANA,
        ELIXIR,
        DEFAULT
    }

    enum stance {
        RUSH,
        AGGRESSIVE,
        CAUTIOUS,
        RETREAT
    }

    enum map_tiles{
        UNKNOWN,
        PLAIN,
        ADAMANTIUM,
        MANA,
        ELIXIR,
        WALL,
        CLOUD,
        HQ_ENEMY,
        HQ_FRIENDLY,
        CURRENT_N,
        CURRENT_NE,
        CURRENT_E,
        CURRENT_SE,
        CURRENT_S,
        CURRENT_SW,
        CURRENT_W,
        CURRENT_NW,
        ISLAND_NEUTRAL
    }

    static MapLocation birth_location;
    /**
     * run() is the method that is called when a robot is instantiated in the Battlecode world.
     * It is like the main function for your robot. If this method returns, the robot dies!
     *
     * @param rc  The RobotController object. You use it to perform actions from this robot, and to get
     *            information on its current status. Essentially your portal to interacting with the world.
     **/
    @SuppressWarnings("unused")
    public static void run(RobotController rc) throws GameActionException {
//        System.out.println("Start of RobotPlayer bc remaining:" + Clock.getBytecodesLeft());


        // Hello world! Standard output is very useful for debugging.
        // Everything you say here will be directly viewable in your terminal when you run a match!
        // System.out.println("I'm a " + rc.getType() + " and I just got created! I have health " + rc.getHealth());

        // You can also use indicators to save debug notes in replays.
        rc.setIndicatorString("Hello world!");

        // keep track of where we started so carriers can return Resources
        MapLocation birth_location = rc.getLocation();

        // Initialize myLastLocation to have a value
        myLastLocation = birth_location;

        // Initialize clockwise preference randomly
        prefersClockwise = (rc.getID() % 2) == 1;


        // Initialize myHealthLastTurn
        myHealthLastTurn = rc.getHealth();

        // Initialize my_state
        my_state = states.INITIAL;

        // Too much bytecode, and we didn't need every island anyway.
//        // teamKnownIslandLocations needs to be initialized to work.
//        for (int i = 0; i < GameConstants.MAX_NUMBER_ISLANDS + 1; i++){
//            HashSet<MapLocation> islandLocations = new HashSet<MapLocation>(GameConstants.MAX_ISLAND_AREA);
//            teamKnownIslandLocations.add(islandLocations);
//        }
//        System.out.println("Start of RobotPlayer while loop bc remaining: " + Clock.getBytecodesLeft());

        while (true) {
            // This code runs during the entire lifespan of the robot, which is why it is in an infinite
            // loop. If we ever leave this loop and return from run(), the robot dies! At the end of the
            // loop, we call Clock.yield(), signifying that we've done everything we want to do.

            turnCount += 1;  // We have now been alive for one more turn!

            didMoveLastTurn = false; // We did not yet moved this turn


            // Try/catch blocks stop unhandled exceptions, which cause your robot to explode.
            try {
                // The same run() function is called for every robot on your team, even if they are
                // different types. Here, we separate the control depending on the RobotType, so we can
                // use different strategies on different robots. If you wish, you are free to rewrite
                // this into a different control structure!
                switch (rc.getType()) {
                    case HEADQUARTERS: Headquarters.runHeadquarters(rc);  break;
                    case CARRIER:      CarrierStrategy.run(rc);   break;
                    case LAUNCHER:     Launcher.runLauncher(rc); break;
                    case BOOSTER: // Examplefuncsplayer doesn't use any of these robot types below.
                    case DESTABILIZER: // You might want to give them a try!
                    case AMPLIFIER:     Amplifier.runAmplifier(rc);   break;
                }

                myHealthLastTurn = rc.getHealth();
                myLastLocation = rc.getLocation();
                myCurrentLocation = myLastLocation;

            } catch (GameActionException e) {
                // Oh no! It looks like we did something illegal in the Battlecode world. You should
                // handle GameActionExceptions judiciously, in case unexpected events occur in the game
                // world. Remember, uncaught exceptions cause your robot to explode!
                System.out.println(rc.getType() + " Exception");
                e.printStackTrace();

            } catch (Exception e) {
                // Oh no! It looks like our code tried to do something bad. This isn't a
                // GameActionException, so it's more likely to be a bug in our code.
                System.out.println(rc.getType() + " Exception");
                e.printStackTrace();

            } finally {
                // Signify we've done everything we want to do, thereby ending our turn.
                // This will make our code wait until the next turn, and then perform this loop again.
                Clock.yield();
            }
            // End of loop: go back to the top. Clock.yield() has ended, so it's time for another turn!
        }

        // Your code should never reach here (unless it's intentional)! Self-destruction imminent...
    }

    ///   **********************
    //    ***   MAP  STUFF   ***
    //    **********************
    static void set_map_location_tile(MapLocation location, map_tiles tile_type){
        int x = location.x;
        int y = location.y;
        int tile = tile_type.ordinal();
        map[x][y]=tile;
    }
    static boolean is_unknown(MapLocation location){
        int x = location.x;
        int y = location.y;
        return map[x][y] == 0;
    }
    static map_tiles get_map_location_tile(MapLocation location){
        int x = location.x;
        int y = location.y;
        return map_tiles.values()[map[x][y]];
    }

    static void process_array_islands(RobotController rc) throws GameActionException {
        for(int i = anchor_decay.Comms.index_island; i < anchor_decay.Comms.index_last_island ; i++){
            int this_island_id;
            MapLocation this_island_location;

            boolean is_location = anchor_decay.Comms.is_location(rc.readSharedArray(i));
            int id_offset = is_location ? 1 : 0;
            this_island_id = anchor_decay.Comms.get_island_id(rc.readSharedArray(i + id_offset));

            if(island_ids.contains(this_island_id) & rc.getType().equals(RobotType.HEADQUARTERS)){
                anchor_decay.Comms.clear_island_location(rc, i, is_location);
            }else {
                this_island_location = anchor_decay.Comms.get_MapLocation(rc.readSharedArray(i));
                if(is_location){
                    island_ids.add(this_island_id);
                    island_locations[this_island_id] = this_island_location;
                }
            }
            if(is_location){i++;} // skip the details word if this was a new island notification
        }
    }

    static void process_array_wells(RobotController rc) throws GameActionException{
        for(int i = anchor_decay.Comms.index_last_well; i >= anchor_decay.Comms.index_well; i--){
            MapLocation this_well_location;
            ResourceType this_well_type;

            int raw_well_data = rc.readSharedArray(i);
            this_well_location = anchor_decay.Comms.get_MapLocation(raw_well_data);
            this_well_type = anchor_decay.Comms.get_well_type(raw_well_data);

            if(known_well(this_well_location)& rc.getType().equals(RobotType.HEADQUARTERS)){
                Comms.clear_well(rc, i);
            }
            else{
                map[this_well_location.x][this_well_location.y] = this_well_type.ordinal();
                well_locations.add(this_well_location);
            }

        }
    }

    static boolean known_well(MapLocation location){
        int map_type = map[location.x][location.y];
        return map_type == map_tiles.ADAMANTIUM.ordinal() || map_type == map_tiles.MANA.ordinal() || map_type == map_tiles.ELIXIR.ordinal();
    }

    // find closest movable direction
    static Direction movable_direction(RobotController rc, Direction desired_dir){
        return Pathing.getClosestValidMoveDirection(rc, desired_dir);
//        if(rc.canMove(desired_dir)) return desired_dir;
//        for (int rotation_offset = 1; rotation_offset <= 4; rotation_offset++){  // 4 is 1/2 of the 8 possible directions
//            Direction left_dir = Direction.values()[(desired_dir.ordinal() +  rotation_offset) % 8];
//            Direction right_dir = Direction.values()[(desired_dir.ordinal() + 8 - rotation_offset) % 8];
//            if (rc.canMove(left_dir)) return left_dir;
//            if (rc.canMove(right_dir)) return right_dir;
//        }
//        return Direction.CENTER;
    }

    // methods to retrieve packed information
    static MapLocation unpackMapLocation(int target) {
        int target_x = target >>> 10;
        int target_y = (target & 0x03f0) >>> 4;
        return new MapLocation(target_x,target_y);
    }

    static int unpackExtra(int target) {
        return target & 0x000f;
    }

    static ResourceType unpackResource(int target){
        return ResourceType.values()[unpackExtra(target)];
    }

    static int packMapLocation(MapLocation location) {
        int x = location.x;
        int y = location.y;
        return (x << 6) & y;
    }

    static int packMapLocationExtra(MapLocation here, int extra) {
        int x = here.x;
        int y = here.y;
        return (x << 10) + (y << 4) + extra;
    }

    static int get_HQ_array_index(RobotController rc) throws GameActionException {
        RobotInfo[] bots = rc.senseNearbyRobots();
        for(RobotInfo bot : bots){
            if(bot.getType() == RobotType.HEADQUARTERS){
                for(int i=0; i<4; i++){
                    int array_i = rc.readSharedArray(i);
                    MapLocation array_loc = unpackMapLocation(array_i);
                    if(bot.getLocation().equals(array_loc)){
                        return i;
                    }
                }
            }
        }
        return 99;
    }


}
