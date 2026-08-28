package net.kyrptonaught.quickshulker.api.shulker;

/**
 * One exact slot inside one carried shulker box. Requests deliberately
 * cannot ask the server to search; actual bounds are checked against the
 * live containers when the request executes.
 */
public record CarriedShulkerSlotEndpoint(int hostInventorySlot, int shulkerSlot)
        implements ShulkerTransferEndpoint {
    public CarriedShulkerSlotEndpoint {
        if (hostInventorySlot < 0) {
            throw new IllegalArgumentException("hostInventorySlot");
        }
        if (shulkerSlot < 0) {
            throw new IllegalArgumentException("shulkerSlot");
        }
    }

}
