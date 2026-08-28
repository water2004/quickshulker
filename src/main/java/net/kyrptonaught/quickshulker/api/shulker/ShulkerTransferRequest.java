package net.kyrptonaught.quickshulker.api.shulker;

/**
 * A bounded client request. Exactly one endpoint must be a player slot and
 * the other one exact slot in one carried shulker box.
 */
public record ShulkerTransferRequest(ShulkerTransferEndpoint source,
                                     ShulkerTransferEndpoint destination,
                                     ShulkerItemFilter filter,
                                     int maxAmount) {
    public static final int MAX_NETWORK_AMOUNT = 4096;

    public ShulkerTransferRequest {
        if (source == null || destination == null || filter == null) {
            throw new IllegalArgumentException("transfer request fields must not be null");
        }
        boolean sourcePlayer = source instanceof PlayerSlotEndpoint;
        boolean destinationPlayer = destination instanceof PlayerSlotEndpoint;
        boolean sourceStorage = source instanceof CarriedShulkerSlotEndpoint;
        boolean destinationStorage = destination instanceof CarriedShulkerSlotEndpoint;
        if (!(sourcePlayer && destinationStorage || sourceStorage && destinationPlayer)) {
            throw new IllegalArgumentException(
                    "shulker transfers require one player slot and one carried shulker slot");
        }
        if (maxAmount <= 0 || maxAmount > MAX_NETWORK_AMOUNT) {
            throw new IllegalArgumentException("maxAmount");
        }
    }

    public PlayerSlotEndpoint playerEndpoint() {
        return source instanceof PlayerSlotEndpoint player
                ? player : (PlayerSlotEndpoint) destination;
    }

    public boolean playerIsSource() {
        return source instanceof PlayerSlotEndpoint;
    }

    public CarriedShulkerSlotEndpoint shulkerEndpoint() {
        return source instanceof CarriedShulkerSlotEndpoint shulker
                ? shulker : (CarriedShulkerSlotEndpoint) destination;
    }

}
