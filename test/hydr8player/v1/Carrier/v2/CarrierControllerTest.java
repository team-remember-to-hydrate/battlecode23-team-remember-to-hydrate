package hydr8player.v1.Carrier.v2;

import hydr8player.v1.Carrier.v2.capabilities.RunRetrieveResources;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

import battlecode.common.GameActionException;
import battlecode.common.RobotController;
import battlecode.common.WellInfo;

import hydr8player.v1.Carrier.v2.capabilities.RunSearchForWell;

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

/**
 * CarrierControllerTest verifies Carrier state transitions occur only when expected
 * These tests do NOT verify what capabilities run
 * These tests do NOT stub rc, they only assert state transitions given state + strategy
 */

public class CarrierControllerTest {
    /**
     * CarrierController SEARCHING_FOR_WELL
     */
    @Test
    public void testCarrierBeginsRetrievingResourcesWhenWellIsFound() throws GameActionException {
        // setup
        RobotController mockedRc = mock(RobotController.class);
        RunSearchForWell mockedRunSearchForWell = mock(RunSearchForWell.class);
        RunRetrieveResources mockedRunRetrieveResources = mock(RunRetrieveResources.class);
        CarrierController cc = new CarrierController(mockedRunSearchForWell, mockedRunRetrieveResources);

        // given
        CarrierState state = new CarrierState(CarrierState.State.SEARCHING_FOR_WELL);
        state.setFoundWell(mock(WellInfo.class));
        // when
        cc.run(mockedRc, state);
        // then
        assertEquals(state.getCurrentState(), CarrierState.State.RETRIEVING_RESOURCES);
    }

    @Test
    public void testCarrierContinuesSearchingWhenWellIsNotFound() throws GameActionException {
        // setup
        RobotController mockedRc = mock(RobotController.class);
        RunSearchForWell mockedRunSearchForWell = mock(RunSearchForWell.class);
        RunRetrieveResources mockedRunRetrieveResources = mock(RunRetrieveResources.class);
        CarrierController cc = new CarrierController(mockedRunSearchForWell, mockedRunRetrieveResources);

        // given
        CarrierState state = new CarrierState(CarrierState.State.SEARCHING_FOR_WELL);
        state.setFoundWell(null);
        // when
        cc.run(mockedRc, state);
        // then
        assertEquals(CarrierState.State.SEARCHING_FOR_WELL, state.getCurrentState());
    }

    /**
     * CarrierController RETRIEVING_RESOURCE
     */
    @Test
    public void testCarrierBeginsDeliveringToHqWhenItIsAtCarryingCapacity() throws GameActionException {
        // setup
        RobotController mockedRc = mock(RobotController.class);
        RunSearchForWell mockedRunSearchForWell = mock(RunSearchForWell.class);
        RunRetrieveResources mockedRunRetrieveResources = mock(RunRetrieveResources.class);
        CarrierController cc = new CarrierController(mockedRunSearchForWell, mockedRunRetrieveResources);

        // given
        CarrierState state = new CarrierState(CarrierState.State.RETRIEVING_RESOURCES);
        state.setFoundWell(mock(WellInfo.class));
        state.setIsAtHoldingCapacity(true);
        // when
        cc.run(mockedRc, state);
        // then
        assertEquals(CarrierState.State.DELIVERING_TO_HQ, state.getCurrentState());
    }

    @Test
    public void testCarrierContinuesRetrievingResourcesWhenItIsNotAtCarryingCapacity() throws GameActionException {
        // setup
        RobotController mockedRc = mock(RobotController.class);
        RunSearchForWell mockedRunSearchForWell = mock(RunSearchForWell.class);
        RunRetrieveResources mockedRunRetrieveResources = mock(RunRetrieveResources.class);
        CarrierController cc = new CarrierController(mockedRunSearchForWell, mockedRunRetrieveResources);

        // given
        CarrierState state = new CarrierState(CarrierState.State.RETRIEVING_RESOURCES);
        state.setFoundWell(mock(WellInfo.class));
        state.setIsAtHoldingCapacity(false);
        // when
        cc.run(mockedRc, state);
        // then
        assertEquals(CarrierState.State.RETRIEVING_RESOURCES, state.getCurrentState());
    }

    /**
     * CarrierController DELIVERING_TO_HQ
     */
    @Test
    public void testCarrierBeginsRetrievingResourcesWhenItHasCarryingCapacityAndFoundWell() throws GameActionException {
        // setup
        RobotController mockedRc = mock(RobotController.class);
        RunSearchForWell mockedRunSearchForWell = mock(RunSearchForWell.class);
        RunRetrieveResources mockedRunRetrieveResources = mock(RunRetrieveResources.class);
        CarrierController cc = new CarrierController(mockedRunSearchForWell, mockedRunRetrieveResources);

        // given
        CarrierState state = new CarrierState(CarrierState.State.RETRIEVING_RESOURCES);
        state.setFoundWell(mock(WellInfo.class));
        state.setIsAtHoldingCapacity(false);
        // when
        cc.run(mockedRc, state);
        // then
        assertEquals(CarrierState.State.RETRIEVING_RESOURCES, state.getCurrentState());
    }
}
