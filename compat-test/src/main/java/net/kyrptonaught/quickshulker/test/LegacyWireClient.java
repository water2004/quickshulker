package net.kyrptonaught.quickshulker.test;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/** Exercises the frozen v3 channels without installing the v4 implementation. */
final class LegacyWireClient {
    private LegacyWireClient() {
    }

    static void register() {
        PayloadTypeRegistry.playC2S().register(Open.ID, Open.CODEC);
        PayloadTypeRegistry.playS2C().register(OpenInventory.ID, OpenInventory.CODEC);
        ClientPlayNetworking.registerGlobalReceiver(OpenInventory.ID, (payload, context) ->
                context.client().setScreen(new InventoryScreen(context.player())));
    }

    static void open(int slot) {
        if (!ClientPlayNetworking.canSend(Open.ID)) {
            throw new AssertionError("Server did not advertise the original open-shulker channel");
        }
        ClientPlayNetworking.send(new Open(slot));
    }

    private record Open(int slot) implements CustomPacketPayload {
        private static final Type<Open> ID = new Type<>(
                ResourceLocation.fromNamespaceAndPath("quickshulker", "open_shulker_packet"));
        private static final StreamCodec<FriendlyByteBuf, Open> CODEC = StreamCodec.ofMember(
                (payload, buffer) -> buffer.writeInt(payload.slot()), buffer -> new Open(buffer.readInt()));

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return ID;
        }
    }

    private record OpenInventory() implements CustomPacketPayload {
        private static final Type<OpenInventory> ID = new Type<>(
                ResourceLocation.fromNamespaceAndPath("quickshulker", "open_inv"));
        private static final StreamCodec<FriendlyByteBuf, OpenInventory> CODEC = StreamCodec.ofMember(
                (payload, buffer) -> { }, buffer -> new OpenInventory());

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return ID;
        }
    }
}
