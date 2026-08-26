package net.kyrptonaught.quickshulker.api.storage;

import net.minecraft.world.item.ItemStack;

/** The concrete endpoint selected by the transfer engine. */
public record ResolvedEndpoint(Kind kind,
                               int hostInventorySlot,
                               int slot,
                               ItemStack authoritativeHost,
                               ItemStack authoritativeStack) {
    public enum Kind {
        PLAYER_INVENTORY,
        CARRIED_STORAGE,
        MUTABLE_STACK,
        STORAGE_ITEM,
        MENU_SLOT
    }

    public ResolvedEndpoint {
        authoritativeHost = authoritativeHost == null ? ItemStack.EMPTY : authoritativeHost.copy();
        authoritativeStack = authoritativeStack == null ? ItemStack.EMPTY : authoritativeStack.copy();
    }
}
