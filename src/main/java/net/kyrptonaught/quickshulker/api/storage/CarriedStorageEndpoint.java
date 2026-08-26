package net.kyrptonaught.quickshulker.api.storage;

/** Selects slots inside registered storage items carried in the player's main inventory. */
public record CarriedStorageEndpoint(StorageSelector storage, SlotSelector slots)
        implements TransferEndpoint {
    public CarriedStorageEndpoint {
        if (storage == null) throw new IllegalArgumentException("storage");
        if (slots == null) throw new IllegalArgumentException("slots");
    }
}
