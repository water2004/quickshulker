package net.kyrptonaught.quickshulker.client.api;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.kyrptonaught.quickshulker.api.storage.ResolvedEndpoint;
import net.kyrptonaught.quickshulker.api.storage.ResolvedTransfer;
import net.kyrptonaught.quickshulker.api.storage.TransferResult;
import net.kyrptonaught.quickshulker.api.storage.TransferSpec;
import net.kyrptonaught.quickshulker.api.storage.TransferStatus;
import net.kyrptonaught.quickshulker.network.DirectTransferRequestPacket;
import net.kyrptonaught.quickshulker.network.DirectTransferResultPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

/** Client facade for the screen-independent transfer protocol. */
@Environment(EnvType.CLIENT)
public final class QuickStorageClient {
    private static final int RETRY_TICKS = 20;
    private static final int MAX_ATTEMPTS = 6;
    private static final int MAX_RETAINED_HANDLES = 256;
    private static final Map<UUID, TransferHandle> HANDLES = new LinkedHashMap<>();

    private QuickStorageClient() {
    }

    public static boolean isAvailable() {
        return ClientPlayNetworking.canSend(DirectTransferRequestPacket.ID);
    }

    public static TransferHandle submit(TransferSpec spec) {
        trimHandles();
        UUID requestId = UUID.randomUUID();
        TransferHandle handle = new TransferHandle(requestId);
        if (HANDLES.size() >= MAX_RETAINED_HANDLES) {
            handle.complete(TransferResult.empty(TransferStatus.UNKNOWN));
            return handle;
        }
        HANDLES.put(requestId, handle);

        if (spec == null || !spec.isNetworkSafe()) {
            handle.complete(TransferResult.empty(TransferStatus.INVALID_ENDPOINT));
        } else if (!isAvailable()) {
            handle.complete(TransferResult.empty(TransferStatus.UNSUPPORTED));
        } else {
            send(handle, spec);
        }
        return handle;
    }

    public static void forget(TransferHandle handle) {
        if (handle != null) {
            HANDLES.remove(handle.requestId());
            PendingSpecs.SPECS.remove(handle.requestId());
        }
    }

    public static void clear() {
        HANDLES.clear();
        PendingSpecs.SPECS.clear();
    }

    public static void tick() {
        if (Minecraft.getInstance().getConnection() == null) {
            clear();
            return;
        }
        for (TransferHandle handle : HANDLES.values()) {
            if (handle.isDone() || ++handle.ticksSinceSend < RETRY_TICKS) continue;
            if (handle.attempts >= MAX_ATTEMPTS || !isAvailable()) {
                handle.complete(TransferResult.empty(TransferStatus.UNKNOWN));
                PendingSpecs.SPECS.remove(handle.requestId());
                continue;
            }
            TransferSpec spec = PendingSpecs.SPECS.get(handle.requestId());
            if (spec == null) {
                handle.complete(TransferResult.empty(TransferStatus.UNKNOWN));
            } else {
                send(handle, spec);
            }
        }
    }

    public static void receive(DirectTransferResultPacket packet) {
        TransferHandle handle = HANDLES.get(packet.requestId());
        if (handle == null || handle.isDone()) return;
        applyAuthoritativeState(packet.result());
        handle.complete(packet.result());
        PendingSpecs.SPECS.remove(packet.requestId());
    }

    private static void send(TransferHandle handle, TransferSpec spec) {
        PendingSpecs.SPECS.put(handle.requestId(), spec);
        ClientPlayNetworking.send(new DirectTransferRequestPacket(handle.requestId(), spec));
        handle.attempts++;
        handle.ticksSinceSend = 0;
    }

    private static void applyAuthoritativeState(TransferResult result) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) return;
        for (ResolvedTransfer transfer : result.transfers()) {
            applyEndpoint(player, transfer.source());
            applyEndpoint(player, transfer.destination());
        }
        player.getInventory().setChanged();
    }

    private static void applyEndpoint(LocalPlayer player, ResolvedEndpoint endpoint) {
        if (endpoint.kind() == ResolvedEndpoint.Kind.PLAYER_INVENTORY) {
            int slot = endpoint.slot();
            if (slot >= 0 && slot < player.getInventory().getContainerSize()) {
                player.getInventory().setItem(slot, endpoint.authoritativeStack().copy());
            }
            return;
        }
        if (endpoint.kind() == ResolvedEndpoint.Kind.CARRIED_STORAGE) {
            int hostSlot = endpoint.hostInventorySlot();
            if (hostSlot >= 0 && hostSlot < player.getInventory().getContainerSize()) {
                player.getInventory().setItem(hostSlot, endpoint.authoritativeHost().copy());
            }
        }
    }

    private static void trimHandles() {
        if (HANDLES.size() <= MAX_RETAINED_HANDLES) return;
        Iterator<Map.Entry<UUID, TransferHandle>> iterator = HANDLES.entrySet().iterator();
        while (HANDLES.size() > MAX_RETAINED_HANDLES && iterator.hasNext()) {
            Map.Entry<UUID, TransferHandle> entry = iterator.next();
            if (entry.getValue().isDone()) {
                PendingSpecs.SPECS.remove(entry.getKey());
                iterator.remove();
            }
        }
    }

    private static final class PendingSpecs {
        private static final Map<UUID, TransferSpec> SPECS = new LinkedHashMap<>();
    }
}
