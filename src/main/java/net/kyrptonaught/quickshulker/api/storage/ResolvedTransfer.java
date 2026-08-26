package net.kyrptonaught.quickshulker.api.storage;

import net.minecraft.world.item.ItemStack;

public record ResolvedTransfer(ResolvedEndpoint source,
                               ResolvedEndpoint destination,
                               ItemStack movedStack) {
    public ResolvedTransfer {
        if (source == null || destination == null) throw new IllegalArgumentException("endpoints");
        movedStack = movedStack == null ? ItemStack.EMPTY : movedStack.copy();
    }
}
