package net.kyrptonaught.quickshulker.test;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ItemContainerContents;

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
                    player.inventoryMenu.sendAllDataToRemote();
                }));
    }
}
