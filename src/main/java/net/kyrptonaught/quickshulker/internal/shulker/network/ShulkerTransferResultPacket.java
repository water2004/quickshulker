package net.kyrptonaught.quickshulker.internal.shulker.network;

import net.kyrptonaught.quickshulker.QuickShulkerMod;
import net.kyrptonaught.quickshulker.api.shulker.ShulkerTransferResult;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record ShulkerTransferResultPacket(long sequence, ShulkerTransferResult result)
        implements CustomPacketPayload {
    public static final Type<ShulkerTransferResultPacket> ID = new Type<>(
            Identifier.fromNamespaceAndPath(
                    QuickShulkerMod.MOD_ID, "shulker_transfer_result"));
    public static final StreamCodec<RegistryFriendlyByteBuf, ShulkerTransferResultPacket> CODEC =
            StreamCodec.ofMember(
                    ShulkerTransferResultPacket::write,
                    ShulkerTransferResultPacket::read);

    public ShulkerTransferResultPacket {
        if (sequence <= 0) throw new IllegalArgumentException("sequence");
        if (result == null) throw new IllegalArgumentException("result");
    }

    private void write(RegistryFriendlyByteBuf buf) {
        buf.writeVarLong(sequence);
        ShulkerTransferCodecs.writeResult(buf, result);
    }

    private static ShulkerTransferResultPacket read(RegistryFriendlyByteBuf buf) {
        return new ShulkerTransferResultPacket(
                buf.readVarLong(), ShulkerTransferCodecs.readResult(buf));
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return ID;
    }
}
