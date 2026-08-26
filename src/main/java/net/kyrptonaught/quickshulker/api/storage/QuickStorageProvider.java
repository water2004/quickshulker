package net.kyrptonaught.quickshulker.api.storage;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/**
 * Provides screen-independent access to storage carried by an item stack.
 *
 * <p>Providers describe storage only. Opening a menu remains a separate
 * Quick Shulker capability.</p>
 */
public interface QuickStorageProvider {
    boolean canAccess(Player player, ItemStack hostStack);

    StorageAccess open(Player player, ItemStack hostStack);
}
