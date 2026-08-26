package net.kyrptonaught.quickshulker.gametest;

import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.kyrptonaught.quickshulker.api.QuickOpenableRegistry;
import net.kyrptonaught.quickshulker.api.QuickShulkerData;
import net.kyrptonaught.quickshulker.api.storage.CarriedStorageEndpoint;
import net.kyrptonaught.quickshulker.api.storage.IndexSelector;
import net.kyrptonaught.quickshulker.api.storage.PlayerInventoryEndpoint;
import net.kyrptonaught.quickshulker.api.storage.QuickStorageRegistry;
import net.kyrptonaught.quickshulker.api.storage.QuickStorageTransfer;
import net.kyrptonaught.quickshulker.api.storage.SlotSelector;
import net.kyrptonaught.quickshulker.api.storage.StackMatcher;
import net.kyrptonaught.quickshulker.api.storage.StorageSelector;
import net.kyrptonaught.quickshulker.api.storage.TransferLimit;
import net.kyrptonaught.quickshulker.api.storage.TransferResult;
import net.kyrptonaught.quickshulker.api.storage.TransferSpec;
import net.kyrptonaught.quickshulker.api.storage.TransferStatus;
import net.kyrptonaught.quickshulker.network.DirectTransferTransactions;
import net.kyrptonaught.quickshulker.util.BundleHelper;
import net.minecraft.core.component.DataComponents;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ItemContainerContents;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class QuickStorageGameTests {
    private static final int BOX_SLOT = 9;
    private static final int OUTPUT_SLOT = 10;

    @GameTest
    public void legacyAndDirectInsertUseTheSameStorageSemantics(GameTestHelper helper) {
        ServerPlayer player = player(helper);
        ItemStack legacyBox = box();
        ItemStack directBox = box();
        ItemStack legacyInput = new ItemStack(Items.STONE, 70);
        ItemStack directInput = legacyInput.copy();

        BundleHelper.bundleItemIntoStack(player, legacyBox, legacyInput, null);
        player.getInventory().setItem(0, directInput);
        player.getInventory().setItem(BOX_SLOT, directBox);
        TransferResult result = QuickStorageTransfer.execute(player, insertSpec(0, BOX_SLOT));

        helper.assertValueEqual(result.status(), TransferStatus.SUCCESS,
                "Direct insertion should succeed");
        helper.assertValueEqual(result.movedCount(), 70,
                "Direct insertion should move every input item");
        helper.assertValueEqual(legacyInput.getCount(),
                player.getInventory().getItem(0).getCount(),
                "Legacy and direct insertion should leave the same remainder");
        helper.assertTrue(sameContents(legacyBox, directBox),
                "Legacy and direct insertion must produce identical storage contents");
        helper.succeed();
    }

    @GameTest
    public void directExtractChoosesMatchingItemAndEmptyDestination(GameTestHelper helper) {
        ServerPlayer player = player(helper);
        ItemStack box = box(new ItemStack(Items.DIRT, 8), new ItemStack(Items.STONE, 12));
        player.getInventory().setItem(BOX_SLOT, box);

        TransferSpec spec = new TransferSpec(
                new CarriedStorageEndpoint(
                        new StorageSelector(IndexSelector.exact(BOX_SLOT), StackMatcher.any()),
                        SlotSelector.any()),
                new PlayerInventoryEndpoint(SlotSelector.empty(OUTPUT_SLOT)),
                StackMatcher.item(new ItemStack(Items.STONE)),
                new TransferLimit(5, 1, 1));
        TransferResult result = QuickStorageTransfer.execute(player, spec);

        helper.assertValueEqual(result.status(), TransferStatus.SUCCESS,
                "Matching extraction should succeed");
        helper.assertValueEqual(result.movedCount(), 5,
                "Extraction should obey the amount limit");
        helper.assertValueEqual(player.getInventory().getItem(OUTPUT_SLOT).getCount(), 5,
                "The selected empty inventory slot should receive the items");
        helper.assertValueEqual(count(box, Items.STONE), 7,
                "Only the requested item should leave storage");
        helper.assertValueEqual(count(box, Items.DIRT), 8,
                "Non-matching storage contents must remain untouched");
        helper.succeed();
    }

    @GameTest
    public void duplicateRequestIdMutatesStorageOnlyOnce(GameTestHelper helper) {
        ServerPlayer player = player(helper);
        ItemStack box = box(new ItemStack(Items.STONE, 16));
        player.getInventory().setItem(BOX_SLOT, box);
        TransferSpec spec = extractSpec(BOX_SLOT, OUTPUT_SLOT, 6);
        UUID requestId = UUID.randomUUID();

        TransferResult first = DirectTransferTransactions.executeOnce(player, requestId, spec);
        TransferResult duplicate = DirectTransferTransactions.executeOnce(player, requestId, spec);

        helper.assertValueEqual(first.movedCount(), 6, "First request should move six items");
        helper.assertValueEqual(duplicate.movedCount(), 6,
                "Duplicate response should retain the original transaction result");
        helper.assertValueEqual(player.getInventory().getItem(OUTPUT_SLOT).getCount(), 6,
                "Retrying one request ID must not apply the mutation twice");
        helper.assertValueEqual(count(box, Items.STONE), 10,
                "Storage must be debited exactly once");
        DirectTransferTransactions.clear(player);
        helper.succeed();
    }

    @GameTest
    public void noMatchAndNoSpaceDoNotMutateInventory(GameTestHelper helper) {
        ServerPlayer player = player(helper);
        ItemStack box = box(new ItemStack(Items.DIRT, 8));
        player.getInventory().setItem(BOX_SLOT, box);

        TransferResult noMatch = QuickStorageTransfer.execute(player,
                extractSpec(BOX_SLOT, OUTPUT_SLOT, 4));
        helper.assertValueEqual(noMatch.status(), TransferStatus.NO_MATCH,
                "Absent material should report no match");

        player.getInventory().setItem(0, new ItemStack(Items.STONE, 4));
        player.getInventory().setItem(OUTPUT_SLOT, new ItemStack(Items.DIRT, 64));
        TransferResult noSpace = QuickStorageTransfer.execute(player, insertSpec(0, BOX_SLOT,
                SlotSelector.exact(0)));
        helper.assertValueEqual(noSpace.status(), TransferStatus.NO_SPACE,
                "A full incompatible destination should report no space");
        helper.assertValueEqual(player.getInventory().getItem(0).getCount(), 4,
                "Failed insertion must not consume source items");
        helper.assertValueEqual(count(box, Items.DIRT), 8,
                "Failed operations must not mutate storage");
        helper.succeed();
    }

    @GameTest
    public void storageRejectsNestedShulkerBoxes(GameTestHelper helper) {
        ServerPlayer player = player(helper);
        ItemStack host = box();
        player.getInventory().setItem(0, box(new ItemStack(Items.EMERALD, 3)));
        player.getInventory().setItem(BOX_SLOT, host);

        TransferResult result = QuickStorageTransfer.execute(player, insertSpec(0, BOX_SLOT));

        helper.assertValueEqual(result.status(), TransferStatus.NO_SPACE,
                "The legacy shulker rule should reject nested shulker boxes");
        helper.assertTrue(!player.getInventory().getItem(0).isEmpty(),
                "Rejected nested storage must remain intact");
        helper.assertValueEqual(count(host, Items.SHULKER_BOX), 0,
                "The destination storage must remain unchanged");
        helper.succeed();
    }

    @GameTest
    public void legacyRegistrationFeedsTheNewStorageRegistry(GameTestHelper helper) {
        ServerPlayer player = player(helper);
        ItemStack box = box();
        QuickShulkerData legacy = QuickOpenableRegistry.getQuickie(box.getItem());

        helper.assertTrue(legacy != null && legacy.supportsBundleing,
                "The original public registry must remain available");
        helper.assertTrue(QuickStorageRegistry.supports(player, box),
                "The same legacy registration must expose the new storage capability");
        helper.succeed();
    }

    private static ServerPlayer player(GameTestHelper helper) {
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        player.getInventory().clearContent();
        return player;
    }

    private static TransferSpec insertSpec(int sourceSlot, int boxSlot) {
        return insertSpec(sourceSlot, boxSlot, SlotSelector.any());
    }

    private static TransferSpec insertSpec(int sourceSlot, int boxSlot,
                                           SlotSelector storageSlots) {
        return new TransferSpec(
                new PlayerInventoryEndpoint(SlotSelector.nonEmpty(sourceSlot)),
                new CarriedStorageEndpoint(
                        new StorageSelector(IndexSelector.exact(boxSlot), StackMatcher.any()),
                        storageSlots),
                StackMatcher.any(), TransferLimit.all());
    }

    private static TransferSpec extractSpec(int boxSlot, int outputSlot, int amount) {
        return new TransferSpec(
                new CarriedStorageEndpoint(
                        new StorageSelector(IndexSelector.exact(boxSlot), StackMatcher.any()),
                        SlotSelector.any()),
                new PlayerInventoryEndpoint(SlotSelector.empty(outputSlot)),
                StackMatcher.item(new ItemStack(Items.STONE)),
                new TransferLimit(amount, 1, 1));
    }

    private static ItemStack box(ItemStack... stacks) {
        ItemStack box = new ItemStack(Items.SHULKER_BOX);
        box.set(DataComponents.CONTAINER,
                ItemContainerContents.fromItems(List.of(stacks)));
        return box;
    }

    private static List<ItemStack> contents(ItemStack box) {
        ItemContainerContents contents = box.get(DataComponents.CONTAINER);
        if (contents == null) return List.of();
        List<ItemStack> result = new ArrayList<>();
        contents.nonEmptyItemCopyStream().forEach(result::add);
        return result;
    }

    private static int count(ItemStack box, net.minecraft.world.item.Item item) {
        return contents(box).stream()
                .filter(stack -> stack.is(item))
                .mapToInt(ItemStack::getCount)
                .sum();
    }

    private static boolean sameContents(ItemStack first, ItemStack second) {
        List<ItemStack> left = contents(first);
        List<ItemStack> right = contents(second);
        if (left.size() != right.size()) return false;
        for (int i = 0; i < left.size(); i++) {
            if (!ItemStack.matches(left.get(i), right.get(i))) return false;
        }
        return true;
    }
}
