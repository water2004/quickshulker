package net.kyrptonaught.quickshulker.api.shulker;

/** A fixed-size receipt. It never contains cached shulker or inventory state. */
public record ShulkerTransferResult(ShulkerTransferStatus status, int movedCount) {
    public ShulkerTransferResult {
        if (status == null) throw new IllegalArgumentException("status");
        if (movedCount < 0) throw new IllegalArgumentException("movedCount");
        if ((status == ShulkerTransferStatus.SUCCESS) != (movedCount > 0)) {
            throw new IllegalArgumentException(
                    "SUCCESS must move items and non-success results must not");
        }
    }

    public static ShulkerTransferResult empty(ShulkerTransferStatus status) {
        return new ShulkerTransferResult(status, 0);
    }

    public boolean movedAnything() {
        return movedCount > 0;
    }
}
