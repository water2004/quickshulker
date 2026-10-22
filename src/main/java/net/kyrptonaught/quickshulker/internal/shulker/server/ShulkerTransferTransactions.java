package net.kyrptonaught.quickshulker.internal.shulker.server;

import net.kyrptonaught.quickshulker.api.shulker.ShulkerTransferRequest;
import net.kyrptonaught.quickshulker.api.shulker.ShulkerTransferResult;
import net.kyrptonaught.quickshulker.api.shulker.ShulkerTransferStatus;
import net.kyrptonaught.quickshulker.api.shulker.server.ShulkerStorages;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.item.PlayerInventoryStorage;
import net.fabricmc.fabric.api.transfer.v1.storage.SlottedStorage;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageUtil;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleSlotStorage;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.server.level.ServerPlayer;

import java.util.IdentityHashMap;
import java.util.Map;

/**
 * Per-connection sequencing and idempotency. Only the last fixed-size receipt
 * is retained; no inventory, container, or item state is cached.
 */
public final class ShulkerTransferTransactions {
    private static final int MAX_NEW_REQUESTS_PER_TICK = 8;
    private static final Map<ServerPlayer, PlayerState> PLAYERS = new IdentityHashMap<>();

    private ShulkerTransferTransactions() {
    }

    public static ShulkerTransferResult executeOnce(ServerPlayer player,
                                                    long sequence,
                                                    ShulkerTransferRequest request) {
        if (player == null || request == null || sequence <= 0) {
            return ShulkerTransferResult.empty(ShulkerTransferStatus.INVALID_ENDPOINT);
        }

        PlayerState state = PLAYERS.computeIfAbsent(player, ignored -> new PlayerState());
        if (sequence == state.lastSequence && state.lastResult != null) {
            return state.lastResult;
        }
        // Requests are serialized by the client, but every copy of one request
        // can still be lost. Accept gaps so a later request can recover the
        // connection; only stale sequence numbers are invalid.
        if (sequence < state.lastSequence) {
            return ShulkerTransferResult.empty(ShulkerTransferStatus.OUT_OF_ORDER);
        }

        long gameTime = player.level().getGameTime();
        if (state.rateTick != gameTime) {
            state.rateTick = gameTime;
            state.requestsThisTick = 0;
        }

        ShulkerTransferResult result;
        if (++state.requestsThisTick > MAX_NEW_REQUESTS_PER_TICK) {
            result = ShulkerTransferResult.empty(ShulkerTransferStatus.RATE_LIMITED);
        } else {
            try {
                result = execute(player, request);
            } catch (RuntimeException error) {
                result = ShulkerTransferResult.empty(ShulkerTransferStatus.ERROR);
            }
        }

        state.lastSequence = sequence;
        state.lastResult = result;
        return result;
    }

    private static ShulkerTransferResult execute(ServerPlayer player,
                                                  ShulkerTransferRequest request) {
        int playerSlot = request.playerEndpoint().slot();
        int hostSlot = request.shulkerEndpoint().hostInventorySlot();
        int inventorySize = player.getInventory().getNonEquipmentItems().size();
        if (playerSlot >= inventorySize || hostSlot >= inventorySize) {
            return ShulkerTransferResult.empty(ShulkerTransferStatus.INVALID_ENDPOINT);
        }

        SlottedStorage<ItemVariant> shulker = ShulkerStorages
                .findCarried(player, hostSlot)
                .orElse(null);
        if (shulker == null) {
            return ShulkerTransferResult.empty(ShulkerTransferStatus.UNSUPPORTED);
        }
        int shulkerSlot = request.shulkerEndpoint().shulkerSlot();
        if (shulkerSlot >= shulker.getSlotCount()) {
            return ShulkerTransferResult.empty(ShulkerTransferStatus.INVALID_ENDPOINT);
        }

        SingleSlotStorage<ItemVariant> playerStorage = PlayerInventoryStorage
                .of(player)
                .getSlot(playerSlot);
        SingleSlotStorage<ItemVariant> shulkerStorage = shulker.getSlot(shulkerSlot);
        Storage<ItemVariant> source = request.playerIsSource()
                ? playerStorage : shulkerStorage;
        Storage<ItemVariant> destination = request.playerIsSource()
                ? shulkerStorage : playerStorage;

        ItemVariant available = request.playerIsSource()
                ? playerStorage.getResource() : shulkerStorage.getResource();
        if (available.isBlank() || !request.filter().matches(available.toStack())) {
            return ShulkerTransferResult.empty(ShulkerTransferStatus.NO_MATCH);
        }

        try (Transaction transaction = Transaction.openOuter()) {
            long moved = StorageUtil.move(
                    source,
                    destination,
                    variant -> request.filter().matches(variant.toStack()),
                    request.maxAmount(),
                    transaction);
            if (moved <= 0) {
                return ShulkerTransferResult.empty(ShulkerTransferStatus.NO_SPACE);
            }
            transaction.commit();
            return new ShulkerTransferResult(
                    ShulkerTransferStatus.SUCCESS, Math.toIntExact(moved));
        }
    }

    public static void clear(ServerPlayer player) {
        if (player != null) PLAYERS.remove(player);
    }

    private static final class PlayerState {
        private long lastSequence;
        private ShulkerTransferResult lastResult;
        private long rateTick = Long.MIN_VALUE;
        private int requestsThisTick;
    }
}
