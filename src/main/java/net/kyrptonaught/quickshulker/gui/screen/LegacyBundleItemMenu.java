package net.kyrptonaught.quickshulker.gui.screen;

import net.kyrptonaught.quickshulker.gui.MenuTypes;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;

/**
 * Exact server-side slot layout used by original Quick Shulker v3 clients.
 * The separate type prevents the legacy custom menu from leaking into the v4
 * and vanilla-client presentation paths.
 */
public final class LegacyBundleItemMenu extends BundleItemMenu {
    private static final int CONTAINER_SIZE = 64;

    public LegacyBundleItemMenu(int syncId, Inventory playerInventory) {
        this(syncId, playerInventory, new BundleContainer(CONTAINER_SIZE));
    }

    public LegacyBundleItemMenu(int syncId, Inventory playerInventory,
                                Container container) {
        super(MenuTypes.BUNDLE_ITEM, syncId, playerInventory, container);
    }
}
