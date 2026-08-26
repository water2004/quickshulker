package net.kyrptonaught.quickshulker.api.storage;

import net.minecraft.world.item.ItemStack;

/** Storage slots inside a trusted, server-local host stack. */
public record StorageItemEndpoint(ItemStack hostStack, SlotSelector slots)
        implements TransferEndpoint {
    public StorageItemEndpoint {
        if (hostStack == null) throw new IllegalArgumentException("hostStack");
        if (slots == null) throw new IllegalArgumentException("slots");
    }
}
