package net.kyrptonaught.quickshulker.internal;

import net.kyrptonaught.quickshulker.api.QuickShulkerData;
import net.kyrptonaught.quickshulker.api.storage.QuickStorageProvider;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.ItemLike;

import java.util.HashMap;
import java.util.Map;

/** Shared backing store for the legacy open API and the storage API. */
public final class StorageRegistryBackend {
    private static final Map<Class<? extends ItemLike>, Entry> ENTRIES = new HashMap<>();

    private StorageRegistryBackend() {
    }

    public static void registerLegacy(Class<? extends ItemLike> type,
                                      QuickShulkerData data,
                                      QuickStorageProvider provider) {
        Entry entry = ENTRIES.computeIfAbsent(type, ignored -> new Entry());
        entry.legacyData = data;
        entry.storageProvider = provider;
    }

    public static void registerStorage(Class<? extends ItemLike> type,
                                       QuickStorageProvider provider) {
        ENTRIES.computeIfAbsent(type, ignored -> new Entry()).storageProvider = provider;
    }

    public static QuickShulkerData legacyData(ItemLike item) {
        Entry entry = entry(item);
        return entry == null ? null : entry.legacyData;
    }

    public static QuickStorageProvider storageProvider(ItemLike item) {
        Entry entry = entry(item);
        return entry == null ? null : entry.storageProvider;
    }

    private static Entry entry(ItemLike item) {
        if (item == null) return null;
        if (item instanceof BlockItem blockItem) {
            Entry blockEntry = ENTRIES.get(blockItem.getBlock().getClass());
            if (blockEntry != null) return blockEntry;
        }
        return ENTRIES.get(item.getClass());
    }

    private static final class Entry {
        private QuickShulkerData legacyData;
        private QuickStorageProvider storageProvider;
    }
}
