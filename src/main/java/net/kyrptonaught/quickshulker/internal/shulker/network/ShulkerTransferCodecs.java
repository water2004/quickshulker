package net.kyrptonaught.quickshulker.internal.shulker.network;

import net.kyrptonaught.quickshulker.api.shulker.CarriedShulkerSlotEndpoint;
import net.kyrptonaught.quickshulker.api.shulker.PlayerSlotEndpoint;
import net.kyrptonaught.quickshulker.api.shulker.ShulkerItemFilter;
import net.kyrptonaught.quickshulker.api.shulker.ShulkerTransferEndpoint;
import net.kyrptonaught.quickshulker.api.shulker.ShulkerTransferRequest;
import net.kyrptonaught.quickshulker.api.shulker.ShulkerTransferResult;
import net.kyrptonaught.quickshulker.api.shulker.ShulkerTransferStatus;
import net.kyrptonaught.quickshulker.util.PacketUtils;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.item.ItemStack;

/** Fixed, bounded codecs for the direct shulker protocol. */
final class ShulkerTransferCodecs {
    private ShulkerTransferCodecs() {
    }

    public static void writeRequest(RegistryFriendlyByteBuf buf, ShulkerTransferRequest request) {
        writeEndpoint(buf, request.source());
        writeEndpoint(buf, request.destination());
        writeFilter(buf, request.filter());
        buf.writeVarInt(request.maxAmount());
    }

    public static ShulkerTransferRequest readRequest(RegistryFriendlyByteBuf buf) {
        return new ShulkerTransferRequest(
                readEndpoint(buf),
                readEndpoint(buf),
                readFilter(buf),
                buf.readVarInt());
    }

    public static void writeResult(RegistryFriendlyByteBuf buf,
                                   ShulkerTransferResult result) {
        writeEnum(buf, result.status());
        buf.writeVarInt(result.movedCount());
    }

    public static ShulkerTransferResult readResult(RegistryFriendlyByteBuf buf) {
        return new ShulkerTransferResult(
                readEnum(buf, ShulkerTransferStatus.values()),
                buf.readVarInt());
    }

    private static void writeEndpoint(RegistryFriendlyByteBuf buf,
                                      ShulkerTransferEndpoint endpoint) {
        if (endpoint instanceof PlayerSlotEndpoint player) {
            buf.writeByte(0);
            buf.writeVarInt(player.slot());
            return;
        }
        if (endpoint instanceof CarriedShulkerSlotEndpoint shulker) {
            buf.writeByte(1);
            buf.writeVarInt(shulker.hostInventorySlot());
            buf.writeVarInt(shulker.shulkerSlot());
            return;
        }
        throw new IllegalArgumentException("unknown remote endpoint");
    }

    private static ShulkerTransferEndpoint readEndpoint(RegistryFriendlyByteBuf buf) {
        return switch (buf.readUnsignedByte()) {
            case 0 -> new PlayerSlotEndpoint(buf.readVarInt());
            case 1 -> new CarriedShulkerSlotEndpoint(buf.readVarInt(), buf.readVarInt());
            default -> throw new IllegalArgumentException("unknown remote endpoint");
        };
    }

    private static void writeFilter(RegistryFriendlyByteBuf buf,
                                    ShulkerItemFilter filter) {
        writeEnum(buf, filter.mode());
        if (filter.mode() != ShulkerItemFilter.Mode.ANY) {
            PacketUtils.writeItemStack(buf, filter.template());
        }
    }

    private static ShulkerItemFilter readFilter(RegistryFriendlyByteBuf buf) {
        ShulkerItemFilter.Mode mode = readEnum(buf, ShulkerItemFilter.Mode.values());
        ItemStack template = mode == ShulkerItemFilter.Mode.ANY
                ? ItemStack.EMPTY : PacketUtils.readItemStack(buf);
        return new ShulkerItemFilter(mode, template);
    }

    private static void writeEnum(RegistryFriendlyByteBuf buf, Enum<?> value) {
        buf.writeVarInt(value.ordinal());
    }

    private static <T> T readEnum(RegistryFriendlyByteBuf buf, T[] values) {
        int ordinal = buf.readVarInt();
        if (ordinal < 0 || ordinal >= values.length) {
            throw new IllegalArgumentException("unknown enum value " + ordinal);
        }
        return values[ordinal];
    }
}
