package net.kyrptonaught.quickshulker.api.storage;

import net.kyrptonaught.quickshulker.api.QuickShulkerData;
import net.kyrptonaught.quickshulker.internal.StorageRegistryBackend;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

/**
 * Registry for screen-independent item storage.
 *
 * <p>The legacy quick-open registry and this registry share one backing entry.
 * Registrations made through either API therefore resolve through the same
 * storage provider.</p>
 */
public final class QuickStorageRegistry {
    private QuickStorageRegistry() {
    }

    public static void register(Class<? extends ItemLike> item, QuickStorageProvider provider) {
        if (item == null || provider == null) throw new IllegalArgumentException("item/provider");
        StorageRegistryBackend.registerStorage(item, provider);
    }

    public static QuickStorageProvider get(ItemLike item) {
        return StorageRegistryBackend.storageProvider(item);
    }

    public static boolean supports(Player player, ItemStack hostStack) {
        if (hostStack == null || hostStack.isEmpty()) return false;
        QuickStorageProvider provider = get(hostStack.getItem());
        return provider != null && provider.canAccess(player, hostStack);
    }

    /** Adapts an unchanged legacy registration to the storage capability. */
    public static QuickStorageProvider legacyProvider(QuickShulkerData data) {
        return new QuickStorageProvider() {
            @Override
            public boolean canAccess(Player player, ItemStack hostStack) {
                return data.supportsBundleing
                        && (data.ignoreSingleStackCheck || hostStack.getCount() <= 1);
            }

            @Override
            public StorageAccess open(Player player, ItemStack hostStack) {
                if (!canAccess(player, hostStack)) return null;
                Container container = data.getInventory(player, hostStack);
                if (container == null) return null;
                return new StorageAccess() {
                    @Override
                    public Container container() {
                        return container;
                    }

                    @Override
                    public boolean canInsert(int slot, ItemStack stack) {
                        return container.canPlaceItem(slot, stack)
                                && data.canBundleInsertItem(player, container, hostStack, stack);
                    }

                    @Override
                    public void commit() {
                        container.stopOpen(player);
                    }
                };
            }
        };
    }
}
