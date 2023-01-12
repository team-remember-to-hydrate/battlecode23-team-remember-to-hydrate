package hydr8player.v1.Carrier.v2;

import battlecode.common.GameActionException;
import battlecode.common.RobotController;
import battlecode.common.WellInfo;
import hydr8player.v1.Carrier.v2.capabilities.RunRetrieveResources;
import hydr8player.v1.Carrier.v2.capabilities.RunSearchForWell;
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
    public void testCarrierRunSearchForWellCapability() throws GameActionException {
        // setup
        RobotController mockedRc = mock(RobotController.class);
        RunSearchForWell mockedRunSearchForWell = mock(RunSearchForWell.class);
        RunRetrieveResources mockedRunRetrieveResources = mock(RunRetrieveResources.class);
        CarrierController cc = new CarrierController(mockedRunSearchForWell, mockedRunRetrieveResources);

        // given
        CarrierState state = new CarrierState(CarrierState.State.SEARCHING_FOR_WELL);
        // when
        cc.run(mockedRc, state);

        // then ensure only correct capability runs
        verify(mockedRunSearchForWell).run(mockedRc, state); // side effect
        verify(mockedRunRetrieveResources, never()).run(mockedRc, state); // side effect
    }

    /**
     * CarrierController RETRIEVING_RESOURCE
     */
    @Test
    public void testCarrierRunRetrieveResourcesCapability() throws GameActionException {
        // setup
        RobotController mockedRc = mock(RobotController.class);
        RunSearchForWell mockedRunSearchForWell = mock(RunSearchForWell.class);
        RunRetrieveResources mockedRunRetrieveResources = mock(RunRetrieveResources.class);
        CarrierController cc = new CarrierController(mockedRunSearchForWell, mockedRunRetrieveResources);

        // given
        WellInfo mockedFoundWell = mock(WellInfo.class);
        CarrierState state = new CarrierState(CarrierState.State.RETRIEVING_RESOURCE);
        state.setFoundWell(mockedFoundWell);

        // when
        cc.run(mockedRc, state);

        // then ensure only correct capability runs
        verify(mockedRunSearchForWell, never()).run(mockedRc, state);
        verify(mockedRunRetrieveResources).run(mockedRc, state);
    }

    /**
     * CarrierController RETURNING_TO_HQ
     */

    // TODO: 1/12/2023
}
