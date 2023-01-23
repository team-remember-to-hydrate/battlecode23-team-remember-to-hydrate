package latest;

import battlecode.common.*;

import java.util.ArrayList;
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
    static Direction lastMoved = Direction.NORTH;
    static boolean didMoveLastTurn = false;
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
    static ArrayList<ArrayList<MapLocation>> teamKnownIslandLocations =
            new ArrayList<ArrayList<MapLocation>>(GameConstants.MAX_NUMBER_ISLANDS + 1);
    static ArrayList<Integer> myIslandFullInfoBroadcastQueue = new ArrayList<>();
    static ArrayList<Integer> myIslandDetailsBroadcastQueue = new ArrayList<>();
    static ArrayList<Short> myWellBroadcastQueue = new ArrayList<>();



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

        // Hello world! Standard output is very useful for debugging.
        // Everything you say here will be directly viewable in your terminal when you run a match!
        // System.out.println("I'm a " + rc.getType() + " and I just got created! I have health " + rc.getHealth());

        // You can also use indicators to save debug notes in replays.
        rc.setIndicatorString("Hello world!");

        // keep track of where we started so carriers can return Resources
        MapLocation birth_location = rc.getLocation();

        // Initialize myLastLocation to have a value
        myLastLocation = birth_location;

        // teamKnownIslandLocations needs to be initialized to work.
        for (int i = 0; i < GameConstants.MAX_NUMBER_ISLANDS + 1; i++){
            ArrayList<MapLocation> islandLocations = new ArrayList<MapLocation>(GameConstants.MAX_ISLAND_AREA);
            teamKnownIslandLocations.add(islandLocations);
        }


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
    static void set_map_location(MapLocation location, map_tiles tile_type){
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
    static map_tiles get_map_location(MapLocation location){
        int x = location.x;
        int y = location.y;
        return map_tiles.values()[map[x][y]];
    }

    // find closest movable direction
    static Direction movable_direction(RobotController rc, Direction desired_dir){
        if(rc.canMove(desired_dir)) return desired_dir;
        for (int rotation_offset = 1; rotation_offset <= 4; rotation_offset++){  // 4 is 1/2 of the 8 possible directions
            Direction left_dir = Direction.values()[(desired_dir.ordinal() +  rotation_offset) % 8];
            Direction right_dir = Direction.values()[(desired_dir.ordinal() + 8 - rotation_offset) % 8];
            if (rc.canMove(left_dir)) return left_dir;
            if (rc.canMove(right_dir)) return right_dir;
        }
        return Direction.CENTER;
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
