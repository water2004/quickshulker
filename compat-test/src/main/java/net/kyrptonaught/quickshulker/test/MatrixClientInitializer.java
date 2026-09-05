package net.kyrptonaught.quickshulker.test;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ConnectScreen;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.multiplayer.resolver.ServerAddress;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ItemContainerContents;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public final class MatrixClientInitializer implements ClientModInitializer {
    private static final String API_CLASS =
            "net.kyrptonaught.quickshulker.api.shulker.client.ShulkerTransferClient";
    private static final String LEGACY_PACKET_CLASS =
            "net.kyrptonaught.quickshulker.network.OpenShulkerPacket";

    private static Stage stage = Stage.WAIT_TITLE;
    private static int stageTicks;
    private static int authoritativeTicks;
    private static Object directHandle;
    private static boolean bundlePhase;

    @Override
    public void onInitializeClient() {
        ClientTickEvents.END_CLIENT_TICK.register(MatrixClientInitializer::tick);
    }

    static void tick(Minecraft client) {
        if (stage == Stage.DONE) return;
        try {
            if (stageTicks > 0 && stageTicks % 200 == 0) {
                System.out.println("[compat-test] waiting " + describeState(client));
            }
            if (++stageTicks > 1200) {
                throw new AssertionError("Timed out: " + describeState(client));
            }
            switch (stage) {
                case WAIT_TITLE -> connect(client);
                case WAIT_JOIN -> waitForInventory(client);
                case WAIT_DIRECT -> verifyDirectTransfer(client);
                case WAIT_HELD -> openHeld(client);
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
        if (client.player == null || client.gui.screen() != null) return;
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

        if (directAvailable) {
            submitDirectTransfer();
            advance(Stage.WAIT_DIRECT);
            return;
        }

        if (expectedClient.equals("none")) {
            client.gameMode.handleContainerInput(client.player.inventoryMenu.containerId,
                    9, 0, ContainerInput.SWAP, client.player);
            advance(Stage.WAIT_HELD);
            return;
        }

        openModded(client, 9);
    }

    private static void openHeld(Minecraft client) {
        if (client.player == null || !client.player.getMainHandItem().is(bundlePhase ? Items.BUNDLE : Items.SHULKER_BOX)
                || stageTicks < 5) return;
        client.gameMode.useItem(client.player, InteractionHand.MAIN_HAND);
        advance(Stage.WAIT_MENU);
    }

    private static void submitDirectTransfer() throws Exception {
        Class<?> shulkerEndpoint = Class.forName(
                "net.kyrptonaught.quickshulker.api.shulker.ShulkerTransferEndpoint");
        Class<?> carriedEndpoint = Class.forName(
                "net.kyrptonaught.quickshulker.api.shulker.CarriedShulkerSlotEndpoint");
        Class<?> playerEndpoint = Class.forName(
                "net.kyrptonaught.quickshulker.api.shulker.PlayerSlotEndpoint");
        Class<?> stackFilter = Class.forName(
                "net.kyrptonaught.quickshulker.api.shulker.ShulkerItemFilter");
        Class<?> requestClass = Class.forName(
                "net.kyrptonaught.quickshulker.api.shulker.ShulkerTransferRequest");
        Class<?> clientApi = Class.forName(API_CLASS);

        Object source = carriedEndpoint.getConstructor(int.class, int.class)
                .newInstance(9, 0);
        Object destination = playerEndpoint.getConstructor(int.class)
                .newInstance(0);
        Object filter = stackFilter.getMethod("sameItem", ItemStack.class)
                .invoke(null, new ItemStack(Items.STONE));
        Object request = requestClass.getConstructor(
                        shulkerEndpoint, shulkerEndpoint, stackFilter, int.class)
                .newInstance(source, destination, filter, 4);
        directHandle = clientApi.getMethod("submit", requestClass)
                .invoke(null, request);
    }

    private static void verifyDirectTransfer(Minecraft client) throws Exception {
        if (client.player == null || directHandle == null) return;
        Class<?> handleClass = directHandle.getClass();
        if (!Boolean.TRUE.equals(handleClass.getMethod("isDone").invoke(directHandle))) {
            return;
        }

        Object result = handleClass.getMethod("resultOrNull").invoke(directHandle);
        if (result == null) return;
        Object status = result.getClass().getMethod("status").invoke(result);
        if (!"SUCCESS".equals(status.toString())) {
            throw new AssertionError("Direct transfer completed with " + status);
        }
        if (countLooseStone(client) != 4
                || countStoredStone(client.player.getInventory().getItem(9)) != 0) {
            return;
        }
        if (++authoritativeTicks < 3) return;

        beginBundle(client);
    }

    private static void takeStone(Minecraft client) {
        if (client.player == null || client.gameMode == null
                || client.player.containerMenu == client.player.inventoryMenu) return;
        var menu = client.player.containerMenu;
        if (menu.slots.isEmpty() || !menu.slots.getFirst().getItem().is(bundlePhase ? Items.DIAMOND : Items.STONE)
                || menu.slots.getFirst().getItem().getCount() != 4) {
            throw new AssertionError("Screen did not expose the four expected items (bundle=" + bundlePhase + ")");
        }
        if (bundlePhase) {
            int expectedSlots = System.getProperty("quickshulker.matrix.expectedClient").equals("new") ? 100 : 90;
            if (menu.slots.size() != expectedSlots) {
                throw new AssertionError("Bundle menu slot count=" + menu.slots.size() + ", expected=" + expectedSlots);
            }
        }
        client.gameMode.handleContainerInput(
                menu.containerId, 0, 0, ContainerInput.QUICK_MOVE, client.player);
        advance(Stage.WAIT_AUTHORITATIVE_ITEM);
    }

    private static void verifyAuthoritativeItem(Minecraft client) {
        if (client.player == null) return;
        if (countLooseStone(client) != 4 || (bundlePhase && countLooseDiamonds(client) != 4)) return;
        if (++authoritativeTicks < 3) return;
        client.player.closeContainer();
        advance(Stage.WAIT_CLOSE);
    }

    private static void finishWhenClosed(Minecraft client) throws Exception {
        if (client.player == null
                || client.player.containerMenu != client.player.inventoryMenu
                || countLooseStone(client) != 4) return;
        if (!bundlePhase) {
            beginBundle(client);
            return;
        }
        if (countLooseDiamonds(client) != 4 || stageTicks < 10) return;
        writeResult("PASS client="
                + System.getProperty("quickshulker.matrix.expectedClient")
                + " direct="
                + System.getProperty("quickshulker.matrix.expectedDirect")
                + " shulker=4 bundle=4");
        stage = Stage.DONE;
        client.stop();
    }

    private static void beginBundle(Minecraft client) throws Exception {
        bundlePhase = true;
        authoritativeTicks = 0;
        if (System.getProperty("quickshulker.matrix.expectedClient").equals("none")) {
            client.gameMode.handleContainerInput(client.player.inventoryMenu.containerId,
                    10, 0, ContainerInput.SWAP, client.player);
            advance(Stage.WAIT_HELD);
        } else {
            openModded(client, 10);
        }
    }

    private static void openModded(Minecraft client, int slot) throws Exception {
        Class<?> packet = Class.forName(LEGACY_PACKET_CLASS);
        if (System.getProperty("quickshulker.matrix.expectedClient").equals("new")) {
            packet.getMethod("sendOpenPacket", int.class, ItemStack.class)
                    .invoke(null, slot, client.player.getInventory().getItem(slot));
        } else {
            packet.getMethod("sendOpenPacket", int.class).invoke(null, slot);
        }
        advance(Stage.WAIT_MENU);
    }

    private static int countLooseDiamonds(Minecraft client) {
        return client.player.getInventory().getNonEquipmentItems().stream()
                .filter(stack -> stack.is(Items.DIAMOND))
                .mapToInt(ItemStack::getCount).sum();
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

    private static String describeState(Minecraft client) {
        String state = "stage=" + stage + " bundle=" + bundlePhase
                + " screen=" + (client.gui.screen() == null ? "none" : client.gui.screen().getClass().getName());
        if (client.player == null) return state + " player=absent";
        ItemStack box = client.player.getInventory().getItem(9);
        return state + " player=" + client.player.getName().getString()
                + " alive=" + client.player.isAlive()
                + " slot9=" + box + " storedStone=" + countStoredStone(box)
                + " held=" + client.player.getMainHandItem()
                + " menu=" + client.player.containerMenu.getClass().getName();
    }

    private static void advance(Stage next) {
        System.out.println("[compat-test] " + stage + " -> " + next + " bundle=" + bundlePhase);
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
        WAIT_DIRECT,
        WAIT_HELD,
        WAIT_MENU,
        WAIT_AUTHORITATIVE_ITEM,
        WAIT_CLOSE,
        DONE
    }
}
