package net.kyrptonaught.quickshulker.internal.menu;

import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.kyrptonaught.quickshulker.api.Util;
import net.kyrptonaught.quickshulker.gui.screen.PagedBundleItemMenu;
import net.kyrptonaught.quickshulker.internal.compat.ClientProtocolResolver.ClientProtocol;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.item.component.BundleContents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.ShulkerBoxMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.concurrent.atomic.AtomicInteger;

public final class BundleMenuRouterGameTests {
    private static final int PLAYER_SLOT = 9;

    @GameTest
    public void originalV3CanExtractContentsBeyondFirstPage(GameTestHelper helper) {
        verifySecondPage(helper, ClientProtocol.ORIGINAL_V3);
    }

    @GameTest
    public void vanillaCanExtractContentsBeyondFirstPage(GameTestHelper helper) {
        verifySecondPage(helper, ClientProtocol.VANILLA);
    }

    private static void verifySecondPage(GameTestHelper helper, ClientProtocol protocol) {
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        var contents = new BundleContents.Mutable(BundleContents.EMPTY);
        contents.tryInsert(new ItemStack(Items.DIAMOND, 4));
        int preceding = 0;
        for (var item : BuiltInRegistries.ITEM) {
            ItemStack filler = new ItemStack(item);
            if (filler.isEmpty() || item == Items.DIAMOND || filler.getMaxStackSize() != 64) continue;
            if (contents.tryInsert(filler) == 1 && ++preceding == 45) break;
        }
        helper.assertValueEqual(preceding, 45, "Fixture must fill the first bundle page");
        ItemStack bundle = new ItemStack(Items.BUNDLE);
        bundle.set(DataComponents.BUNDLE_CONTENTS, contents.toImmutable());
        player.getInventory().setItem(PLAYER_SLOT, bundle);
        BundleMenuRouter.open(protocol, player, bundle,
                () -> helper.fail("Unexpected enhanced bundle path"));
        player.containerMenu.clicked(50, 0, ContainerInput.PICKUP, player);
        helper.assertTrue(player.containerMenu.getSlot(0).getItem().is(Items.DIAMOND),
                "Second page must expose the last bundle entry");
        player.containerMenu.quickMoveStack(player, 0);
        player.closeContainer();
        int diamonds = player.getInventory().getNonEquipmentItems().stream()
                .filter(stack -> stack.is(Items.DIAMOND)).mapToInt(ItemStack::getCount).sum();
        helper.assertValueEqual(diamonds, 4, "All diamonds must reach the player inventory");
        helper.succeed();
    }

    @GameTest
    public void originalV3GetsTheVanillaPagedBundleMenu(
            GameTestHelper helper) {
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        BundleMenuRouter.open(
                ClientProtocol.ORIGINAL_V3,
                player, new ItemStack(Items.BUNDLE),
                () -> helper.fail("The v4 bundle path must not run for a v3 client"));

        helper.assertTrue(player.containerMenu instanceof PagedBundleItemMenu,
                "Original v3 must use the vanilla-compatible paged presentation");
        helper.assertTrue(player.containerMenu.getType() == MenuType.GENERIC_9x6,
                "Original v3 must not require a custom registry entry");
        helper.assertValueEqual(player.containerMenu.slots.size(), 90,
                "Vanilla chest layout must contain 54 container and 36 player slots");
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
        BundleMenuRouter.open(
                ClientProtocol.V4,
                player, new ItemStack(Items.BUNDLE), modernOpens::incrementAndGet);

        helper.assertValueEqual(modernOpens.get(), 1,
                "The v4 path must invoke its modern opener exactly once");
        helper.assertTrue(!(player.containerMenu instanceof PagedBundleItemMenu),
                "The v4 path must not silently fall back to the vanilla menu");
        helper.succeed();
    }
}
