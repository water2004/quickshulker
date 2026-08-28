package net.kyrptonaught.quickshulker.api.shulker.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.kyrptonaught.quickshulker.api.shulker.ShulkerTransferResult;

import java.util.concurrent.CompletableFuture;

/** Pollable handle for a direct shulker transfer. */
@Environment(EnvType.CLIENT)
public final class ShulkerTransferHandle {
    private final CompletableFuture<ShulkerTransferResult> completion;

    ShulkerTransferHandle(CompletableFuture<ShulkerTransferResult> completion) {
        this.completion = completion;
    }

    /** Returns whether a final result is available. */
    public boolean isDone() {
        return completion.isDone();
    }

    /** Returns the final result, or {@code null} while the request is pending. */
    public ShulkerTransferResult resultOrNull() {
        return completion.getNow(null);
    }
}
