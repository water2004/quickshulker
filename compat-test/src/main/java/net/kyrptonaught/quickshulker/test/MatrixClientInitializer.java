package net.kyrptonaught.quickshulker.test;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ConnectScreen;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.multiplayer.resolver.ServerAddress;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ItemContainerContents;

import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public final class MatrixClientInitializer implements ClientModInitializer {
    private static final String API_CLASS =
            "net.kyrptonaught.quickshulker.client.api.QuickStorageClient";
    private static final String LEGACY_PACKET_CLASS =
            "net.kyrptonaught.quickshulker.network.OpenShulkerPacket";

    private static Stage stage = Stage.WAIT_TITLE;
    private static int stageTicks;
    private static int authoritativeTicks;

    @Override
    public void onInitializeClient() {
        ClientTickEvents.END_CLIENT_TICK.register(MatrixClientInitializer::tick);
    }

    private static void tick(Minecraft client) {
        if (stage == Stage.DONE) return;
        try {
            if (++stageTicks > 400) {
                throw new AssertionError("Timed out in stage " + stage);
            }
            switch (stage) {
                case WAIT_TITLE -> connect(client);
                case WAIT_JOIN -> waitForInventory(client);
                case WAIT_MENU -> takeStone(client);
                case WAIT_AUTHORITATIVE_ITEM -> verifyAuthoritativeItem(client);
                case WAIT_CLOSE -> finishWhenClosed(client);
                case DONE -> { }
            }
        } catch (Throwable error) {
            fail(client, error);
        }
    }

    private static void connect(Minecraft client) {
        if (stageTicks < 10 || client.getConnection() != null || client.gui.screen() == null) return;
        String address = System.getProperty(
                "quickshulker.matrix.address", "127.0.0.1:25571");
        ServerData data = new ServerData(
                "Quick Shulker matrix", address, ServerData.Type.OTHER);
        ConnectScreen.startConnecting(client.gui.screen(), client,
                ServerAddress.parseString(address), data, false, null);
        advance(Stage.WAIT_JOIN);
    }

    private static void waitForInventory(Minecraft client) throws Exception {
        if (client.player == null) return;
        ItemStack box = client.player.getInventory().getItem(9);
        if (!box.is(Items.SHULKER_BOX) || countStoredStone(box) != 4) return;

        String expectedClient = System.getProperty(
                "quickshulker.matrix.expectedClient", "new");
        boolean expectApi = expectedClient.equals("new");
        boolean apiPresent = isClassPresent(API_CLASS);
        if (apiPresent != expectApi) {
            throw new AssertionError("Client API presence was " + apiPresent
                    + ", expected client=" + expectedClient);
        }

        boolean directAvailable = false;
        if (apiPresent) {
            Class<?> api = Class.forName(API_CLASS);
            directAvailable = Boolean.TRUE.equals(api.getMethod("isAvailable").invoke(null));
        }
        boolean expectedDirect = Boolean.parseBoolean(System.getProperty(
                "quickshulker.matrix.expectedDirect", "false"));
        if (directAvailable != expectedDirect) {
            throw new AssertionError("Direct capability was " + directAvailable
                    + ", expected " + expectedDirect);
        }

        Class<?> packet = Class.forName(LEGACY_PACKET_CLASS);
        Method send = packet.getMethod("sendOpenPacket", int.class);
        send.invoke(null, 9);
        advance(Stage.WAIT_MENU);
    }

    private static void takeStone(Minecraft client) {
        if (client.player == null || client.gameMode == null
                || client.player.containerMenu == client.player.inventoryMenu) return;
        var menu = client.player.containerMenu;
        if (menu.slots.isEmpty() || !menu.slots.getFirst().getItem().is(Items.STONE)
                || menu.slots.getFirst().getItem().getCount() != 4) {
            throw new AssertionError("Legacy screen did not expose four stone items");
        }
        client.gameMode.handleContainerInput(
                menu.containerId, 0, 0, ContainerInput.QUICK_MOVE, client.player);
        advance(Stage.WAIT_AUTHORITATIVE_ITEM);
    }

    private static void verifyAuthoritativeItem(Minecraft client) {
        if (client.player == null) return;
        if (countLooseStone(client) != 4) return;
        if (++authoritativeTicks < 3) return;
        client.player.closeContainer();
        advance(Stage.WAIT_CLOSE);
    }

    private static void finishWhenClosed(Minecraft client) throws Exception {
        if (client.player == null
                || client.player.containerMenu != client.player.inventoryMenu
                || countLooseStone(client) != 4) return;
        writeResult("PASS client="
                + System.getProperty("quickshulker.matrix.expectedClient")
                + " direct="
                + System.getProperty("quickshulker.matrix.expectedDirect"));
        stage = Stage.DONE;
        client.stop();
    }

    private static int countLooseStone(Minecraft client) {
        return client.player.getInventory().getNonEquipmentItems().stream()
                .filter(stack -> stack.is(Items.STONE))
                .mapToInt(ItemStack::getCount)
                .sum();
    }

    private static int countStoredStone(ItemStack box) {
        ItemContainerContents contents = box.get(DataComponents.CONTAINER);
        if (contents == null) return 0;
        return contents.nonEmptyItemCopyStream()
                .filter(stack -> stack.is(Items.STONE))
                .mapToInt(ItemStack::getCount)
                .sum();
    }

    private static boolean isClassPresent(String className) {
        try {
            Class.forName(className, false, MatrixClientInitializer.class.getClassLoader());
            return true;
        } catch (ClassNotFoundException error) {
            return false;
        }
    }

    private static void advance(Stage next) {
        stage = next;
        stageTicks = 0;
    }

    private static void fail(Minecraft client, Throwable error) {
        try {
            writeResult("FAIL " + error);
        } catch (Exception writeError) {
            error.addSuppressed(writeError);
        }
        error.printStackTrace();
        stage = Stage.DONE;
        client.stop();
    }

    private static void writeResult(String result) throws Exception {
        Path output = Path.of(System.getProperty("quickshulker.matrix.result"));
        Files.createDirectories(output.getParent());
        Files.writeString(output, result + System.lineSeparator(), StandardCharsets.UTF_8);
    }

    private enum Stage {
        WAIT_TITLE,
        WAIT_JOIN,
        WAIT_MENU,
        WAIT_AUTHORITATIVE_ITEM,
        WAIT_CLOSE,
        DONE
    }
}
