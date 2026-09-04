package net.kyrptonaught.quickshulker.internal.compat;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.kyrptonaught.quickshulker.internal.shulker.network.ShulkerTransferResultPacket;
import net.kyrptonaught.quickshulker.network.OpenInventoryPacket;
import net.minecraft.server.level.ServerPlayer;

/** Classifies the immutable capabilities negotiated for one play connection. */
public final class ClientProtocolResolver {
    private ClientProtocolResolver() {
    }

    public static ClientProtocol resolve(ServerPlayer player) {
        return resolve(
                ServerPlayNetworking.canSend(player, OpenInventoryPacket.OPEN_INV_ID),
                ServerPlayNetworking.canSend(player, ShulkerTransferResultPacket.ID));
    }

    static ClientProtocol resolve(boolean acceptsOriginalV3Packets,
                                  boolean acceptsV4TransferResults) {
        if (acceptsV4TransferResults) return ClientProtocol.V4;
        if (acceptsOriginalV3Packets) return ClientProtocol.ORIGINAL_V3;
        return ClientProtocol.VANILLA;
    }

    public enum ClientProtocol {
        VANILLA,
        ORIGINAL_V3,
        V4
    }
}
