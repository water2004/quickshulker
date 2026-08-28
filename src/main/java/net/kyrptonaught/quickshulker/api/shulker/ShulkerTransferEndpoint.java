package net.kyrptonaught.quickshulker.api.shulker;

/** A bounded endpoint accepted by the screen-independent shulker protocol. */
public sealed interface ShulkerTransferEndpoint permits PlayerSlotEndpoint,
        CarriedShulkerSlotEndpoint {
}
