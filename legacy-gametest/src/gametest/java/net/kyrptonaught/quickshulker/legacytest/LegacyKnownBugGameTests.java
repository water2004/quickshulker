package net.kyrptonaught.quickshulker.legacytest;

import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.kyrptonaught.quickshulker.api.QuickOpenableRegistry;
import net.kyrptonaught.quickshulker.api.QuickShulkerData;
import net.kyrptonaught.quickshulker.api.Util;
import net.kyrptonaught.quickshulker.util.BundleHelper;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.CompoundContainer;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.ShulkerBoxBlock;

import java.util.IdentityHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public final class LegacyKnownBugGameTests {
    /** QS-LB-001: retained deprecated-API compatibility quirk. */
    @GameTest
    @SuppressWarnings("deprecation")
    public void qsLb001DeprecatedSingularArgumentIsIgnored(GameTestHelper helper) {
        try (LegacyTestSupport.RegistrySnapshot ignored =
                     LegacyTestSupport.registrySnapshot()) {
            QuickOpenableRegistry.register(
                    LegacyTestSupport.FirstItemLike.class,
                    false,
                    false,
                    (player, stack) -> { });
            QuickShulkerData data = QuickOpenableRegistry.getQuickie(
                    new LegacyTestSupport.FirstItemLike());
            helper.assertTrue(!data.ignoreSingleStackCheck,
                    "QS-LB-001: the deprecated requiresSingularStack=false "
                            + "argument was and remains ignored");
        }
        helper.succeed();
    }

    /** QS-LB-002: 3.0.4 rejected valid non-SimpleContainer registrations. */
    @GameTest
    public void qsLb002BoxTransferSupportsAnyContainer(GameTestHelper helper) {
        ServerPlayer player = LegacyTestSupport.player(helper);
        ItemStack sourceHost = LegacyTestSupport.box();
        ItemStack targetHost = LegacyTestSupport.box();
        CompoundContainer source = new CompoundContainer(
                new SimpleContainer(new ItemStack(Items.STONE, 6)),
                new SimpleContainer(1));
        CompoundContainer target = new CompoundContainer(
                new SimpleContainer(1), new SimpleContainer(1));
        Map<ItemStack, CompoundContainer> inventories = new IdentityHashMap<>();
        inventories.put(sourceHost, source);
        inventories.put(targetHost, target);

        try (LegacyTestSupport.RegistrySnapshot ignored =
                     LegacyTestSupport.registrySnapshot()) {
            new QuickOpenableRegistry.Builder()
                    .setItem(ShulkerBoxBlock.class)
                    .supportsBundleing(true)
                    .getBundleInv((ignoredPlayer, host) -> inventories.get(host))
                    .register();

            if (LegacyTestSupport.baseline()) {
                LegacyTestSupport.assertThrows(helper, ClassCastException.class,
                        () -> BundleHelper.transferItemsToShulker(
                                player, targetHost, sourceHost, null),
                        "QS-LB-002 must reproduce the 3.0.4 concrete-container cast");
                helper.assertValueEqual(target.countItem(Items.STONE), 0,
                        "The baseline failure must occur before mutation");
            } else {
                BundleHelper.transferItemsToShulker(
                        player, targetHost, sourceHost, null);
                helper.assertValueEqual(target.countItem(Items.STONE), 6,
                        "Current code must honor the public Container abstraction");
                helper.assertValueEqual(source.countItem(Items.STONE), 0,
                        "A successful current transfer must debit the source");
            }
        }
        helper.succeed();
    }

    /** QS-LB-003: 3.0.4 bypassed the target registration's insertion rule. */
    @GameTest
    public void qsLb003BoxTransferEnforcesDestinationPolicy(GameTestHelper helper) {
        ServerPlayer player = LegacyTestSupport.player(helper);
        ItemStack source = LegacyTestSupport.box(new ItemStack(Items.STONE, 6));
        ItemStack target = LegacyTestSupport.box();
        try (LegacyTestSupport.RegistrySnapshot ignored =
                     LegacyTestSupport.registrySnapshot()) {
            new QuickOpenableRegistry.Builder()
                    .setItem(ShulkerBoxBlock.class)
                    .supportsBundleing(true)
                    .canBundleInsertItem((ignoredPlayer, ignoredInventory,
                                         host, offered) -> host != target)
                    .register();

            BundleHelper.transferItemsToShulker(player, target, source, null);
        }

        if (LegacyTestSupport.baseline()) {
            helper.assertValueEqual(LegacyTestSupport.count(target, Items.STONE), 6,
                    "QS-LB-003 must reproduce the 3.0.4 policy bypass");
            helper.assertValueEqual(LegacyTestSupport.count(source, Items.STONE), 0,
                    "The baseline bypass moved the rejected items");
        } else {
            helper.assertValueEqual(LegacyTestSupport.count(target, Items.STONE), 0,
                    "Current code must keep rejected items out of the destination");
            helper.assertValueEqual(LegacyTestSupport.count(source, Items.STONE), 6,
                    "A rejected current transfer must preserve the source");
        }
        helper.succeed();
    }

    /** QS-LB-004: 3.0.4 coupled persistence to a mixin callback object. */
    @GameTest
    public void qsLb004NullCallbackStillPersistsBoxTransfer(GameTestHelper helper) {
        ServerPlayer player = LegacyTestSupport.player(helper);
        ItemStack sourceHost = LegacyTestSupport.box(new ItemStack(Items.STONE, 6));
        ItemStack targetHost = LegacyTestSupport.box();
        LegacyTestSupport.DelayedHostContainer source =
                new LegacyTestSupport.DelayedHostContainer(sourceHost);
        LegacyTestSupport.DelayedHostContainer target =
                new LegacyTestSupport.DelayedHostContainer(targetHost);
        Map<ItemStack, LegacyTestSupport.DelayedHostContainer> inventories =
                new IdentityHashMap<>();
        inventories.put(sourceHost, source);
        inventories.put(targetHost, target);

        try (LegacyTestSupport.RegistrySnapshot ignored =
                     LegacyTestSupport.registrySnapshot()) {
            new QuickOpenableRegistry.Builder()
                    .setItem(ShulkerBoxBlock.class)
                    .supportsBundleing(true)
                    .getBundleInv((ignoredPlayer, host) -> inventories.get(host))
                    .register();
            BundleHelper.transferItemsToShulker(
                    player, targetHost, sourceHost, null);
        }

        if (LegacyTestSupport.baseline()) {
            helper.assertValueEqual(target.closeCount, 0,
                    "QS-LB-004 must reproduce the callback-dependent close");
            helper.assertValueEqual(LegacyTestSupport.count(targetHost, Items.STONE), 0,
                    "The detached baseline target was not persisted");
            helper.assertValueEqual(LegacyTestSupport.count(sourceHost, Items.STONE), 6,
                    "The detached baseline source was not persisted");
        } else {
            helper.assertValueEqual(target.closeCount, 1,
                    "Current persistence must not depend on a callback object");
            helper.assertValueEqual(source.closeCount, 1,
                    "Both current containers must finish together");
            helper.assertValueEqual(LegacyTestSupport.count(targetHost, Items.STONE), 6,
                    "The current destination must contain the moved items");
            helper.assertValueEqual(LegacyTestSupport.count(sourceHost, Items.STONE), 0,
                    "The current source must be persisted as empty");
        }
        helper.succeed();
    }

    /** QS-LB-005: currently retained observable eager inventory lookup. */
    @GameTest
    public void qsLb005DisabledUnbundleStillResolvesInventory(GameTestHelper helper) {
        ServerPlayer player = LegacyTestSupport.player(helper);
        AtomicInteger lookups = new AtomicInteger();
        try (LegacyTestSupport.RegistrySnapshot ignored =
                     LegacyTestSupport.registrySnapshot()) {
            new QuickOpenableRegistry.Builder()
                    .setItem(ShulkerBoxBlock.class)
                    .supportsBundleing(true)
                    .getBundleInv((ignoredPlayer, ignoredHost) -> {
                        lookups.incrementAndGet();
                        return new SimpleContainer(1);
                    })
                    .register();
            boolean attempted = BundleHelper.shouldAttemptUnBundle(
                    player, ClickAction.PRIMARY, LegacyTestSupport.box(),
                    new ItemStack(Items.STONE), false);

            helper.assertTrue(!attempted,
                    "An ineligible disabled click must not attempt extraction");
            helper.assertValueEqual(lookups.get(), 1,
                    "QS-LB-005 records the retained eager inventory lookup");
        }
        helper.succeed();
    }

    /** QS-LB-006: currently retained invalid-input failure. */
    @GameTest
    public void qsLb006NegativeOpenSlotStillFailsFast(GameTestHelper helper) {
        ServerPlayer player = LegacyTestSupport.player(helper);
        LegacyTestSupport.assertThrows(helper, IndexOutOfBoundsException.class,
                () -> Util.openItem(player, -1),
                "QS-LB-006 records the legacy negative-slot failure");
        helper.succeed();
    }

    /** QS-LB-007: 3.0.4 treated a zero-item safeInsert as success. */
    @GameTest
    public void qsLb007UnbundleSkipsZeroMoveCandidate(GameTestHelper helper) {
        ServerPlayer player = LegacyTestSupport.player(helper);
        ItemStack host = LegacyTestSupport.box(
                new ItemStack(Items.DIRT, 8),
                new ItemStack(Items.STONE, 12));
        player.getInventory().setItem(10, new ItemStack(Items.DIRT, 60));
        Slot output = new Slot(player.getInventory(), 10, 0, 0);

        ItemStack result = BundleHelper.unbundleItem(player, host, output);

        if (LegacyTestSupport.baseline()) {
            helper.assertTrue(result != null,
                    "QS-LB-007 must reproduce the false-positive baseline result");
            helper.assertValueEqual(player.getInventory().getItem(10).getCount(), 60,
                    "The baseline stopped after moving zero items");
            helper.assertValueEqual(LegacyTestSupport.count(host, Items.DIRT), 8,
                    "The compatible earlier item was never considered in 3.0.4");
        } else {
            helper.assertTrue(result != null,
                    "Current code must find the compatible earlier item");
            helper.assertValueEqual(player.getInventory().getItem(10).getCount(), 64,
                    "Current code must fill the destination's remaining capacity");
            helper.assertValueEqual(LegacyTestSupport.count(host, Items.DIRT), 4,
                    "Current code must preserve only the unmoved remainder");
        }
        helper.assertValueEqual(LegacyTestSupport.count(host, Items.STONE), 12,
                "The incompatible candidate itself must remain untouched");
        helper.succeed();
    }

    /** QS-LB-007 companion case: no candidate can move at all. */
    @GameTest
    public void qsLb007UnbundleReportsNoTransferWhenEveryMoveIsZero(
            GameTestHelper helper) {
        ServerPlayer player = LegacyTestSupport.player(helper);
        ItemStack host = LegacyTestSupport.box(new ItemStack(Items.STONE, 12));
        player.getInventory().setItem(10, new ItemStack(Items.DIRT, 64));
        Slot output = new Slot(player.getInventory(), 10, 0, 0);

        ItemStack result = BundleHelper.unbundleItem(player, host, output);

        if (LegacyTestSupport.baseline()) {
            helper.assertTrue(result != null,
                    "QS-LB-007 must reproduce the baseline false success");
        } else {
            helper.assertTrue(result == null,
                    "Current code must report that no transfer occurred");
        }
        helper.assertValueEqual(LegacyTestSupport.count(host, Items.STONE), 12,
                "A zero-move extraction must never mutate the source");
        helper.succeed();
    }
}
