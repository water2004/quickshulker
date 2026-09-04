package net.kyrptonaught.quickshulker.gui;

import net.kyrptonaught.quickshulker.QuickShulkerMod;
import net.kyrptonaught.quickshulker.gui.screen.LegacyBundleItemMenu;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.inventory.MenuType;

/** Menu identifiers retained for original Quick Shulker v3 clients. */
public final class MenuTypes {
    public static final MenuType<LegacyBundleItemMenu> BUNDLE_ITEM = Registry.register(
            BuiltInRegistries.MENU,
            Identifier.fromNamespaceAndPath(QuickShulkerMod.MOD_ID, "bundle_item"),
            new MenuType<>(LegacyBundleItemMenu::new, FeatureFlagSet.of()));

    private MenuTypes() {
    }

    public static void registerMenuTypes() {
        // Loading this class performs the vanilla registry registration.
    }
}
