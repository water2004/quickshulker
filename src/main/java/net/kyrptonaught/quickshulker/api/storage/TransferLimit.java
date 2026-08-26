package net.kyrptonaught.quickshulker.api.storage;

/** Independent amount and endpoint fan-out limits for a transfer. */
public record TransferLimit(int maxAmount, int maxSourceSlots, int maxDestinationSlots) {
    public TransferLimit {
        if (maxAmount <= 0 || maxSourceSlots <= 0 || maxDestinationSlots <= 0) {
            throw new IllegalArgumentException("transfer limits must be positive");
        }
    }

    public static TransferLimit all() {
        return new TransferLimit(Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE);
    }

    public static TransferLimit oneSourceStack() {
        return new TransferLimit(Integer.MAX_VALUE, 1, Integer.MAX_VALUE);
    }
}
