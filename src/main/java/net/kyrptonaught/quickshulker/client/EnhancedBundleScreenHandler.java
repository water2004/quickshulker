package net.kyrptonaught.quickshulker.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.kyrptonaught.quickshulker.gui.screen.BundleItemMenu;
import net.kyrptonaught.quickshulker.gui.screen.BundleItemScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.network.protocol.game.ClientboundOpenScreenPacket;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuType;

import java.util.concurrent.TimeUnit;

/** Replaces one expected vanilla open-screen packet with QuickShulker's bundle UI. */
@Environment(EnvType.CLIENT)
public final class EnhancedBundleScreenHandler {
    private static final long EXPECTATION_TIMEOUT = TimeUnit.SECONDS.toNanos(3);
    private static long expectedUntil;

    private EnhancedBundleScreenHandler() {
    }

    public static void expectOpen() {
        expectedUntil = System.nanoTime() + EXPECTATION_TIMEOUT;
    }

    public static boolean openIfExpected(Minecraft client, ClientboundOpenScreenPacket packet) {
        boolean expected = expectedUntil != 0
                && System.nanoTime() <= expectedUntil
                && packet.getType() == MenuType.GENERIC_9x6;
        expectedUntil = 0;
        if (!expected || client.player == null) return false;

        Inventory inventory = client.player.getInventory();
        BundleItemMenu menu = new BundleItemMenu(packet.getContainerId(), inventory);
        client.player.containerMenu = menu;
        client.gui.setScreen(new BundleItemScreen(menu, inventory, packet.getTitle()));
        return true;
    }
}
