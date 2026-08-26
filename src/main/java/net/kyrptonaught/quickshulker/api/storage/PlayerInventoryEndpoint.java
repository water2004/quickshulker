package net.kyrptonaught.quickshulker.api.storage;

/** Selects slots from the player's main 36-slot inventory. */
public record PlayerInventoryEndpoint(SlotSelector slots) implements TransferEndpoint {
    public PlayerInventoryEndpoint {
        if (slots == null) throw new IllegalArgumentException("slots");
    }
}
