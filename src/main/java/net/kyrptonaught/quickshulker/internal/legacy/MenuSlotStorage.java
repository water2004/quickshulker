package net.kyrptonaught.quickshulker.internal.legacy;

import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.item.base.SingleStackStorage;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

/** Transactional Fabric storage view of a menu slot. */
public final class MenuSlotStorage extends SingleStackStorage {
    private final Player player;
    private final Slot slot;

    public MenuSlotStorage(Player player, Slot slot) {
        this.player = player;
        this.slot = slot;
    }

    @Override
    protected ItemStack getStack() {
        // SingleStackStorage mutates the returned stack before setStack(). A
        // copy keeps Slot.safeTake/safeInsert in charge of the real menu slot.
        return slot.getItem().copy();
    }

    @Override
    protected void setStack(ItemStack replacement) {
        ItemStack current = slot.getItem();
        if (!current.isEmpty()
                && ItemStack.isSameItemSameComponents(current, replacement)
                && replacement.getCount() < current.getCount()) {
            int removed = current.getCount() - replacement.getCount();
            ItemStack extracted = slot.safeTake(removed, removed, player);
            if (extracted.getCount() != removed
                    || !ItemStack.isSameItemSameComponents(current, extracted)) {
                throw new IllegalStateException(
                        "menu slot yielded fewer items than planned");
            }
            return;
        }
        if ((current.isEmpty()
                || ItemStack.isSameItemSameComponents(current, replacement))
                && replacement.getCount() > current.getCount()) {
            int inserted = replacement.getCount() - current.getCount();
            ItemStack addition = replacement.copyWithCount(inserted);
            ItemStack remainder = slot.safeInsert(addition, inserted);
            if (!remainder.isEmpty()) {
                throw new IllegalStateException(
                        "menu slot accepted fewer items than planned");
            }
            return;
        }
        slot.set(replacement);
    }

    @Override
    protected boolean canInsert(ItemVariant variant) {
        return slot.mayPlace(variant.toStack());
    }

    @Override
    protected boolean canExtract(ItemVariant variant) {
        return slot.mayPickup(player);
    }

    @Override
    protected int getCapacity(ItemVariant variant) {
        return slot.getMaxStackSize(variant.toStack());
    }

    @Override
    protected void onFinalCommit() {
        slot.setChanged();
    }
}
