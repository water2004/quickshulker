package net.kyrptonaught.quickshulker.legacytest;

import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.kyrptonaught.shulkerutils.ItemStackInventory;
import net.kyrptonaught.shulkerutils.ShulkerUtils;
import net.minecraft.core.component.DataComponents;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public final class LegacyItemStackInventoryGameTests {
    @GameTest
    public void inventoryLoadsHostContentsAndRequestedSize(GameTestHelper helper) {
        ItemStack host = LegacyTestSupport.box(
                new ItemStack(Items.DIRT, 3),
                new ItemStack(Items.STONE, 5));
        ItemStackInventory inventory = new ItemStackInventory(host, 27);

        helper.assertValueEqual(inventory.getContainerSize(), 27,
                "The wrapper must expose the requested slot count");
        helper.assertValueEqual(inventory.getItem(0).getCount(), 3,
                "The first component slot must be loaded");
        helper.assertValueEqual(inventory.getItem(1).getCount(), 5,
                "The second component slot must be loaded");
        helper.succeed();
    }

    @GameTest
    public void setItemImmediatelySerializesTheWholeHost(GameTestHelper helper) {
        ItemStack host = LegacyTestSupport.box(new ItemStack(Items.DIRT, 3));
        ItemStackInventory inventory = new ItemStackInventory(host, 27);

        inventory.setItem(4, new ItemStack(Items.STONE, 7));

        helper.assertValueEqual(LegacyTestSupport.count(host, Items.DIRT), 3,
                "Existing host contents must survive serialization");
        helper.assertValueEqual(LegacyTestSupport.count(host, Items.STONE), 7,
                "setItem must immediately update the host component");
        helper.succeed();
    }

    @GameTest
    public void removeItemImmediatelySerializesTheRemainder(GameTestHelper helper) {
        ItemStack host = LegacyTestSupport.box(new ItemStack(Items.STONE, 10));
        ItemStackInventory inventory = new ItemStackInventory(host, 27);

        ItemStack removed = inventory.removeItem(0, 4);

        helper.assertValueEqual(removed.getCount(), 4,
                "removeItem must return the requested amount");
        helper.assertValueEqual(LegacyTestSupport.count(host, Items.STONE), 6,
                "The host component must contain the remaining amount");
        helper.succeed();
    }

    @GameTest
    public void stopOpenSplitsStackedHostIntoEmptyExtras(GameTestHelper helper) {
        ServerPlayer player = LegacyTestSupport.player(helper);
        ItemStack host = LegacyTestSupport.box(new ItemStack(Items.STONE, 5));
        host.setCount(3);
        ItemStackInventory inventory = new ItemStackInventory(host, 27);

        inventory.stopOpen(player);

        helper.assertValueEqual(host.getCount(), 1,
                "The active storage host must become singular");
        int extraBoxes = 0;
        int extraStoredStone = 0;
        for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) {
            ItemStack stack = player.getInventory().getItem(slot);
            if (stack.is(Items.SHULKER_BOX)) {
                extraBoxes += stack.getCount();
                extraStoredStone += LegacyTestSupport.count(stack, Items.STONE);
            }
        }
        helper.assertValueEqual(extraBoxes, 2,
                "The remaining stacked boxes must be returned to the player");
        helper.assertValueEqual(extraStoredStone, 0,
                "Only the singular active box keeps the container component");
        helper.assertValueEqual(LegacyTestSupport.count(host, Items.STONE), 5,
                "The active box must preserve its contents");
        helper.succeed();
    }

    @GameTest
    public void containsAnyComparesItemTypeOnly(GameTestHelper helper) {
        ItemStack namedStone = new ItemStack(Items.STONE, 1);
        namedStone.set(DataComponents.CUSTOM_NAME, Component.literal("named"));
        SimpleContainer inventory = new SimpleContainer(namedStone);

        helper.assertTrue(ShulkerUtils.shulkerContainsAny(
                        inventory, new ItemStack(Items.STONE, 64)),
                "Containment intentionally ignores count and components");
        helper.assertTrue(!ShulkerUtils.shulkerContainsAny(
                        inventory, new ItemStack(Items.DIRT)),
                "A different item type must not match");
        helper.succeed();
    }

    @GameTest
    public void utilityInsertionRejectsNestedShulkers(GameTestHelper helper) {
        ServerPlayer player = LegacyTestSupport.player(helper);
        SimpleContainer inventory = new SimpleContainer(27);
        ItemStack nested = LegacyTestSupport.box();

        ItemStack remainder = ShulkerUtils.insertIntoShulker(
                inventory, nested, player);

        helper.assertTrue(remainder == nested,
                "Rejected nested storage must be returned unchanged");
        helper.assertTrue(inventory.isEmpty(),
                "Rejected nested storage must not mutate the destination");
        helper.succeed();
    }

    @GameTest
    public void utilityInsertionReturnsOnlyCapacityRemainder(GameTestHelper helper) {
        ServerPlayer player = LegacyTestSupport.player(helper);
        LegacyTestSupport.TrackingContainer inventory =
                new LegacyTestSupport.TrackingContainer(1);
        inventory.setItem(0, new ItemStack(Items.STONE, 62));
        ItemStack offered = new ItemStack(Items.STONE, 8);

        ItemStack remainder = ShulkerUtils.insertIntoShulker(
                inventory, offered, player);

        helper.assertValueEqual(inventory.getItem(0).getCount(), 64,
                "The destination must fill to capacity");
        helper.assertValueEqual(remainder.getCount(), 6,
                "Only the unaccepted remainder must be returned");
        helper.assertValueEqual(inventory.closeCount, 1,
                "A successful utility insertion must finish the container once");
        helper.succeed();
    }
}
