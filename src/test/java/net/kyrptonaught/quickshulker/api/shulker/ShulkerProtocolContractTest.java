package net.kyrptonaught.quickshulker.api.shulker;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

public final class ShulkerProtocolContractTest {
    @Test
    void requestsDescribeExactlyOnePlayerAndOneShulkerSlot() {
        new ShulkerTransferRequest(
                new CarriedShulkerSlotEndpoint(9, 4),
                new PlayerSlotEndpoint(0),
                ShulkerItemFilter.any(), 32);

        assertThrows(IllegalArgumentException.class, () -> new ShulkerTransferRequest(
                new PlayerSlotEndpoint(0), new PlayerSlotEndpoint(1),
                ShulkerItemFilter.any(), 1));
        assertThrows(IllegalArgumentException.class, () -> new ShulkerTransferRequest(
                new CarriedShulkerSlotEndpoint(9, 0),
                new CarriedShulkerSlotEndpoint(10, 0),
                ShulkerItemFilter.any(), 1));
        assertDoesNotThrow(() -> new CarriedShulkerSlotEndpoint(90, 270));
        assertDoesNotThrow(() -> new PlayerSlotEndpoint(90));
        assertThrows(IllegalArgumentException.class,
                () -> new CarriedShulkerSlotEndpoint(-1, 0));
        assertThrows(IllegalArgumentException.class,
                () -> new CarriedShulkerSlotEndpoint(0, -1));
    }

    @Test
    void networkAmountAndReceiptsHaveStrictBounds() {
        assertThrows(IllegalArgumentException.class, () -> new ShulkerTransferRequest(
                new PlayerSlotEndpoint(0),
                new CarriedShulkerSlotEndpoint(9, 0),
                ShulkerItemFilter.any(), ShulkerTransferRequest.MAX_NETWORK_AMOUNT + 1));
        assertThrows(IllegalArgumentException.class,
                () -> new ShulkerTransferResult(ShulkerTransferStatus.SUCCESS, 0));
        assertThrows(IllegalArgumentException.class,
                () -> new ShulkerTransferResult(ShulkerTransferStatus.NO_MATCH, 1));
    }
}
