package net.kyrptonaught.quickshulker.api.shulker;

/** One player inventory slot. Its live bounds are checked when executed. */
public record PlayerSlotEndpoint(int slot)
        implements ShulkerTransferEndpoint {
    public PlayerSlotEndpoint {
        if (slot < 0) throw new IllegalArgumentException("slot");
    }
}
