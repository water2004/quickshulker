package net.kyrptonaught.quickshulker.legacytest;

import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.kyrptonaught.quickshulker.api.QuickOpenableRegistry;
import net.kyrptonaught.quickshulker.util.BundleHelper;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.ShulkerBoxBlock;

import java.util.concurrent.atomic.AtomicInteger;

public final class LegacyTransferGameTests {
    private static final int SOURCE_SLOT = 9;
    private static final int OUTPUT_SLOT = 10;

    @GameTest
    public void bundlePredicateRequiresEligibleSecondaryClick(GameTestHelper helper) {
        ServerPlayer player = LegacyTestSupport.player(helper);
        ItemStack box = LegacyTestSupport.box();
        ItemStack stone = new ItemStack(Items.STONE);

        helper.assertTrue(BundleHelper.shouldAttemptBundle(
                        player, ClickAction.SECONDARY, box, stone, true),
                "An enabled secondary click with an ordinary item must bundle");
        helper.assertTrue(!BundleHelper.shouldAttemptBundle(
                        player, ClickAction.PRIMARY, box, stone, true),
                "A primary click must not bundle");
        helper.assertTrue(!BundleHelper.shouldAttemptBundle(
                        player, ClickAction.SECONDARY, box, stone, false),
                "The feature switch must gate bundling");
        helper.assertTrue(!BundleHelper.shouldAttemptBundle(
                        player, ClickAction.SECONDARY, box,
                        LegacyTestSupport.box(), true),
                "A nested shulker must not enter the normal bundle path");
        helper.assertTrue(!BundleHelper.shouldAttemptBundle(
                        player, ClickAction.SECONDARY, box,
                        ItemStack.EMPTY, true),
                "An empty source must not enter the bundle path");
        helper.succeed();
    }

    @GameTest
    public void unbundleAndBoxTransferPredicatesAreDistinct(GameTestHelper helper) {
        ServerPlayer player = LegacyTestSupport.player(helper);
        ItemStack filled = LegacyTestSupport.box(new ItemStack(Items.STONE, 1));
        ItemStack empty = LegacyTestSupport.box();

        helper.assertTrue(BundleHelper.shouldAttemptUnBundle(
                        player, ClickAction.SECONDARY, filled,
                        ItemStack.EMPTY, true),
                "A secondary click into an empty cursor must unbundle");
        helper.assertTrue(!BundleHelper.shouldAttemptUnBundle(
                        player, ClickAction.SECONDARY, empty,
                        ItemStack.EMPTY, true),
                "An empty host has nothing to unbundle");
        helper.assertTrue(BundleHelper.shouldAttemptTransfer(
                        player, ClickAction.SECONDARY, empty, filled, true),
                "Two singular shulkers must use the box-transfer path");
        filled.setCount(2);
        helper.assertTrue(!BundleHelper.shouldAttemptTransfer(
                        player, ClickAction.SECONDARY, empty, filled, true),
                "A stacked source shulker must not transfer");
        helper.succeed();
    }

    @GameTest
    public void directInsertionMergesBeforeUsingEmptySlots(GameTestHelper helper) {
        ServerPlayer player = LegacyTestSupport.player(helper);
        ItemStack host = LegacyTestSupport.box(
                new ItemStack(Items.STONE, 60),
                new ItemStack(Items.DIRT, 8));
        ItemStack offered = new ItemStack(Items.STONE, 70);

        BundleHelper.bundleItemIntoStack(player, host, offered, null);

        helper.assertValueEqual(offered.getCount(), 0,
                "A host with capacity must consume the complete offered stack");
        helper.assertValueEqual(LegacyTestSupport.count(host, Items.STONE), 130,
                "The existing stack must fill before the remainder uses an empty slot");
        helper.assertValueEqual(LegacyTestSupport.count(host, Items.DIRT), 8,
                "Unrelated contents must remain unchanged");
        helper.succeed();
    }

    @GameTest
    public void directInsertionConsumesOnlyAvailableCapacity(GameTestHelper helper) {
        ServerPlayer player = LegacyTestSupport.player(helper);
        ItemStack[] full = new ItemStack[27];
        for (int slot = 0; slot < full.length; slot++) {
            full[slot] = new ItemStack(Items.DIRT, 64);
        }
        full[4] = new ItemStack(Items.STONE, 62);
        ItemStack host = LegacyTestSupport.box(full);
        ItemStack offered = new ItemStack(Items.STONE, 10);

        BundleHelper.bundleItemIntoStack(player, host, offered, null);

        helper.assertValueEqual(offered.getCount(), 8,
                "Only two items fit in the host");
        helper.assertValueEqual(LegacyTestSupport.count(host, Items.STONE), 64,
                "Partial insertion must fill the compatible stack exactly");
        helper.succeed();
    }

    @GameTest
    public void rejectedInsertionDoesNotMutateEitherStack(GameTestHelper helper) {
        ServerPlayer player = LegacyTestSupport.player(helper);
        ItemStack host = LegacyTestSupport.box();
        ItemStack offered = new ItemStack(Items.STONE, 12);
        try (LegacyTestSupport.RegistrySnapshot ignored =
                     LegacyTestSupport.registrySnapshot()) {
            new QuickOpenableRegistry.Builder()
                    .setItem(ShulkerBoxBlock.class)
                    .supportsBundleing(true)
                    .canBundleInsertItem((ignoredPlayer, ignoredInventory,
                                         ignoredHost, ignoredOffered) -> false)
                    .register();

            BundleHelper.bundleItemIntoStack(player, host, offered, null);
        }

        helper.assertValueEqual(offered.getCount(), 12,
                "A rejected source stack must remain intact");
        helper.assertValueEqual(LegacyTestSupport.count(host, Items.STONE), 0,
                "A rejected destination must remain intact");
        helper.succeed();
    }

    @GameTest
    public void slotInsertionHonorsMayPickupBeforeMutatingHost(GameTestHelper helper) {
        ServerPlayer player = LegacyTestSupport.player(helper);
        player.getInventory().setItem(SOURCE_SLOT,
                new ItemStack(Items.STONE, 12));
        ItemStack host = LegacyTestSupport.box();
        Slot denied = new Slot(player.getInventory(), SOURCE_SLOT, 0, 0) {
            @Override
            public boolean mayPickup(Player ignored) {
                return false;
            }
        };

        BundleHelper.bundleItemIntoStack(player, host,
                player.getInventory().getItem(SOURCE_SLOT), denied, null);

        helper.assertValueEqual(
                player.getInventory().getItem(SOURCE_SLOT).getCount(), 12,
                "A protected source slot must not be debited");
        helper.assertValueEqual(LegacyTestSupport.count(host, Items.STONE), 0,
                "The host must not be credited before pickup permission succeeds");
        helper.succeed();
    }

    @GameTest
    public void slotInsertionDebitsExactlyWhatWasAccepted(GameTestHelper helper) {
        ServerPlayer player = LegacyTestSupport.player(helper);
        ItemStack[] full = new ItemStack[27];
        for (int slot = 0; slot < full.length; slot++) {
            full[slot] = new ItemStack(Items.DIRT, 64);
        }
        full[0] = new ItemStack(Items.STONE, 61);
        ItemStack host = LegacyTestSupport.box(full);
        player.getInventory().setItem(SOURCE_SLOT,
                new ItemStack(Items.STONE, 12));
        Slot source = new Slot(player.getInventory(), SOURCE_SLOT, 0, 0);

        BundleHelper.bundleItemIntoStack(player, host,
                player.getInventory().getItem(SOURCE_SLOT), source, null);

        helper.assertValueEqual(
                player.getInventory().getItem(SOURCE_SLOT).getCount(), 9,
                "The menu slot must lose exactly the accepted amount");
        helper.assertValueEqual(LegacyTestSupport.count(host, Items.STONE), 64,
                "The host must receive the same three items");
        helper.succeed();
    }

    @GameTest
    public void boxTransferMergesAndPreservesRemainders(GameTestHelper helper) {
        ServerPlayer player = LegacyTestSupport.player(helper);
        ItemStack source = LegacyTestSupport.box(
                new ItemStack(Items.DIRT, 8),
                new ItemStack(Items.STONE, 12));
        ItemStack destination = LegacyTestSupport.box(
                new ItemStack(Items.STONE, 60));

        BundleHelper.transferItemsToShulker(
                player, destination, source, null);

        helper.assertValueEqual(
                LegacyTestSupport.count(destination, Items.STONE), 72,
                "Compatible contents must merge and spill into empty slots");
        helper.assertValueEqual(
                LegacyTestSupport.count(destination, Items.DIRT), 8,
                "Different item types must retain their identity");
        helper.assertTrue(LegacyTestSupport.contents(source).isEmpty(),
                "A destination with sufficient capacity must drain the source");
        helper.succeed();
    }

    @GameTest
    public void playersAndHostsRemainIsolated(GameTestHelper helper) {
        ServerPlayer first = LegacyTestSupport.player(helper);
        ServerPlayer second = LegacyTestSupport.player(helper);
        ItemStack firstBox = LegacyTestSupport.box();
        ItemStack secondBox = LegacyTestSupport.box();
        ItemStack firstInput = new ItemStack(Items.STONE, 11);
        ItemStack secondInput = new ItemStack(Items.DIRT, 7);

        BundleHelper.bundleItemIntoStack(first, firstBox, firstInput, null);
        BundleHelper.bundleItemIntoStack(second, secondBox, secondInput, null);

        helper.assertValueEqual(LegacyTestSupport.count(firstBox, Items.STONE), 11,
                "The first player's host must receive only its own source");
        helper.assertValueEqual(LegacyTestSupport.count(firstBox, Items.DIRT), 0,
                "The second player's source must not leak into the first host");
        helper.assertValueEqual(LegacyTestSupport.count(secondBox, Items.DIRT), 7,
                "The second player's host must receive only its own source");
        helper.succeed();
    }

    @GameTest
    public void successfulDirectInsertionFinishesTheContainer(GameTestHelper helper) {
        ServerPlayer player = LegacyTestSupport.player(helper);
        LegacyTestSupport.TrackingContainer container =
                new LegacyTestSupport.TrackingContainer(27);
        AtomicInteger observedCount = new AtomicInteger();
        try (LegacyTestSupport.RegistrySnapshot ignored =
                     LegacyTestSupport.registrySnapshot()) {
            new QuickOpenableRegistry.Builder()
                    .setItem(ShulkerBoxBlock.class)
                    .supportsBundleing(true)
                    .getBundleInv((ignoredPlayer, ignoredHost) -> container)
                    .canBundleInsertItem((ignoredPlayer, ignoredInventory,
                                         ignoredHost, offered) -> {
                        observedCount.set(offered.getCount());
                        return true;
                    })
                    .register();
            ItemStack offered = new ItemStack(Items.STONE, 37);
            BundleHelper.bundleItemIntoStack(
                    player, LegacyTestSupport.box(), offered, null);

            helper.assertValueEqual(observedCount.get(), 37,
                    "The insertion policy must see the complete offered count");
            helper.assertValueEqual(container.closeCount, 1,
                    "One successful operation must finish the container once");
        }
        helper.succeed();
    }
}
