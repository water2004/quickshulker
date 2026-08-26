package net.kyrptonaught.quickshulker.util;

import net.kyrptonaught.quickshulker.api.storage.MenuSlotEndpoint;
import net.kyrptonaught.quickshulker.api.storage.MutableStackEndpoint;
import net.kyrptonaught.quickshulker.api.storage.QuickStorageRegistry;
import net.kyrptonaught.quickshulker.api.storage.QuickStorageTransfer;
import net.kyrptonaught.quickshulker.api.storage.SlotSelector;
import net.kyrptonaught.quickshulker.api.storage.StackMatcher;
import net.kyrptonaught.quickshulker.api.storage.StorageItemEndpoint;
import net.kyrptonaught.quickshulker.api.storage.TransferLimit;
import net.kyrptonaught.quickshulker.api.storage.TransferResult;
import net.kyrptonaught.quickshulker.api.storage.TransferSpec;
import net.kyrptonaught.quickshulker.api.Util;
import net.kyrptonaught.shulkerutils.ShulkerUtils;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

public class BundleHelper {
    public static boolean shouldAttemptBundle(Player player, ClickAction clickType, ItemStack hostStack, ItemStack insertStack, boolean enabledInConfig) {
        return enabledInConfig && clickType == ClickAction.SECONDARY
                && QuickStorageRegistry.supports(player, hostStack)
                && isAcceptedInsertItem(insertStack);
    }

    public static boolean shouldAttemptUnBundle(Player player, ClickAction clickType, ItemStack hostStack, ItemStack insertStack, boolean enabledInConfig) {
        return enabledInConfig && clickType == ClickAction.SECONDARY
                && hostStack.getCount() == 1
                && QuickStorageRegistry.supports(player, hostStack)
                && !Util.getQuickItemInventory(player, hostStack).isEmpty()
                && insertStack.isEmpty();
    }

    public static boolean shouldAttemptTransfer(Player player, ClickAction clickType, ItemStack hostStack, ItemStack insertStack, boolean enabledInConfig){
        return enabledInConfig && clickType == ClickAction.SECONDARY
                && ShulkerUtils.isShulkerItem(hostStack) && hostStack.getCount() == 1
                && QuickStorageRegistry.supports(player, hostStack)
                && isAcceptedTransferItem(player, insertStack);
    }

    private static boolean isAcceptedInsertItem(ItemStack insertStack) {
        return !insertStack.isEmpty() && !ShulkerUtils.isShulkerItem(insertStack);
    }

    private static boolean isAcceptedTransferItem(Player player, ItemStack insertStack) {
        return ShulkerUtils.isShulkerItem(insertStack) && insertStack.getCount() == 1
                && QuickStorageRegistry.supports(player, insertStack);
    }

    public static void bundleItemIntoStack(Player player, ItemStack hostStack, ItemStack insertStack, CallbackInfoReturnable<Boolean> cir) {
        if (bundleItem(player, hostStack, insertStack) != null && cir != null)
            cir.setReturnValue(true);
    }

    public static void bundleItemIntoStack(Player player, ItemStack hostStack, ItemStack insertStack, Slot slot, CallbackInfoReturnable<Boolean> cir){
        if(bundleItem(player, hostStack, insertStack, slot) != null && cir != null){
            cir.setReturnValue(true);
        }
    }

    public static void unbundleStackIntoSlot(Player player, ItemStack hostStack, Slot unbundleSlot, CallbackInfoReturnable<Boolean> cir) {
        if (unbundleItem(player, hostStack, unbundleSlot) != null) {
            cir.setReturnValue(true);
        }
    }

    public static void transferItemsToShulker(Player player, ItemStack hostStack, ItemStack insertStack, CallbackInfoReturnable<Boolean> cir){
        TransferResult result = QuickStorageTransfer.execute(player, new TransferSpec(
                new StorageItemEndpoint(insertStack, SlotSelector.anyReverse()),
                new StorageItemEndpoint(hostStack, SlotSelector.any()),
                StackMatcher.any(), TransferLimit.all()));
        if (result.movedAnything() && cir != null) cir.setReturnValue(true);
    }

    public static ItemStack unbundleItem(Player player, ItemStack hostStack, Slot unbundleSlot) {
        TransferResult result = QuickStorageTransfer.execute(player, new TransferSpec(
                new StorageItemEndpoint(hostStack, SlotSelector.anyReverse()),
                new MenuSlotEndpoint(unbundleSlot),
                StackMatcher.any(), TransferLimit.oneSourceStack()));
        return result.movedAnything() && !result.transfers().isEmpty()
                ? result.transfers().getFirst().movedStack()
                : null;
    }

//    private static ItemStack bundleItem(PlayerEntity player, ItemStack hostStack, ItemStack insertStack) {
//        Inventory bundlingInv = Util.getQuickItemInventory(player, hostStack);
//        QuickShulkerData qsdata = QuickOpenableRegistry.getQuickie(hostStack.getItem());
//        if (bundlingInv != null && qsdata.canBundleInsertItem(player, bundlingInv, hostStack, insertStack)) {
//            try (Transaction transaction = Transaction.openOuter()) {
//                long amount = InventoryStorage.of(bundlingInv, null).insert(ItemVariant.of(insertStack), insertStack.getCount(), transaction);
//                if (amount == 0) return null;
//                transaction.commit();
//                insertStack.decrement((int) amount);
//                bundlingInv.onClose(player);
//                return insertStack;
//            }
//        }
//        return null;
//    }

    private static ItemStack bundleItem(Player player, ItemStack hostStack, ItemStack insertStack) {
        TransferResult result = QuickStorageTransfer.execute(player, new TransferSpec(
                new MutableStackEndpoint(insertStack),
                new StorageItemEndpoint(hostStack, SlotSelector.any()),
                StackMatcher.any(), TransferLimit.all()));
        return result.movedAnything() ? insertStack : null;
    }

    private static ItemStack bundleItem(Player player, ItemStack hostStack, ItemStack insertStack, Slot slot) {
        TransferResult result = QuickStorageTransfer.execute(player, new TransferSpec(
                new MenuSlotEndpoint(slot),
                new StorageItemEndpoint(hostStack, SlotSelector.any()),
                StackMatcher.any(), TransferLimit.all()));
        return result.movedAnything() ? slot.getItem() : null;
    }
}
