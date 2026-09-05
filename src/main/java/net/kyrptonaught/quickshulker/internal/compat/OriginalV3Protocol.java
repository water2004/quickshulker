package net.kyrptonaught.quickshulker.internal.compat;

import net.kyrptonaught.quickshulker.network.OpenShulkerPacket;
import net.kyrptonaught.quickshulker.network.QuickBundlePacket;

/** Registers the frozen wire surface used by original Quick Shulker v3. */
public final class OriginalV3Protocol {
    private OriginalV3Protocol() {
    }

    public static void register() {
        OpenShulkerPacket.registerReceivePacket();
        QuickBundlePacket.registerReceivePacket();
    }
}
