package net.kyrptonaught.quickshulker.api.storage;

import java.util.List;

public record TransferResult(TransferStatus status,
                             int movedCount,
                             List<ResolvedTransfer> transfers) {
    public TransferResult {
        if (status == null) throw new IllegalArgumentException("status");
        transfers = transfers == null ? List.of() : List.copyOf(transfers);
    }

    public static TransferResult empty(TransferStatus status) {
        return new TransferResult(status, 0, List.of());
    }

    public boolean movedAnything() {
        return movedCount > 0;
    }
}
