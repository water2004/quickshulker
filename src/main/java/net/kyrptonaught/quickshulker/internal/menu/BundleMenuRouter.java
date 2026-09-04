package net.kyrptonaught.quickshulker.internal.menu;

import net.kyrptonaught.quickshulker.gui.screen.BundleContainer;
import net.kyrptonaught.quickshulker.gui.screen.LegacyBundleItemMenu;
import net.kyrptonaught.quickshulker.gui.screen.PagedBundleItemMenu;
import net.kyrptonaught.quickshulker.internal.compat.ClientProtocolResolver;
import net.kyrptonaught.quickshulker.internal.compat.ClientProtocolResolver.ClientProtocol;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.item.ItemStack;

/** Selects one bundle presentation before opening; it never retries or falls back. */
public final class BundleMenuRouter {
    private BundleMenuRouter() {
    }

    public static void open(ServerPlayer player, ItemStack stack,
                            Runnable modernV4Open) {
        open(ClientProtocolResolver.resolve(player), player, stack, modernV4Open);
    }

    static void open(ClientProtocol protocol, ServerPlayer player,
                     ItemStack stack, Runnable modernV4Open) {
        switch (protocol) {
            case ORIGINAL_V3 -> openOriginalV3(player, stack);
            case V4 -> modernV4Open.run();
            case VANILLA -> openVanilla(player, stack);
        }
    }

    private static void openOriginalV3(ServerPlayer player, ItemStack stack) {
        player.openMenu(new SimpleMenuProvider(
                (containerId, inventory, ignored) -> new LegacyBundleItemMenu(
                        containerId, inventory, new BundleContainer(stack, 64)),
                title(stack)));
    }

    private static void openVanilla(ServerPlayer player, ItemStack stack) {
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
}
