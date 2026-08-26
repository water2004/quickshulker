package net.kyrptonaught.quickshulker.api.storage;

import net.kyrptonaught.quickshulker.util.PacketUtils;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

/** Wire codecs for the screen-independent transfer protocol. */
public final class TransferCodecs {
    private static final int MAX_MATCH_CANDIDATES = 64;
    private static final int MAX_TRANSFERS = 256;

    private TransferCodecs() {
    }

    public static void writeSpec(RegistryFriendlyByteBuf buf, TransferSpec spec) {
        if (!spec.isNetworkSafe()) throw new IllegalArgumentException("server-local endpoint in network spec");
        writeEndpoint(buf, spec.source());
        writeEndpoint(buf, spec.destination());
        writeMatcher(buf, spec.matcher());
        buf.writeVarInt(spec.limit().maxAmount());
        buf.writeVarInt(spec.limit().maxSourceSlots());
        buf.writeVarInt(spec.limit().maxDestinationSlots());
    }

    public static TransferSpec readSpec(RegistryFriendlyByteBuf buf) {
        TransferEndpoint source = readEndpoint(buf);
        TransferEndpoint destination = readEndpoint(buf);
        StackMatcher matcher = readMatcher(buf);
        TransferLimit limit = new TransferLimit(
                buf.readVarInt(), buf.readVarInt(), buf.readVarInt());
        return new TransferSpec(source, destination, matcher, limit);
    }

    public static void writeResult(RegistryFriendlyByteBuf buf, TransferResult result) {
        writeEnum(buf, result.status());
        buf.writeVarInt(result.movedCount());
        if (result.transfers().size() > MAX_TRANSFERS) {
            throw new IllegalArgumentException("too many resolved transfers");
        }
        buf.writeVarInt(result.transfers().size());
        for (ResolvedTransfer transfer : result.transfers()) {
            writeResolvedEndpoint(buf, transfer.source());
            writeResolvedEndpoint(buf, transfer.destination());
            PacketUtils.writeItemStack(buf, transfer.movedStack());
        }
    }

    public static TransferResult readResult(RegistryFriendlyByteBuf buf) {
        TransferStatus status = readEnum(buf, TransferStatus.values());
        int moved = buf.readVarInt();
        int size = readBoundedSize(buf, MAX_TRANSFERS);
        List<ResolvedTransfer> transfers = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            transfers.add(new ResolvedTransfer(
                    readResolvedEndpoint(buf),
                    readResolvedEndpoint(buf),
                    PacketUtils.readItemStack(buf)));
        }
        return new TransferResult(status, moved, transfers);
    }

    private static void writeEndpoint(RegistryFriendlyByteBuf buf, TransferEndpoint endpoint) {
        if (endpoint instanceof PlayerInventoryEndpoint player) {
            buf.writeByte(0);
            writeSlotSelector(buf, player.slots());
            return;
        }
        if (endpoint instanceof CarriedStorageEndpoint storage) {
            buf.writeByte(1);
            writeIndexSelector(buf, storage.storage().inventorySlots());
            writeMatcher(buf, storage.storage().hostMatcher());
            writeSlotSelector(buf, storage.slots());
            return;
        }
        throw new IllegalArgumentException("server-local endpoint in network spec");
    }

    private static TransferEndpoint readEndpoint(RegistryFriendlyByteBuf buf) {
        return switch (buf.readUnsignedByte()) {
            case 0 -> new PlayerInventoryEndpoint(readSlotSelector(buf));
            case 1 -> new CarriedStorageEndpoint(
                    new StorageSelector(readIndexSelector(buf), readMatcher(buf)),
                    readSlotSelector(buf));
            default -> throw new IllegalArgumentException("unknown transfer endpoint");
        };
    }

    private static void writeSlotSelector(RegistryFriendlyByteBuf buf, SlotSelector selector) {
        writeIndexSelector(buf, selector.indexes());
        writeEnum(buf, selector.condition());
    }

    private static SlotSelector readSlotSelector(RegistryFriendlyByteBuf buf) {
        return new SlotSelector(
                readIndexSelector(buf),
                readEnum(buf, SlotSelector.Condition.values()));
    }

    private static void writeIndexSelector(RegistryFriendlyByteBuf buf, IndexSelector selector) {
        writeEnum(buf, selector.mode());
        buf.writeVarInt(selector.index() + 1);
        writeEnum(buf, selector.order());
    }

    private static IndexSelector readIndexSelector(RegistryFriendlyByteBuf buf) {
        return new IndexSelector(
                readEnum(buf, IndexSelector.Mode.values()),
                buf.readVarInt() - 1,
                readEnum(buf, IndexSelector.Order.values()));
    }

    private static void writeMatcher(RegistryFriendlyByteBuf buf, StackMatcher matcher) {
        writeEnum(buf, matcher.mode());
        if (matcher.candidates().size() > MAX_MATCH_CANDIDATES) {
            throw new IllegalArgumentException("too many matcher candidates");
        }
        buf.writeVarInt(matcher.candidates().size());
        for (ItemStack candidate : matcher.candidates()) {
            PacketUtils.writeItemStack(buf, candidate);
        }
    }

    private static StackMatcher readMatcher(RegistryFriendlyByteBuf buf) {
        StackMatcher.Mode mode = readEnum(buf, StackMatcher.Mode.values());
        int size = readBoundedSize(buf, MAX_MATCH_CANDIDATES);
        List<ItemStack> candidates = new ArrayList<>(size);
        for (int i = 0; i < size; i++) candidates.add(PacketUtils.readItemStack(buf));
        return new StackMatcher(mode, candidates);
    }

    private static void writeResolvedEndpoint(RegistryFriendlyByteBuf buf,
                                              ResolvedEndpoint endpoint) {
        writeEnum(buf, endpoint.kind());
        buf.writeVarInt(endpoint.hostInventorySlot() + 1);
        buf.writeVarInt(endpoint.slot() + 1);
        PacketUtils.writeItemStack(buf, endpoint.authoritativeHost());
        PacketUtils.writeItemStack(buf, endpoint.authoritativeStack());
    }

    private static ResolvedEndpoint readResolvedEndpoint(RegistryFriendlyByteBuf buf) {
        return new ResolvedEndpoint(
                readEnum(buf, ResolvedEndpoint.Kind.values()),
                buf.readVarInt() - 1,
                buf.readVarInt() - 1,
                PacketUtils.readItemStack(buf),
                PacketUtils.readItemStack(buf));
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

    private static int readBoundedSize(RegistryFriendlyByteBuf buf, int maximum) {
        int size = buf.readVarInt();
        if (size < 0 || size > maximum) throw new IllegalArgumentException("invalid list size");
        return size;
    }
}
