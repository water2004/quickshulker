package net.kyrptonaught.quickshulker.util;

import net.kyrptonaught.quickshulker.api.Util;
import net.kyrptonaught.quickshulker.internal.legacy.LegacyStorageTransfers;
import net.kyrptonaught.shulkerutils.ShulkerUtils;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Legacy interaction facade retained for existing mixins and integrations. */
public class BundleHelper {
    public static boolean shouldAttemptBundle(Player player, ClickAction clickType, ItemStack hostStack, ItemStack insertStack, boolean enabledInConfig) {
        return enabledInConfig && clickType == ClickAction.SECONDARY
                && Util.isOpenableItem(hostStack)
                && isAcceptedInsertItem(insertStack)
                && Util.getQuickItemInventory(player, hostStack) != null;
    }

    public static boolean shouldAttemptUnBundle(Player player, ClickAction clickType, ItemStack hostStack, ItemStack insertStack, boolean enabledInConfig) {
        Container storage = Util.getQuickItemInventory(player, hostStack);
        return storage != null
                && enabledInConfig && clickType == ClickAction.SECONDARY
                && hostStack.getCount() == 1
                && !storage.isEmpty()
                && insertStack.isEmpty();
    }

    public static boolean shouldAttemptTransfer(Player player, ClickAction clickType, ItemStack hostStack, ItemStack insertStack, boolean enabledInConfig){
        return enabledInConfig && clickType == ClickAction.SECONDARY
                && ShulkerUtils.isShulkerItem(hostStack) && hostStack.getCount() == 1
                && Util.getQuickItemInventory(player, hostStack) != null
                && isAcceptedTransferItem(player, insertStack);
    }

    private static boolean isAcceptedInsertItem(ItemStack insertStack) {
        return !insertStack.isEmpty() && !ShulkerUtils.isShulkerItem(insertStack);
    }

    private static boolean isAcceptedTransferItem(Player player, ItemStack insertStack) {
        return ShulkerUtils.isShulkerItem(insertStack) && insertStack.getCount() == 1
                && Util.getQuickItemInventory(player, insertStack) != null;
    }

    public static void bundleItemIntoStack(Player player, ItemStack hostStack, ItemStack insertStack, CallbackInfoReturnable<Boolean> cir) {
        if (bundleItem(player, hostStack, insertStack) != null && cir != null)
            cir.setReturnValue(true);
    }

    public static void bundleItemIntoStack(Player player, ItemStack hostStack, ItemStack insertStack, Slot slot, CallbackInfoReturnable<Boolean> cir){
        if(bundleItem(player, hostStack, slot) != null && cir != null){
            cir.setReturnValue(true);
        }
    }

    public static void unbundleStackIntoSlot(Player player, ItemStack hostStack, Slot unbundleSlot, CallbackInfoReturnable<Boolean> cir) {
        if (unbundleItem(player, hostStack, unbundleSlot) != null) {
            cir.setReturnValue(true);
        }
    }

    public static void transferItemsToShulker(Player player, ItemStack hostStack, ItemStack insertStack, CallbackInfoReturnable<Boolean> cir){
        if (LegacyStorageTransfers.moveAll(player, insertStack, hostStack)
                && cir != null) {
            cir.setReturnValue(true);
        }
    }

    public static ItemStack unbundleItem(Player player, ItemStack hostStack, Slot unbundleSlot) {
        return LegacyStorageTransfers.extractToSlot(
                player, hostStack, unbundleSlot);
    }

    private static ItemStack bundleItem(Player player, ItemStack hostStack, ItemStack insertStack) {
        return LegacyStorageTransfers.insert(player, hostStack, insertStack);
    }

    private static ItemStack bundleItem(Player player, ItemStack hostStack, Slot slot) {
        return LegacyStorageTransfers.insertFromSlot(player, hostStack, slot);
    }
}
