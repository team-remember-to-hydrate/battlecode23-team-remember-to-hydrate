package hydr8player.v1.Carrier.v2;

import static hydr8player.v1.Carrier.v2.RobotPlayer.runCarrierGatherResources;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

import battlecode.common.GameActionException;
import battlecode.common.RobotController;
import battlecode.common.WellInfo;
import org.junit.Test;

/**
 * author: hydr8
 * Title: Carrier II
 * Summary: Greedy Best First Carrier + Initial HQ Direction
 * Rationale: Enhanced Ad and Mana Growth.
 * Explanation:
 *  (HQ, Carrier)
 *  Have my carriers effectively path find to the initially sensed well (usually Ad) and back to HQ.
 *  This will reduce meandering and increase growth of that first sensed resource, however,
 *  this alone might reduce growth of the opposite resource (Mana if first sensed well is Ad).
 *  A minimum effort way to mitigate this risk is to mix carrier exploration strategies (Carrier I strat to
 *   find Ad + Mn around the map, plus a few carriers using Carrier II to squeeze more growth out of that
 *   initially sensed well (usually Ad)).
 *
 *   [ ] hydr8 is currently implementing
 */

public class CarrierTest {
    @Test
    public void testCarrierGatherResourcesStateDefault() {
        CarrierGatherResourcesState carrierGatherResourcesState = new CarrierGatherResourcesState();
        assert(carrierGatherResourcesState.getFoundWell() == null);
        assert(carrierGatherResourcesState.getCurrentState() == CarrierGatherResourcesState.State.SEARCHING_FOR_WELL);
    }

    @Test
    public void testCarrierBeginsRetrievingResourcesWhenWellIsFound() throws GameActionException {
        // given
        RobotController mockedRc = mock(RobotController.class);
        WellInfo mockedWellInfo = mock(WellInfo.class);
        CarrierGatherResourcesState state = new CarrierGatherResourcesState();
        state.setCurrentState(CarrierGatherResourcesState.State.SEARCHING_FOR_WELL);
        state.setFoundWell(mockedWellInfo);

        // when
        runCarrierGatherResources(mockedRc, state);

        // then
        assertEquals(state.getCurrentState(), CarrierGatherResourcesState.State.RETRIEVING_RESOURCE);
    }

    @Test
    public void testCarrierContinuesSearchingWhenWellIsNotFound() throws GameActionException {
        // given
        RobotController mockedRc = mock(RobotController.class);
        CarrierGatherResourcesState state = new CarrierGatherResourcesState();
        state.setCurrentState(CarrierGatherResourcesState.State.SEARCHING_FOR_WELL);
        state.setFoundWell(null);

        // when
        runCarrierGatherResources(mockedRc, state);

        // then
        assertEquals(state.getCurrentState(), CarrierGatherResourcesState.State.SEARCHING_FOR_WELL);
    }
}
