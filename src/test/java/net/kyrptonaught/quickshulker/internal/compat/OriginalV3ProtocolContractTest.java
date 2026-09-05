package net.kyrptonaught.quickshulker.internal.compat;

import io.netty.buffer.Unpooled;
import net.kyrptonaught.quickshulker.network.EnderChestS2CSyncPacket;
import net.kyrptonaught.quickshulker.network.OpenInventoryPacket;
import net.kyrptonaught.quickshulker.network.OpenShulkerPacket;
import net.kyrptonaught.quickshulker.network.QuickBundlePacket;
import net.minecraft.SharedConstants;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.Bootstrap;
import net.minecraft.world.item.ItemStack;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

/** Golden wire contract from original Quick Shulker 3.0.2 for Minecraft 26.2. */
public final class OriginalV3ProtocolContractTest {
    private static final int MARKER = 0x01020304;

    @BeforeAll
    static void bootstrapMinecraftRegistries() {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
    }

    @Test
    void originalV3ChannelIdentifiersStayFrozen() {
        assertEquals("quickshulker:open_shulker_packet",
                new OpenShulkerPacket(0).type().id().toString());
        assertEquals("quickshulker:quick_bundle_packet",
                new QuickBundlePacket(0, ItemStack.EMPTY).type().id().toString());
        assertEquals("quickshulker:quick_bundleheld_packet",
                new QuickBundlePacket.BundleIntoHeld(List.of(), 0)
                        .type().id().toString());
        assertEquals("quickshulker:quick_unbundle_packet",
                new QuickBundlePacket.UnbundlePacket(0, ItemStack.EMPTY)
                        .type().id().toString());
        assertEquals("quickshulker:open_inv",
                new OpenInventoryPacket().type().id().toString());
        assertEquals("quickshulker:s2c_echest_content_packet",
                new EnderChestS2CSyncPacket.S2CEChestContentPacket(List.of())
                        .type().id().toString());
        assertEquals("quickshulker:s2c_echest_slot_packet",
                new EnderChestS2CSyncPacket.S2CEChestSlotPacket(0, ItemStack.EMPTY)
                        .type().id().toString());
    }

    @Test
    void originalV3PacketFieldOrderAndWidthsStayFrozen() throws Exception {
        FriendlyByteBuf openBuffer = new FriendlyByteBuf(Unpooled.buffer());
        OpenShulkerPacket.CODEC.encode(openBuffer, new OpenShulkerPacket(MARKER));
        assertArrayEquals(new byte[]{1, 2, 3, 4}, bytes(openBuffer));

        FriendlyByteBuf inventoryBuffer = new FriendlyByteBuf(Unpooled.buffer());
        OpenInventoryPacket.CODEC.encode(inventoryBuffer, new OpenInventoryPacket());
        assertArrayEquals(new byte[0], bytes(inventoryBuffer));

        assertArrayEquals(new byte[]{1, 2, 3, 4, 0},
                encodeRegistryPacket(QuickBundlePacket.class,
                        new QuickBundlePacket(MARKER, ItemStack.EMPTY)));
        assertArrayEquals(new byte[]{1, 2, 3, 4, 0},
                encodeRegistryPacket(QuickBundlePacket.UnbundlePacket.class,
                        new QuickBundlePacket.UnbundlePacket(MARKER, ItemStack.EMPTY)));
        assertArrayEquals(new byte[]{2, 0, 0, 1, 2, 3, 4},
                encodeRegistryPacket(QuickBundlePacket.BundleIntoHeld.class,
                        new QuickBundlePacket.BundleIntoHeld(
                                List.of(ItemStack.EMPTY, ItemStack.EMPTY), MARKER)));
        assertArrayEquals(new byte[]{0}, encodeRegistryCodec(
                EnderChestS2CSyncPacket.S2CEChestContentPacket.CODEC,
                new EnderChestS2CSyncPacket.S2CEChestContentPacket(List.of())));
        assertArrayEquals(new byte[]{1, 2, 3, 4, 0}, encodeRegistryCodec(
                EnderChestS2CSyncPacket.S2CEChestSlotPacket.CODEC,
                new EnderChestS2CSyncPacket.S2CEChestSlotPacket(
                        MARKER, ItemStack.EMPTY)));
    }

    @SuppressWarnings("unchecked")
    private static byte[] encodeRegistryPacket(Class<?> owner,
                                                CustomPacketPayload payload)
            throws Exception {
        Field codecField = owner.getDeclaredField("CODEC");
        codecField.setAccessible(true);
        StreamCodec<RegistryFriendlyByteBuf, CustomPacketPayload> codec =
                (StreamCodec<RegistryFriendlyByteBuf, CustomPacketPayload>)
                        codecField.get(null);
        return encodeRegistryCodec(codec, payload);
    }

    private static <T> byte[] encodeRegistryCodec(
            StreamCodec<RegistryFriendlyByteBuf, T> codec, T payload) {
        RegistryFriendlyByteBuf buffer = new RegistryFriendlyByteBuf(
                Unpooled.buffer(),
                RegistryAccess.fromRegistryOfRegistries(BuiltInRegistries.REGISTRY));
        codec.encode(buffer, payload);
        return bytes(buffer);
    }

    private static byte[] bytes(FriendlyByteBuf buffer) {
        byte[] result = new byte[buffer.readableBytes()];
        buffer.getBytes(buffer.readerIndex(), result);
        return result;
    }
}
