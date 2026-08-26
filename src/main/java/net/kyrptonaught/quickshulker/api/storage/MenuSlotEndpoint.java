package net.kyrptonaught.quickshulker.api.storage;

import net.minecraft.world.inventory.Slot;

/** A trusted, server-local menu slot endpoint used by legacy quick actions. */
public record MenuSlotEndpoint(Slot slot) implements TransferEndpoint {
    public MenuSlotEndpoint {
        if (slot == null) throw new IllegalArgumentException("slot");
    }
}
