package net.kyrptonaught.quickshulker.internal.legacy;

import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.kyrptonaught.quickshulker.api.Util;
import net.kyrptonaught.quickshulker.gui.MenuTypes;
import net.kyrptonaught.quickshulker.gui.screen.LegacyBundleItemMenu;
import net.kyrptonaught.quickshulker.gui.screen.PagedBundleItemMenu;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.ShulkerBoxMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.concurrent.atomic.AtomicInteger;

public final class OriginalV3CompatibilityGameTests {
    private static final int PLAYER_SLOT = 9;

    @GameTest
    public void originalV3GetsItsCustomBundleMenuAndExactSlotLayout(
            GameTestHelper helper) {
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        OriginalV3Compatibility.openBundle(
                OriginalV3Compatibility.ClientKind.ORIGINAL_V3,
                player, new ItemStack(Items.BUNDLE),
                () -> helper.fail("The v4 bundle path must not run for a v3 client"));

        helper.assertTrue(player.containerMenu instanceof LegacyBundleItemMenu,
                "Original v3 must receive its custom bundle menu");
        helper.assertTrue(player.containerMenu.getType() == MenuTypes.BUNDLE_ITEM,
                "Original v3 menu identifier must remain quickshulker:bundle_item");
        helper.assertValueEqual(player.containerMenu.slots.size(), 100,
                "Original v3 expects 64 bundle and 36 player slots");
        for (int slot = 0; slot < 64; slot++) {
            helper.assertValueEqual(
                    player.containerMenu.getSlot(slot).getContainerSlot(), slot,
                    "Original v3 bundle slot order changed at " + slot);
        }
        for (int slot = 0; slot < 27; slot++) {
            helper.assertValueEqual(
                    player.containerMenu.getSlot(64 + slot).getContainerSlot(),
                    9 + slot,
                    "Original v3 main-inventory slot order changed at " + slot);
        }
        for (int slot = 0; slot < 9; slot++) {
            helper.assertValueEqual(
                    player.containerMenu.getSlot(91 + slot).getContainerSlot(), slot,
                    "Original v3 hotbar slot order changed at " + slot);
        }
        helper.succeed();
    }

    @GameTest
    public void vanillaGetsOnlyTheVanillaPagedBundleMenu(GameTestHelper helper) {
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        player.getInventory().setItem(PLAYER_SLOT, new ItemStack(Items.BUNDLE));
        Util.openItem(player, 0, PLAYER_SLOT);

        helper.assertTrue(player.containerMenu instanceof PagedBundleItemMenu,
                "Vanilla must receive the screen-independent paged menu");
        helper.assertTrue(player.containerMenu.getType() == MenuType.GENERIC_9x6,
                "Vanilla must receive a vanilla menu identifier");
        helper.assertValueEqual(player.containerMenu.slots.size(), 90,
                "Vanilla expects 54 container and 36 player slots");
        helper.succeed();
    }

    @GameTest
    public void vanillaGetsTheVanillaShulkerMenu(GameTestHelper helper) {
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        player.getInventory().setItem(
                PLAYER_SLOT, new ItemStack(Items.SHULKER_BOX));
        Util.openItem(player, 0, PLAYER_SLOT);

        helper.assertTrue(player.containerMenu instanceof ShulkerBoxMenu,
                "Vanilla must receive Minecraft's standard shulker menu");
        helper.assertTrue(player.containerMenu.getType() == MenuType.SHULKER_BOX,
                "The shulker menu must use a vanilla identifier");
        helper.succeed();
    }

    @GameTest
    public void v4UsesOnlyTheModernBundleCallback(GameTestHelper helper) {
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        AtomicInteger modernOpens = new AtomicInteger();
        OriginalV3Compatibility.openBundle(
                OriginalV3Compatibility.ClientKind.V4,
                player, new ItemStack(Items.BUNDLE), modernOpens::incrementAndGet);

        helper.assertValueEqual(modernOpens.get(), 1,
                "The v4 path must invoke its modern opener exactly once");
        helper.assertTrue(!(player.containerMenu instanceof LegacyBundleItemMenu),
                "The v4 path must not silently fall back to the original v3 menu");
        helper.assertTrue(!(player.containerMenu instanceof PagedBundleItemMenu),
                "The v4 path must not silently fall back to the vanilla menu");
        helper.succeed();
    }
}
