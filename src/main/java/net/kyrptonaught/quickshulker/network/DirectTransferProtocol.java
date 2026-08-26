package net.kyrptonaught.quickshulker.network;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.kyrptonaught.quickshulker.api.storage.TransferResult;

public final class DirectTransferProtocol {
    private DirectTransferProtocol() {
    }

    public static void registerServer() {
        PayloadTypeRegistry.serverboundPlay().register(
                DirectTransferRequestPacket.ID, DirectTransferRequestPacket.CODEC);
        PayloadTypeRegistry.clientboundPlay().register(
                DirectTransferResultPacket.ID, DirectTransferResultPacket.CODEC);
        ServerPlayNetworking.registerGlobalReceiver(DirectTransferRequestPacket.ID,
                (payload, context) -> context.server().execute(() -> {
                    TransferResult result = DirectTransferTransactions.executeOnce(
                            context.player(), payload.requestId(), payload.spec());
                    if (ServerPlayNetworking.canSend(
                            context.player(), DirectTransferResultPacket.ID)) {
                        ServerPlayNetworking.send(context.player(),
                                new DirectTransferResultPacket(payload.requestId(), result));
                    }
                }));
    }
}
