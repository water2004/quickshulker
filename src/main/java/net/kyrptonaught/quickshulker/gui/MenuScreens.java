package net.kyrptonaught.quickshulker.gui;

import net.kyrptonaught.quickshulker.gui.screen.BundleItemScreen;

/** Client screen registration for the original v3 bundle menu identifier. */
public final class MenuScreens {
    private MenuScreens() {
    }

    public static void registerMenuScreens() {
        net.minecraft.client.gui.screens.MenuScreens.register(
                MenuTypes.BUNDLE_ITEM, BundleItemScreen::new);
    }
}
