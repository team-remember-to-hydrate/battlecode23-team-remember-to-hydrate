package sprint_1.CarrierV2;

import battlecode.common.MapLocation;
import org.junit.Test;
import static org.mockito.Mockito.*;

import battlecode.common.GameActionException;
import battlecode.common.RobotController;

import org.mockito.InOrder;
import org.mockito.verification.VerificationMode;

public class CarrierTest {
    static RobotController rc = null;
    static CarrierController cc = null;
    static Carrier mockCarrier = null;
    static InOrder ops = null;
    static void setup(){
        rc = mock(RobotController.class);
        cc = new CarrierController();
        mockCarrier = mock(Carrier.class);
        ops = inOrder(mockCarrier);
    }

    static Carrier op(){
        return ops.verify(mockCarrier, times(1));
    }

    static Carrier op(VerificationMode v){ // op(never())
        return ops.verify(mockCarrier, v);
    }

    static void noMore(){
        verifyNoMoreInteractions(mockCarrier);
    }

    @Test
    public void ShouldSearchForWellWithRandomMovementByDefault() throws GameActionException {
        setup();

        mockCarrier.hqLoc = new MapLocation(3, 3);
        cc.run(rc, mockCarrier);

        op().searchForWell(rc);
        op().moveRandom(rc);
        noMore();
    }

    @Test
    public void ShouldSearchForHqByDefault() throws GameActionException {
        setup();

        cc.run(rc, mockCarrier);

        op().searchForHq(rc);
        noMore();
    }

    @Test
    public void ShouldTravelToWellWithBugNavWhenItCanHoldResourcesAndIsNotNextToWell() throws GameActionException {
        setup();

        mockCarrier.hqLoc = new MapLocation(3, 3);
        mockCarrier.wellLoc = new MapLocation(0, 0);
        mockCarrier.amountResourcesHeld = 0;
        when(rc.getLocation()).thenReturn(new MapLocation(2, 2)); // not adjacent
        cc.run(rc, mockCarrier);

        op().moveWithBugNav(rc, mockCarrier.wellLoc);
        noMore();
    }

    @Test
    public void ShouldStopAndCollectResourcesWhenAtWell() throws GameActionException {
        setup();

        mockCarrier.hqLoc = new MapLocation(3,3);
        mockCarrier.wellLoc = new MapLocation(0, 0);
        when(rc.getLocation()).thenReturn(new MapLocation(1, 1));
        when(rc.canCollectResource(any(), anyInt())).thenReturn(true);
        cc.run(rc, mockCarrier);

        op().tryCollectResources(rc, mockCarrier.wellLoc);
        noMore();
    }

    @Test
    public void ShouldTravelToHqWhenResourceCapacityIsFull() throws GameActionException {
        setup();

        mockCarrier.wellLoc = new MapLocation(1,1);
        mockCarrier.hqLoc = new MapLocation(0, 0);
        mockCarrier.amountResourcesHeld = Carrier.MAX_RESOURCE_CAPACITY;
        cc.run(rc, mockCarrier);

        op().moveWithBugNav(rc, mockCarrier.hqLoc);
        op().tryTransferAllResources(rc, mockCarrier.hqLoc);
        noMore();
    }

    @Test
    public void ShouldPickUpAnchorFromHqWhenOneIsAvailableAndCarryingNothingElse() throws GameActionException {
        setup();

        mockCarrier.amountResourcesHeld = 0;
        mockCarrier.hqLoc = new MapLocation(0,0);
        mockCarrier.wellLoc = new MapLocation(3, 3);
        when(rc.getLocation()).thenReturn(new MapLocation(1, 1));
        when(rc.canTakeAnchor(any(), any())).thenReturn(true);

        cc.run(rc, mockCarrier);

        op().tryPickUpAnchor(rc, mockCarrier.hqLoc);
        noMore();
    }
}
