package net.kyrptonaught.quickshulker.test;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.item.component.BundleContents;
import java.nio.file.Files;
import java.nio.file.Path;

import java.util.List;

public final class MatrixServerInitializer implements ModInitializer {
    @Override
    public void onInitialize() {
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) ->
                server.execute(() -> {
                    var player = handler.getPlayer();
                    player.getInventory().clearContent();
                    ItemStack box = new ItemStack(Items.SHULKER_BOX);
                    box.set(DataComponents.CONTAINER, ItemContainerContents.fromItems(
                            List.of(new ItemStack(Items.STONE, 4))));
                    player.getInventory().setItem(9, box);
                    ItemStack bundle = new ItemStack(Items.BUNDLE);
                    var contents = new BundleContents.Mutable(BundleContents.EMPTY);
                    contents.tryInsert(new ItemStack(Items.DIAMOND, 4));
                    bundle.set(DataComponents.BUNDLE_CONTENTS, contents.toImmutable());
                    player.getInventory().setItem(10, bundle);
                    player.inventoryMenu.sendAllDataToRemote();
                    Path output = Path.of(System.getProperty("quickshulker.matrix.serverResult"));
                    try { Files.deleteIfExists(output); } catch (Exception e) { throw new RuntimeException(e); }
                }));
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            String stopFile = System.getProperty("quickshulker.matrix.serverStop");
            if (stopFile != null && Files.exists(Path.of(stopFile))) {
                server.halt(false);
                return;
            }
            for (var player : server.getPlayerList().getPlayers()) {
                var items = player.getInventory().getNonEquipmentItems();
                int stone = items.stream().filter(s -> s.is(Items.STONE)).mapToInt(ItemStack::getCount).sum();
                int diamond = items.stream().filter(s -> s.is(Items.DIAMOND)).mapToInt(ItemStack::getCount).sum();
                boolean boxesEmpty = items.stream().filter(s -> s.is(Items.SHULKER_BOX))
                        .allMatch(s -> s.getOrDefault(DataComponents.CONTAINER, ItemContainerContents.EMPTY)
                                .nonEmptyItemCopyStream().findAny().isEmpty());
                boolean bundlesEmpty = items.stream().filter(s -> s.is(Items.BUNDLE))
                        .allMatch(s -> s.getOrDefault(DataComponents.BUNDLE_CONTENTS, BundleContents.EMPTY).isEmpty());
                if (stone != 4 || diamond != 4 || !boxesEmpty || !bundlesEmpty
                        || player.containerMenu != player.inventoryMenu) continue;
                Path output = Path.of(System.getProperty("quickshulker.matrix.serverResult"));
                if (Files.exists(output)) continue;
                try {
                    Files.createDirectories(output.getParent());
                    Files.writeString(output, "PASS authoritative shulker=4 bundle=4 player=" + player.getName().getString());
                } catch (Exception e) { throw new RuntimeException(e); }
            }
        });
    }
}
