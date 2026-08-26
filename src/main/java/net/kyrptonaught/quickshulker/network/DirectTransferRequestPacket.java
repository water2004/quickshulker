package net.kyrptonaught.quickshulker.network;

import net.kyrptonaught.quickshulker.QuickShulkerMod;
import net.kyrptonaught.quickshulker.api.storage.TransferCodecs;
import net.kyrptonaught.quickshulker.api.storage.TransferSpec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

import java.util.UUID;

public record DirectTransferRequestPacket(UUID requestId, TransferSpec spec)
        implements CustomPacketPayload {
    public static final Type<DirectTransferRequestPacket> ID = new Type<>(
            Identifier.fromNamespaceAndPath(QuickShulkerMod.MOD_ID, "direct_transfer_request"));
    public static final StreamCodec<RegistryFriendlyByteBuf, DirectTransferRequestPacket> CODEC =
            StreamCodec.ofMember(DirectTransferRequestPacket::write, DirectTransferRequestPacket::read);

    private void write(RegistryFriendlyByteBuf buf) {
        buf.writeUUID(requestId);
        TransferCodecs.writeSpec(buf, spec);
    }

    private static DirectTransferRequestPacket read(RegistryFriendlyByteBuf buf) {
        return new DirectTransferRequestPacket(buf.readUUID(), TransferCodecs.readSpec(buf));
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return ID;
    }
}
