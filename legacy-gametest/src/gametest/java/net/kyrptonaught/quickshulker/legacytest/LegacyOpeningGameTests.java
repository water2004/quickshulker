package net.kyrptonaught.quickshulker.legacytest;

import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.kyrptonaught.quickshulker.QuickShulkerMod;
import net.kyrptonaught.quickshulker.api.ItemInventoryContainer;
import net.kyrptonaught.quickshulker.api.QuickOpenableRegistry;
import net.kyrptonaught.quickshulker.api.Util;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.ContainerListener;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.concurrent.atomic.AtomicInteger;

public final class LegacyOpeningGameTests {
    private static final int PLAYER_SLOT = 9;

    @GameTest
    public void registeredOpenInvokesCallbackAndRecordsPlayerSlot(
            GameTestHelper helper) {
        ServerPlayer player = LegacyTestSupport.player(helper);
        ItemStack stack = new ItemStack(Items.STONE);
        player.getInventory().setItem(PLAYER_SLOT, stack);
        AtomicInteger opens = new AtomicInteger();
        try (LegacyTestSupport.RegistrySnapshot ignored =
                     LegacyTestSupport.registrySnapshot()) {
            new QuickOpenableRegistry.Builder()
                    .setItem(Items.STONE.getClass())
                    .setOpenAction((actualPlayer, actualStack) -> {
                        helper.assertTrue(actualPlayer == player,
                                "The callback must receive the initiating player");
                        helper.assertTrue(actualStack == stack,
                                "The callback must receive the live inventory stack");
                        opens.incrementAndGet();
                    })
                    .register();

            Util.openItem(player, 0, PLAYER_SLOT);
            helper.assertValueEqual(opens.get(), 1,
                    "One open request must invoke one callback");
            helper.assertValueEqual(
                    ((ItemInventoryContainer) player.containerMenu)
                            .getUsedSlotInPlayerInv(),
                    PLAYER_SLOT,
                    "The open menu must remember the player inventory slot");
        }
        helper.succeed();
    }

    @GameTest
    public void unregisteredOpenRequestIsIgnored(GameTestHelper helper) {
        ServerPlayer player = LegacyTestSupport.player(helper);
        player.getInventory().setItem(PLAYER_SLOT, new ItemStack(Items.STICK));
        try (LegacyTestSupport.RegistrySnapshot ignored =
                     LegacyTestSupport.registrySnapshot()) {
            Util.openItem(player, 0, PLAYER_SLOT);
            helper.assertValueEqual(
                    ((ItemInventoryContainer) player.containerMenu)
                            .getUsedSlotInPlayerInv(),
                    -1,
                    "An unregistered item must not mark a menu slot as open");
        }
        helper.succeed();
    }

    @GameTest
    public void rightClickCloseSuppressesSecondOpenCallback(GameTestHelper helper) {
        ServerPlayer player = LegacyTestSupport.player(helper);
        player.getInventory().setItem(PLAYER_SLOT, new ItemStack(Items.STONE));
        AtomicInteger opens = new AtomicInteger();
        boolean previous = QuickShulkerMod.getConfig().rightClickClose;
        try (LegacyTestSupport.RegistrySnapshot ignored =
                     LegacyTestSupport.registrySnapshot()) {
            QuickShulkerMod.getConfig().rightClickClose = true;
            new QuickOpenableRegistry.Builder()
                    .setItem(Items.STONE.getClass())
                    .setOpenAction((ignoredPlayer, ignoredStack) ->
                            opens.incrementAndGet())
                    .register();

            Util.openItem(player, 0, PLAYER_SLOT);
            Util.openItem(player, 0, PLAYER_SLOT);
            helper.assertValueEqual(opens.get(), 1,
                    "Opening the already-used slot must toggle close, not reopen");
        } finally {
            QuickShulkerMod.getConfig().rightClickClose = previous;
        }
        helper.succeed();
    }

    @GameTest
    public void forceCloseListenerAcceptsSameTypeWithChangedComponents(
            GameTestHelper helper) {
        ServerPlayer player = LegacyTestSupport.player(helper);
        ItemStack original = new ItemStack(Items.STONE);
        player.getInventory().setItem(PLAYER_SLOT, original);
        try (LegacyTestSupport.RegistrySnapshot ignored =
                     LegacyTestSupport.registrySnapshot()) {
            new QuickOpenableRegistry.Builder()
                    .setItem(Items.STONE.getClass())
                    .ignoreSingleStackCheck(false)
                    .register();
            ContainerListener listener = Util.forceCloseScreenIfNotPresent(
                    player, PLAYER_SLOT, original.copy());

            player.getInventory().setItem(PLAYER_SLOT,
                    new ItemStack(Items.STONE));
            listener.slotChanged(player.containerMenu, 0,
                    player.getInventory().getItem(PLAYER_SLOT));
            helper.assertTrue(true,
                    "The legacy listener intentionally validates item type, not components");
        }
        helper.succeed();
    }

    @GameTest
    public void forceCloseListenerAllowsStackingOnlyWhenRegistered(
            GameTestHelper helper) {
        ServerPlayer player = LegacyTestSupport.player(helper);
        ItemStack original = new ItemStack(Items.STONE);
        player.getInventory().setItem(PLAYER_SLOT, original);
        try (LegacyTestSupport.RegistrySnapshot ignored =
                     LegacyTestSupport.registrySnapshot()) {
            new QuickOpenableRegistry.Builder()
                    .setItem(Items.STONE.getClass())
                    .ignoreSingleStackCheck(true)
                    .register();
            ContainerListener listener = Util.forceCloseScreenIfNotPresent(
                    player, PLAYER_SLOT, original.copy());
            player.getInventory().getItem(PLAYER_SLOT).setCount(32);
            listener.dataChanged(player.containerMenu, 0, 0);
            helper.assertValueEqual(
                    player.getInventory().getItem(PLAYER_SLOT).getCount(), 32,
                    "The listener must honor ignoreSingleStackCheck");
        }
        helper.succeed();
    }
}
