package net.kyrptonaught.quickshulker.api.storage;

import net.minecraft.world.item.ItemStack;

/**
 * A trusted, server-local mutable stack endpoint. It is deliberately not
 * accepted by the network codec.
 */
public record MutableStackEndpoint(ItemStack stack) implements TransferEndpoint {
    public MutableStackEndpoint {
        if (stack == null) throw new IllegalArgumentException("stack");
    }
}
