package net.kyrptonaught.quickshulker.client.api;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.kyrptonaught.quickshulker.api.storage.TransferResult;

import java.util.UUID;

/** Pollable handle for a direct storage transfer. */
@Environment(EnvType.CLIENT)
public final class TransferHandle {
    private final UUID requestId;
    private TransferResult result;
    int attempts;
    int ticksSinceSend;

    TransferHandle(UUID requestId) {
        this.requestId = requestId;
    }

    public UUID requestId() {
        return requestId;
    }

    public boolean isDone() {
        return result != null;
    }

    public TransferResult result() {
        return result;
    }

    void complete(TransferResult result) {
        if (this.result == null) this.result = result;
    }
}
