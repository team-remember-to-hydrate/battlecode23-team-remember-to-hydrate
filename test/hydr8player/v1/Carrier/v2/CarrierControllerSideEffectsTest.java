package hydr8player.v1.Carrier.v2;

import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import battlecode.common.WellInfo;
import hydr8player.v1.Carrier.v2.capabilities.*;
import org.junit.Test;

import static org.mockito.Mockito.*;

/**
 * author: hydr8
 * these tests ensure a Carrier runs the expected capabilities when it is in a specific state
 * these tests do NOT specify what those capabilities do
 */
public class CarrierControllerSideEffectsTest {
    /**
     * CarrierController SEARCHING_FOR_WELL
     */
    @Test
    public void testCarrierRunFindWellCapability() throws GameActionException {
        // setup
        RobotController mockedRc = mock(RobotController.class);
        RunFindWell mockedRunFindWell = mock(RunFindWell.class);
        RunRetrieveResources mockedRunRetrieveResources = mock(RunRetrieveResources.class);
        RunDeliverToHq mockedRunDeliverToHq = mock(RunDeliverToHq.class);
        RunPathingToWell mockedRunPathingToWell = mock(RunPathingToWell.class);
        RunPathingToHq mockedRunPathingToHq = mock(RunPathingToHq.class);

        CarrierController cc = new CarrierController(
                mockedRunFindWell,
                mockedRunRetrieveResources,
                mockedRunDeliverToHq,
                mockedRunPathingToWell,
                mockedRunPathingToHq
        );

        // given
        CarrierState state = new CarrierState(CarrierState.State.SEARCHING_FOR_WELL);
        when(mockedRc.getLocation()).thenReturn(new MapLocation(0, 0)); // mock to avoid null pointer
        when(mockedRc.isActionReady()).thenReturn(true); // mock to avoid null pointer

        // when
        cc.run(mockedRc, state);

        // then ensure only correct capability runs
        verify(mockedRunPathingToWell).run(mockedRc, state); // side effect
        verify(mockedRunFindWell).run(mockedRc, state); // side effect
        verify(mockedRunRetrieveResources, never()).run(mockedRc, state); // side effect
    }

    /**
     * CarrierController RETRIEVING_RESOURCE
     */
    @Test
    public void testCarrierRunRetrieveResourcesCapability() throws GameActionException {
        // setup
        RobotController mockedRc = mock(RobotController.class);
        RunFindWell mockedRunFindWell = mock(RunFindWell.class);
        RunRetrieveResources mockedRunRetrieveResources = mock(RunRetrieveResources.class);
        RunDeliverToHq mockedRunDeliverToHq = mock(RunDeliverToHq.class);
        RunPathingToWell mockedRunPathingToWell = mock(RunPathingToWell.class);
        RunPathingToHq mockedRunPathingToHq = mock(RunPathingToHq.class);

        CarrierController cc = new CarrierController(
                mockedRunFindWell,
                mockedRunRetrieveResources,
                mockedRunDeliverToHq,
                mockedRunPathingToWell,
                mockedRunPathingToHq
        );

        // given
        WellInfo mockedFoundWell = mock(WellInfo.class);
        CarrierState state = new CarrierState(CarrierState.State.RETRIEVING_RESOURCES);
        when(mockedRc.getLocation()).thenReturn(new MapLocation(0, 0)); // mock to avoid null pointer
        when(mockedRc.isActionReady()).thenReturn(true); // mock to avoid null pointer

        state.setWell(mockedFoundWell);

        // when
        cc.run(mockedRc, state);

        // then ensure only correct capability runs
        verify(mockedRunPathingToWell).run(mockedRc, state);
        verify(mockedRunRetrieveResources).run(mockedRc, state);
        verify(mockedRunPathingToHq, never()).run(mockedRc, state);
        verify(mockedRunFindWell, never()).run(mockedRc, state);
    }

    /**
     * CarrierController DELIVERING_TO_HQ
     */
    @Test
    public void testCarrierRunDeliverToHqCapability() throws GameActionException {
        RobotController mockedRc = mock(RobotController.class);
        RunFindWell mockedRunFindWell = mock(RunFindWell.class);
        RunRetrieveResources mockedRunRetrieveResources = mock(RunRetrieveResources.class);
        RunDeliverToHq mockedRunDeliverToHq = mock(RunDeliverToHq.class);
        RunPathingToWell mockedRunPathingToWell = mock(RunPathingToWell.class);
        RunPathingToHq mockedRunPathingToHq = mock(RunPathingToHq.class);
        CarrierController cc = new CarrierController(
                mockedRunFindWell,
                mockedRunRetrieveResources,
                mockedRunDeliverToHq,
                mockedRunPathingToWell,
                mockedRunPathingToHq
        );

        // given
        CarrierState state = new CarrierState(CarrierState.State.DELIVERING_TO_HQ);
        state.setHqLoc(new MapLocation(0,0));
        state.setWell(mock(WellInfo.class));

        // when
        cc.run(mockedRc, state);

        // then ensure only correct capability runs
        verify(mockedRunPathingToWell, never()).run(mockedRc, state);
        verify(mockedRunRetrieveResources, never()).run(mockedRc, state);
        verify(mockedRunFindWell, never()).run(mockedRc, state);
        verify(mockedRunPathingToHq).run(mockedRc, state);
        verify(mockedRunDeliverToHq).run(mockedRc, state);
    }
}
