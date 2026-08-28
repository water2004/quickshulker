package net.kyrptonaught.quickshulker.api.shulker.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.kyrptonaught.quickshulker.api.shulker.ShulkerTransferRequest;
import net.kyrptonaught.quickshulker.api.shulker.ShulkerTransferResult;
import net.kyrptonaught.quickshulker.internal.shulker.client.ShulkerTransferClientRuntime;
import net.kyrptonaught.quickshulker.internal.shulker.network.ShulkerTransferRequestPacket;

import java.util.concurrent.CompletableFuture;

/**
 * Client facade for same-version, screen-independent carried-shulker
 * transfers.
 *
 * <p>Requests execute serially against server-authoritative inventory state.
 * Callers may submit from any thread; completion is published by the Minecraft
 * client thread and may be polled through the returned handle.</p>
 */
@Environment(EnvType.CLIENT)
public final class ShulkerTransferClient {
    private ShulkerTransferClient() {
    }

    /**
     * Returns whether the connected server advertises the direct shulker
     * transfer protocol. This method must be called on the client thread.
     */
    public static boolean isAvailable() {
        return ClientPlayNetworking.canSend(ShulkerTransferRequestPacket.ID);
    }

    /**
     * Queues one request without opening a screen.
     *
     * <p>An unavailable server, a full client queue, disconnection, timeout,
     * or invalid request completes the returned handle with the corresponding
     * non-success status. Submission never falls back to legacy screen
     * simulation.</p>
     */
    public static ShulkerTransferHandle submit(ShulkerTransferRequest request) {
        CompletableFuture<ShulkerTransferResult> completion = new CompletableFuture<>();
        ShulkerTransferClientRuntime.submit(request, completion);
        return new ShulkerTransferHandle(completion);
    }
}
