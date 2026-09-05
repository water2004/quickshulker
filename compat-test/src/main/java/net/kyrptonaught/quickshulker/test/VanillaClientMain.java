package net.kyrptonaught.quickshulker.test;

import net.minecraft.client.Minecraft;

/** Uses an unmodified Mojang client, not a Fabric client pretending to be vanilla. */
public final class VanillaClientMain {
    public static void main(String[] args) {
        Thread driver = new Thread(() -> {
            try {
                while (!Thread.currentThread().isInterrupted()) {
                    Minecraft client = Minecraft.getInstance();
                    if (client != null) client.execute(() -> MatrixClientInitializer.tick(client));
                    Thread.sleep(50);
                }
            } catch (InterruptedException stopped) {
                Thread.currentThread().interrupt();
            }
        }, "vanilla-compat-test-driver");
        driver.setDaemon(true);
        driver.start();
        net.minecraft.client.main.Main.main(args);
    }
}
