package net.kyrptonaught.quickshulker.network;

import net.kyrptonaught.quickshulker.QuickShulkerMod;
import net.kyrptonaught.quickshulker.api.storage.TransferCodecs;
import net.kyrptonaught.quickshulker.api.storage.TransferResult;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

import java.util.UUID;

public record DirectTransferResultPacket(UUID requestId, TransferResult result)
        implements CustomPacketPayload {
    public static final Type<DirectTransferResultPacket> ID = new Type<>(
            Identifier.fromNamespaceAndPath(QuickShulkerMod.MOD_ID, "direct_transfer_result"));
    public static final StreamCodec<RegistryFriendlyByteBuf, DirectTransferResultPacket> CODEC =
            StreamCodec.ofMember(DirectTransferResultPacket::write, DirectTransferResultPacket::read);

    private void write(RegistryFriendlyByteBuf buf) {
        buf.writeUUID(requestId);
        TransferCodecs.writeResult(buf, result);
    }

    private static DirectTransferResultPacket read(RegistryFriendlyByteBuf buf) {
        return new DirectTransferResultPacket(buf.readUUID(), TransferCodecs.readResult(buf));
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return ID;
    }
}
