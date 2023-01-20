package mike_comm_system;

public class Comms {

/**
 * Comms Array layout:
 * We are given an array of size 64. Each index contains a Word (2-bytes, 16 bits).
 *
 * [Index] - Data Contained (Details in format Field 1: details; Field 2: details)
 *
 * [0-3] - HQ Updates (Info; Location, *possibly round-dependent, see below)
 * [4-10] - Well Updates (Type; Upgraded bit; Location)
 * [11-30] - Island Updates (Location; or Details *see below)
 * [31-36] - Obstacle Updates (Type:currents, clouds, storms/walls; Location)
 * [37] - Enemy HQ Updates (Econ Unit Count; Combat Unit Count; Location)
 * [38] - Carrier Announcing Newly Acquired Anchor (Anchor Type; Location)
 * [39] - Unused (yet)
 * [40-59] - Orders from HQ or Amplifier, order is Group Membership Assignment or Group Task Assignment
 *      Membership Assignment:
 *      Task Assignment:
 * [60-61] - Amplifier or HQ reporting enemy strength at location (technically usable by any unit, not just Amps/HQs).
 * [62-64] - Unused (yet)
 */

/**
 * Below are detailed explanations.
 * Some general rules that might help make methods reusable:
 * - Locations should always be at the end of the bit-packed Word. They should be made compatible with Josh's
 * Map Location implementation and all zeros should not be a valid map location.
 * - We will have a 'unit count partitions' array declared at the top. It will help us convey enemy numbers strength
 * with less bit usage. Details below.
 *
 * General Heuristics Using Comms:
 * We want to minimize declaring info about features in sight of a headquarters. Any spawned unit can see those features
 * immediately, and they would clog up the comm array. Please check distance from nearest HQ of a feature before
 * announcing it. Use your judgement for exceptions (enemy forces).
 * Information should be prioritized. Maybe the last 2 slots in each category should be reserved for info that's at
 * least as 'important' as anything already in that type of array slot.
 *
 * Format:
 * Name of Index Type:
 * [Field 1] [Field 2] [Field 3] ... [Field N]
 * [Field 1 bit count] ... [Field N bit count]
 */

    /**
     * Count Partitions:
     * These are used so we can communicate a general idea of a number, using less bits.
     * Useful to send a count of enemy combatants visible at a location.
     * Each index corresponds to its 2-bit binary representation (00 = index 0, 01 = index 1, ..., 11 = index 3).
     * The value at that index is the minimum 'count' represented by these 2 bits.
     * So, for example, if a location is provided with a count bits value of 10, we know that the true value of
     * whatever was counted at that location is at minimum 3 (index b10 below) and must be less than 6 (value at index
     * b11).
     * We use this because often we don't care about the exact amount of something, but a general understanding of the
     * situation. This breaks the enemy count down to categories of 0 enemies, 1-2 enemies, 3-4 enemies, or 6+.
     */
    public final static int[] countPartitions = {0,1,3,6};

/**
 * HQ Updates:
 * Simple Implementation:
 * [hasAdamantiumVisible] [hasManaVisible] [enemy combatant countPartition] [location]
 * [1 bit] [1 bit] [2 bits] [12 bits]
 *
 * The simple implementation is to keep this comm index format static throughout the game. HQs fill it out on turn 1 on
 * a first-space-available basis and record their space as their assigned space. If we deem it important, the HQs could
 * instead use the first 4 bits to give their ID number (I believe HQs are numbered 1-8, with one team having even vs
 * odd numbering).
 *
 * Advanced Implementation:
 * If we decide there is little value for other bots to know about HQs (besides the other HQs knowing), we can have
 * the format above persist for the first turn only (so HQs can learn of one another). After that, each HQ will wipe its
 * own space and can use this space to broadcast one of the following:
 * 1. Requests to itself for resources or assistance.
 * 2. General size of its pools of each resource.
 * 3. HQs could receive orders given to their respective index.
 *
 *
 */

/**
 * Well Updates:
 * [wellIsUpgraded Status] [wellType] [Unused] [Location]
 * [1] [2] [1] [12]
 *
 * WellIsUpgraded - 1 if upgraded, 0 if not
 * WellType - 10 for Elixer, 01 for Mana, 00 for Adamantium
 * Location - 12 bit location
 *
 * Wells that are discovered are added to the array. If we later implement priorities of updates, the order
 * of information was selected to allow easy priority comparison.
 */

/**
 * Island Updates:
 * Island updates take up to 2 indices depending on whether location, details, or both are included.
 *
 * Location Update:
 * [isLocation] [friendly-owned] [visible combatStrengthOfOwner countPartition] [location]
 * [1] [1] [2] [12]
 *
 * isLocation - 1 if this is a bitpacked island location rather than an island detail
 * friendlyOwned - 1 if we have captured this island
 * combatStrengthOfOwner - uses countPartition to show strength of controlling forces.
 * location - 12-bit location
 *
 * Detail Update:
 * [isLocation] [islandID number] [friendlyAnchorCarrierPresent] [friendlyCombatStrength] [enemyCombatStrength]
 * [1] [6] [1] [2] [2]
 *
 * isLocation - Must have value 0 to represent details, since 1 represents an island Location Update
 * islandID - direct binary representation of the island ID which as of Version 2.0 is understood to be between 4 and 35
 * friendlyAnchorCarrierPresent - 1 if a friendly anchor carrier is visible. Can default to 0. Useful to know if we
 * accidentally sent 2 anchors to an island and now the extra needs re-assignment.
 *
 * Details are always assumed to be associated with the island location above them. For this reason, we only have room for 10
 * island updates per round. There are clever ways around this (assign even/odd indices for locations. If something is
 * in a wrong location or paired with a mis-matched type, then assume they are independent 'lone' updates).
 *
 * Bots thinking about updating this should check the detail islandID fields to see if they can update an existing
 * entry and save space while providing an updated value.
 *
 *
 *
 */

/**
 * Obstacle Updates:
 * [isWall] [Unused, for now] [Location]
 * [1] [3] [12]
 *
 * isWall - 1 if the obstacle is a wall and is being reported.
 * location - 12-bit location
 *
 * This needs some rework. If we want to have cloud-dependent strategies, we need to signal that.
 * Currents would take more details to convey their direction. Possibly a 2-word system or separate current tracker
 * index.
 */

/**
 * Enemy HQ Updates:
 * [enemyEconUnitCount countPartition] [enemyCombatUnitCount countPartition] [location]
 * [2] [2] [12]
 *
 * enemyEconUnitCount - a countPartition representation of sighted carriers + boosters
 * enemyCombatUnitCount - a countPartition representation of sighted launchers + destabilizers.
 * location - 12-bit location
 *
 * Only 1 slot for this. We usually won't care much since we can determine location with map symmetry and our units
 * there won't last long as of V2.0 so there won't be a lot of competition to announce this.
 *
 */

/**
 * Carrier Announce Anchor Possession
 * [anchorIsAccelerating] [hasNoValidIslandInSight] [Unused] [location]
 * [1] [1] [4] [12]
 *
 * anchorIsAccelerating - 1 if the anchor held is accelerating
 * hasNoValidIslandInSight - 1 if carrier can't see a valid island plant location
 *
 * If for any reason a carrier has newly received an anchor, and especially if it doesn't know where to go, it needs to
 * announce that it has an anchor so that HQ can use an Order to send it to a location or safety.
 *
 * How this might happen: Carriers are passing anchors via 'bucket brigade' or a recently injured carrier passes it to
 * a healthier carrier.
 */

/**
 * Orders from HQ or Amplifier:
 * 2 types of Order:
 * 1. Group Membership Assignment - Orders all bots meeting the criteria at a location to join a group.
 * 2. Group Task Assignment - Orders all bots who are members of a group to complete a task.
 *
 * Each type of order is 2 words (2 comms indices). To assign a group and order them in one round requires 4 words.
 * Existing groups can be given task orders without re-assigning membership in subsequent rounds.
 * ________________________________________________________________
 *
 * Membership Assignment order:
 * Word 1:
 * [isTaskAssignment] [groupIDNumber] [botTypes] [shouldOverrideExistingMembership]
 * [existingMembersOutsideRadiusShouldUnassignMembership] [extraSelectionRadius]
 * [1] [4] [3] [1] [1] [2]
 *
 * isTaskAssignment - 1 if this is a Task Assignment Order, 0 if it is Group Assignment
 * groupIDNumber - The ID number of the group being assigned members
 * botTypes - [0,1,2,3,4,5,6,7] for [all (except HQ), carrier, launcher, amplifier, destabilizer, booster, unused]
 * shouldOverrideExistingMembership - 1 if any qualifying bot should join even if it already has a group membership
 * existingMembersOutsideRadiusShouldUnassignMembership - 1 if bots already assigned to this group number but outside
 *      the selection zone should drop their group membership (and thereby not be included in the future group orders).
 * extraSelectionRadius - two bits to be binary appended to selection radius (below) if we want a huge selection area
 *
 * Word 2:
 * [selectionRadius] [location]
 * [4] [12]
 *
 * selectionRadius - binary representation of intended selection radius from location
 * location - 12-bit location
 *
 * TODO: Discuss this:
 * The group number bits could be freed up if we used the comm array index to signify group. This would let us have
 * more bits for botTypes and select multiple types from predefined groups (destabilizer plus launchers).
 * ________________________________________________________
 *
 * Task Assignment order:
 * Word 1:
 * [isTaskAssignment] [groupIDNumber] [extra task info]
 * [1] [4] [11]
 *
 * isTaskAssignment - 1 if this is a Task Assignment Order, 0 if it is Group Assignment
 * groupIDNumber - The ID number of the group being assigned members
 * extraInfo - We can use this to designate behaviors of the ordered units. We might have a pattern for mining and what
 * type of mine to harvest, a pattern telling carriers to 'feed' the mine at the location, something stating whether to
 * move cautiously or ignore enemy fire, etc.
 *
 * Word 2:
 * [extra bits] [location]
 *
 * location - 12-bit location
 *
 * TODO: Discuss this:
 * If we only have 8 'types' of task assignments, we can get this down to a single Word. I anticipate we might want more
 * information and details, which requires the second word. Maybe that's a premature move to make at this point.
 *
 *
 */

/**
 * Amplifier Announce Enemy Strength At Location:
 * [enemyEconUnitCount countPartition] [enemyCombatUnitCount countPartition] [location]
 * [2] [2] [12]
 * enemyEconUnitCount - a countPartition representation of sighted carriers + boosters
 * enemyCombatUnitCount - a countPartition representation of sighted launchers + destabilizers.
 * location - 12-bit location
 */
}
