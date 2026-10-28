package net.kyrptonaught.quickshulker.internal.shulker.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.kyrptonaught.quickshulker.api.shulker.PlayerSlotEndpoint;
import net.kyrptonaught.quickshulker.api.shulker.ShulkerItemFilter;
import net.kyrptonaught.quickshulker.api.shulker.ShulkerTransferRequest;
import net.kyrptonaught.quickshulker.api.shulker.ShulkerTransferResult;
import net.kyrptonaught.quickshulker.api.shulker.ShulkerTransferStatus;
import net.kyrptonaught.quickshulker.api.shulker.client.ShulkerTransferClient;
import net.kyrptonaught.quickshulker.internal.shulker.network.ShulkerTransferRequestPacket;
import net.kyrptonaught.quickshulker.internal.shulker.network.ShulkerTransferResultPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.item.ItemStack;

import java.util.concurrent.CompletableFuture;

/** Internal lifecycle and transport implementation behind the public client facade. */
@Environment(EnvType.CLIENT)
public final class ShulkerTransferClientRuntime {
    private static final int RETRY_TICKS = 20;
    private static final int MAX_ATTEMPTS = 6;
    private static final int MAX_QUEUED_REQUESTS = 64;

    private static final SerialRequestQueue<PendingTransfer> REQUESTS =
            new SerialRequestQueue<>(MAX_QUEUED_REQUESTS);
    private static volatile long connectionGeneration;
    private static long nextSequence = 1;

    private ShulkerTransferClientRuntime() {
    }

    public static void submit(ShulkerTransferRequest request,
                              CompletableFuture<ShulkerTransferResult> completion) {
        if (completion == null) throw new IllegalArgumentException("completion");
        Minecraft client = Minecraft.getInstance();
        long submittedGeneration = connectionGeneration;
        if (!client.isSameThread()) {
            client.execute(() -> enqueue(request, completion, submittedGeneration));
            return;
        }
        enqueue(request, completion, submittedGeneration);
    }

    private static void enqueue(ShulkerTransferRequest request,
                                CompletableFuture<ShulkerTransferResult> completion,
                                long submittedGeneration) {
        if (submittedGeneration != connectionGeneration) {
            complete(completion, ShulkerTransferStatus.DISCONNECTED);
            return;
        }
        if (request == null) {
            complete(completion, ShulkerTransferStatus.INVALID_ENDPOINT);
            return;
        }
        if (!ShulkerTransferClient.isAvailable()) {
            complete(completion, ShulkerTransferStatus.UNSUPPORTED);
            return;
        }
        if (!REQUESTS.offer(new PendingTransfer(completion, request))) {
            complete(completion, ShulkerTransferStatus.RATE_LIMITED);
            return;
        }
        startNext();
    }

    public static void tick() {
        if (Minecraft.getInstance().getConnection() == null) {
            if (REQUESTS.retainedCount() > 0) {
                clear(ShulkerTransferStatus.DISCONNECTED);
            }
            return;
        }

        startNext();
        PendingTransfer active = REQUESTS.active();
        if (active == null) return;

        if (active.receipt != null
                && (active.receipt.status() != ShulkerTransferStatus.SUCCESS
                || isPlayerSlotSynchronized(active))) {
            finishActive(active.receipt);
            return;
        }

        if (++active.ticksSinceSend < RETRY_TICKS) return;
        if (active.attempts >= MAX_ATTEMPTS || !ShulkerTransferClient.isAvailable()) {
            finishActive(ShulkerTransferResult.empty(ShulkerTransferStatus.TIMEOUT));
            return;
        }
        sendActive();
    }

    public static void receive(ShulkerTransferResultPacket packet) {
        PendingTransfer active = REQUESTS.active();
        if (packet == null || active == null || packet.sequence() != active.sequence) return;
        active.receipt = packet.result();
        if (packet.result().status() != ShulkerTransferStatus.SUCCESS
                || isPlayerSlotSynchronized(active)) {
            finishActive(packet.result());
        }
    }

    public static void beginConnection() {
        connectionGeneration++;
        clear(ShulkerTransferStatus.DISCONNECTED);
    }

    public static void endConnection() {
        connectionGeneration++;
        clear(ShulkerTransferStatus.DISCONNECTED);
    }

    private static void startNext() {
        if (REQUESTS.active() != null || REQUESTS.retainedCount() == 0) return;
        if (!ShulkerTransferClient.isAvailable()) {
            clear(ShulkerTransferStatus.UNSUPPORTED);
            return;
        }

        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) return;

        PendingTransfer active = REQUESTS.activateNext();
        PlayerSlotEndpoint playerEndpoint = active.request.playerEndpoint();
        if (playerEndpoint.slot()
                >= player.getInventory().getNonEquipmentItems().size()) {
            finishActive(ShulkerTransferResult.empty(
                    ShulkerTransferStatus.INVALID_ENDPOINT));
            return;
        }
        active.sequence = nextSequence++;
        active.playerStackBefore = player.getInventory()
                .getItem(playerEndpoint.slot()).copy();
        sendActive();
    }

    private static void sendActive() {
        PendingTransfer active = REQUESTS.active();
        if (active == null) return;
        try {
            ClientPlayNetworking.send(new ShulkerTransferRequestPacket(
                    active.sequence, active.request));
            active.attempts++;
            active.ticksSinceSend = 0;
        } catch (RuntimeException error) {
            finishActive(ShulkerTransferResult.empty(ShulkerTransferStatus.ERROR));
        }
    }

    private static boolean isPlayerSlotSynchronized(PendingTransfer pending) {
        ShulkerTransferResult receipt = pending.receipt;
        if (receipt == null || receipt.status() != ShulkerTransferStatus.SUCCESS) return false;

        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) return false;
        ItemStack current = player.getInventory()
                .getItem(pending.request.playerEndpoint().slot());
        ItemStack before = pending.playerStackBefore;
        int moved = receipt.movedCount();

        if (pending.request.playerIsSource()) {
            int expectedCount = Math.max(0, before.getCount() - moved);
            return expectedCount == 0
                    ? current.isEmpty()
                    : ItemStack.isSameItemSameComponents(before, current)
                    && current.getCount() == expectedCount;
        }

        int expectedCount = before.getCount() + moved;
        if (before.isEmpty()) {
            ShulkerItemFilter filter = pending.request.filter();
            return !current.isEmpty()
                    && filter.matches(current)
                    && current.getCount() == expectedCount;
        }
        return ItemStack.isSameItemSameComponents(before, current)
                && current.getCount() == expectedCount;
    }

    private static void finishActive(ShulkerTransferResult result) {
        PendingTransfer finished = REQUESTS.completeActive();
        if (finished == null) return;
        finished.completion.complete(result);
        startNext();
    }

    private static void clear(ShulkerTransferStatus status) {
        ShulkerTransferResult result = ShulkerTransferResult.empty(status);
        for (PendingTransfer pending : REQUESTS.clear()) {
            pending.completion.complete(result);
        }
        nextSequence = 1;
    }

    private static void complete(CompletableFuture<ShulkerTransferResult> completion,
                                 ShulkerTransferStatus status) {
        completion.complete(ShulkerTransferResult.empty(status));
    }

    private static final class PendingTransfer {
        private final CompletableFuture<ShulkerTransferResult> completion;
        private final ShulkerTransferRequest request;
        private long sequence;
        private ItemStack playerStackBefore = ItemStack.EMPTY;
        private ShulkerTransferResult receipt;
        private int attempts;
        private int ticksSinceSend;

        private PendingTransfer(CompletableFuture<ShulkerTransferResult> completion,
                                ShulkerTransferRequest request) {
            this.completion = completion;
            this.request = request;
        }
    }
}
