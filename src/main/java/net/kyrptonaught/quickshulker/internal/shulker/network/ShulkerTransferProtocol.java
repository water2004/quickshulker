package net.kyrptonaught.quickshulker.internal.shulker.network;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.kyrptonaught.quickshulker.api.shulker.ShulkerTransferResult;
import net.kyrptonaught.quickshulker.internal.shulker.server.ShulkerTransferTransactions;

/** Registers the same-version direct shulker protocol. */
public final class ShulkerTransferProtocol {
    private ShulkerTransferProtocol() {
    }

    public static void register() {
        PayloadTypeRegistry.serverboundPlay().register(
                ShulkerTransferRequestPacket.ID, ShulkerTransferRequestPacket.CODEC);
        PayloadTypeRegistry.clientboundPlay().register(
                ShulkerTransferResultPacket.ID, ShulkerTransferResultPacket.CODEC);
        ServerPlayNetworking.registerGlobalReceiver(ShulkerTransferRequestPacket.ID,
                (payload, context) -> context.server().execute(() -> {
                    ShulkerTransferResult result = ShulkerTransferTransactions.executeOnce(
                            context.player(), payload.sequence(), payload.request());

                    // Vanilla remains authoritative. Re-send it for both first attempts and
                    // duplicates before the fixed-size receipt is delivered.
                    context.player().inventoryMenu.sendAllDataToRemote();
                    if (ServerPlayNetworking.canSend(
                            context.player(), ShulkerTransferResultPacket.ID)) {
                        ServerPlayNetworking.send(context.player(),
                                new ShulkerTransferResultPacket(payload.sequence(), result));
                    }
                }));
    }
}
