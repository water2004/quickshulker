package net.kyrptonaught.quickshulker.network;

import net.kyrptonaught.quickshulker.api.storage.QuickStorageTransfer;
import net.kyrptonaught.quickshulker.api.storage.TransferResult;
import net.kyrptonaught.quickshulker.api.storage.TransferSpec;
import net.kyrptonaught.quickshulker.api.storage.TransferStatus;
import net.kyrptonaught.quickshulker.api.storage.ResolvedEndpoint;
import net.kyrptonaught.quickshulker.api.storage.ResolvedTransfer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

/** Per-player idempotency cache for direct transfer requests. */
public final class DirectTransferTransactions {
    private static final int MAX_RESULTS_PER_PLAYER = 256;
    private static final Map<UUID, LinkedHashMap<UUID, TransferResult>> RESULTS = new LinkedHashMap<>();

    private DirectTransferTransactions() {
    }

    public static TransferResult executeOnce(ServerPlayer player, UUID requestId, TransferSpec spec) {
        LinkedHashMap<UUID, TransferResult> playerResults = RESULTS.computeIfAbsent(
                player.getUUID(), ignored -> new LinkedHashMap<>());
        TransferResult cached = playerResults.get(requestId);
        if (cached != null) return refreshAuthoritativeState(player, cached);

        TransferResult result = spec != null && spec.isNetworkSafe()
                ? QuickStorageTransfer.execute(player, spec)
                : TransferResult.empty(TransferStatus.INVALID_ENDPOINT);
        playerResults.put(requestId, result);
        while (playerResults.size() > MAX_RESULTS_PER_PLAYER) {
            UUID oldest = playerResults.keySet().iterator().next();
            playerResults.remove(oldest);
        }
        return result;
    }

    private static TransferResult refreshAuthoritativeState(ServerPlayer player,
                                                            TransferResult result) {
        if (result.transfers().isEmpty()) return result;
        return new TransferResult(result.status(), result.movedCount(),
                result.transfers().stream()
                        .map(transfer -> new ResolvedTransfer(
                                refreshEndpoint(player, transfer.source()),
                                refreshEndpoint(player, transfer.destination()),
                                transfer.movedStack()))
                        .toList());
    }

    private static ResolvedEndpoint refreshEndpoint(ServerPlayer player,
                                                    ResolvedEndpoint endpoint) {
        if (endpoint.kind() == ResolvedEndpoint.Kind.PLAYER_INVENTORY) {
            int slot = endpoint.slot();
            ItemStack current = slot >= 0 && slot < player.getInventory().getContainerSize()
                    ? player.getInventory().getItem(slot)
                    : ItemStack.EMPTY;
            return new ResolvedEndpoint(endpoint.kind(), -1, slot,
                    ItemStack.EMPTY, current);
        }
        if (endpoint.kind() == ResolvedEndpoint.Kind.CARRIED_STORAGE) {
            int hostSlot = endpoint.hostInventorySlot();
            ItemStack host = hostSlot >= 0 && hostSlot < player.getInventory().getContainerSize()
                    ? player.getInventory().getItem(hostSlot)
                    : ItemStack.EMPTY;
            return new ResolvedEndpoint(endpoint.kind(), hostSlot, endpoint.slot(),
                    host, endpoint.authoritativeStack());
        }
        return endpoint;
    }

    public static void clear(ServerPlayer player) {
        if (player != null) RESULTS.remove(player.getUUID());
    }
}
