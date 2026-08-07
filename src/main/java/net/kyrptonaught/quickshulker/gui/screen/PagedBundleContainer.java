package net.kyrptonaught.quickshulker.gui.screen;

import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/** A fixed-size window over the bundle's logical inventory. */
final class PagedBundleContainer implements Container {
    private final BundleContainer backing;
    private final int pageSize;
    private int page;

    PagedBundleContainer(BundleContainer backing, int pageSize) {
        this.backing = backing;
        this.pageSize = pageSize;
    }

    void setPage(int page) {
        this.page = page;
    }

    private int logicalSlot(int slot) {
        return page * pageSize + slot;
    }

    private boolean isValidSlot(int slot) {
        int logicalSlot = logicalSlot(slot);
        return slot >= 0 && slot < pageSize && logicalSlot < backing.getContainerSize();
    }

    @Override
    public int getContainerSize() {
        return pageSize;
    }

    @Override
    public boolean isEmpty() {
        for (int slot = 0; slot < pageSize; slot++) {
            if (!getItem(slot).isEmpty()) return false;
        }
        return true;
    }

    @Override
    public ItemStack getItem(int slot) {
        return isValidSlot(slot) ? backing.getItem(logicalSlot(slot)) : ItemStack.EMPTY;
    }

    @Override
    public ItemStack removeItem(int slot, int amount) {
        return isValidSlot(slot) ? backing.removeItem(logicalSlot(slot), amount) : ItemStack.EMPTY;
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        return isValidSlot(slot) ? backing.removeItemNoUpdate(logicalSlot(slot)) : ItemStack.EMPTY;
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        if (isValidSlot(slot)) backing.setItem(logicalSlot(slot), stack);
    }

    @Override
    public int getMaxStackSize(ItemStack stack) {
        return backing.getMaxStackSize(stack);
    }

    @Override
    public void setChanged() {
        backing.setChanged();
    }

    @Override
    public boolean stillValid(Player player) {
        return backing.stillValid(player);
    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        return isValidSlot(slot) && backing.canPlaceItem(logicalSlot(slot), stack);
    }

    @Override
    public void clearContent() {
        for (int slot = 0; slot < pageSize; slot++) {
            if (isValidSlot(slot)) backing.setItem(logicalSlot(slot), ItemStack.EMPTY);
        }
    }
}
