package net.kyrptonaught.quickshulker.internal.legacy;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.kyrptonaught.quickshulker.gui.MenuTypes;
import net.kyrptonaught.quickshulker.gui.screen.BundleContainer;
import net.kyrptonaught.quickshulker.gui.screen.LegacyBundleItemMenu;
import net.kyrptonaught.quickshulker.gui.screen.PagedBundleItemMenu;
import net.kyrptonaught.quickshulker.internal.shulker.network.ShulkerTransferResultPacket;
import net.kyrptonaught.quickshulker.network.OpenInventoryPacket;
import net.kyrptonaught.quickshulker.network.OpenShulkerPacket;
import net.kyrptonaught.quickshulker.network.QuickBundlePacket;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.item.ItemStack;

/**
 * Server-side compatibility boundary for the original Quick Shulker v3 wire
 * protocol. This project's pre-v4 releases are intentionally not a separate
 * compatibility target.
 */
public final class OriginalV3Compatibility {
    private OriginalV3Compatibility() {
    }

    public static void register() {
        MenuTypes.registerMenuTypes();
        OpenShulkerPacket.registerReceivePacket();
        QuickBundlePacket.registerReceivePacket();
    }

    /** Opens a bundle with the one menu protocol understood by this client. */
    public static void openBundle(ServerPlayer player, ItemStack stack,
                                  Runnable modernV4Open) {
        openBundle(classify(player), player, stack, modernV4Open);
    }

    static void openBundle(ClientKind client, ServerPlayer player,
                           ItemStack stack, Runnable modernV4Open) {
        switch (client) {
            case ORIGINAL_V3 -> openLegacyBundle(player, stack);
            case V4 -> modernV4Open.run();
            case VANILLA -> openVanillaBundle(player, stack);
        }
    }

    static ClientKind classify(ServerPlayer player) {
        return classify(
                ServerPlayNetworking.canSend(player, OpenInventoryPacket.OPEN_INV_ID),
                ServerPlayNetworking.canSend(player, ShulkerTransferResultPacket.ID));
    }

    static ClientKind classify(boolean acceptsOriginalV3Packets,
                               boolean acceptsV4TransferResults) {
        if (acceptsV4TransferResults) return ClientKind.V4;
        if (acceptsOriginalV3Packets) return ClientKind.ORIGINAL_V3;
        return ClientKind.VANILLA;
    }

    private static void openLegacyBundle(ServerPlayer player, ItemStack stack) {
        player.openMenu(new SimpleMenuProvider(
                (containerId, inventory, ignored) -> new LegacyBundleItemMenu(
                        containerId, inventory, new BundleContainer(stack, 64)),
                title(stack)));
    }

    private static void openVanillaBundle(ServerPlayer player, ItemStack stack) {
        player.openMenu(new SimpleMenuProvider(
                (containerId, inventory, ignored) -> new PagedBundleItemMenu(
                        containerId, inventory, new BundleContainer(stack, 64)),
                title(stack)));
    }

    private static Component title(ItemStack stack) {
        return stack.getComponents().has(DataComponents.CUSTOM_NAME)
                ? stack.getHoverName()
                : Component.translatable("item.minecraft.bundle");
    }

    enum ClientKind {
        VANILLA,
        ORIGINAL_V3,
        V4
    }
}
