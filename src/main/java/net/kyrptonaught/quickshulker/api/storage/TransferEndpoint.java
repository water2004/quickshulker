package net.kyrptonaught.quickshulker.api.storage;

/** Marker for a source or destination participating in a storage transfer. */
public sealed interface TransferEndpoint permits PlayerInventoryEndpoint,
        CarriedStorageEndpoint, MutableStackEndpoint, StorageItemEndpoint,
        MenuSlotEndpoint {
}
