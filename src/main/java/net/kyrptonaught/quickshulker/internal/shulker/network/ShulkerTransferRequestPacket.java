package net.kyrptonaught.quickshulker.internal.shulker.network;

import net.kyrptonaught.quickshulker.QuickShulkerMod;
import net.kyrptonaught.quickshulker.api.shulker.ShulkerTransferRequest;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record ShulkerTransferRequestPacket(long sequence, ShulkerTransferRequest request)
        implements CustomPacketPayload {
    public static final Type<ShulkerTransferRequestPacket> ID = new Type<>(
            Identifier.fromNamespaceAndPath(
                    QuickShulkerMod.MOD_ID, "shulker_transfer_request"));
    public static final StreamCodec<RegistryFriendlyByteBuf, ShulkerTransferRequestPacket> CODEC =
            StreamCodec.ofMember(
                    ShulkerTransferRequestPacket::write,
                    ShulkerTransferRequestPacket::read);

    public ShulkerTransferRequestPacket {
        if (sequence <= 0) throw new IllegalArgumentException("sequence");
        if (request == null) throw new IllegalArgumentException("request");
    }

    private void write(RegistryFriendlyByteBuf buf) {
        buf.writeVarLong(sequence);
        ShulkerTransferCodecs.writeRequest(buf, request);
    }

    private static ShulkerTransferRequestPacket read(RegistryFriendlyByteBuf buf) {
        return new ShulkerTransferRequestPacket(
                buf.readVarLong(), ShulkerTransferCodecs.readRequest(buf));
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return ID;
    }
}
