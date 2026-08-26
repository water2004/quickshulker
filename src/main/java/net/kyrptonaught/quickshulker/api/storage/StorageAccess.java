package net.kyrptonaught.quickshulker.api.storage;

import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;

/** A mutable storage view whose changes can be committed to its host item. */
public interface StorageAccess extends AutoCloseable {
    Container container();

    default boolean canExtract(int slot, ItemStack stack) {
        return true;
    }

    default boolean canInsert(int slot, ItemStack stack) {
        return container().canPlaceItem(slot, stack);
    }

    void commit();

    @Override
    default void close() {
    }
}
