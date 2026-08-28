package net.kyrptonaught.quickshulker;

import net.kyrptonaught.quickshulker.network.EnderChestS2CSyncPacket;
import net.kyrptonaught.quickshulker.internal.shulker.network.ShulkerTransferProtocol;
import net.kyrptonaught.quickshulker.network.OpenInventoryPacket;
import net.kyrptonaught.quickshulker.network.OpenShulkerPacket;
import net.kyrptonaught.quickshulker.network.QuickBundlePacket;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.loader.api.FabricLoader;
import net.kyrptonaught.kyrptconfig.config.ConfigManager;
import net.kyrptonaught.quickshulker.api.*;
import net.kyrptonaught.quickshulker.config.ConfigOptions;
import net.kyrptonaught.quickshulker.event.EventListeners;
import net.kyrptonaught.quickshulker.gui.screen.BundleContainer;
import net.kyrptonaught.quickshulker.gui.screen.BundleItemMenu;
import net.kyrptonaught.quickshulker.gui.screen.PagedBundleItemMenu;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.BundleItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class QuickShulkerMod implements ModInitializer, RegisterQuickShulker {

    public static final String MOD_ID = "quickshulker";
    public static ConfigManager.SingleConfigManager config = new ConfigManager.SingleConfigManager(MOD_ID, new ConfigOptions());
    public static double lastMouseX, lastMouseY;
    public static Logger LOGGER = LogManager.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        config.load();
        ShulkerTransferProtocol.register();
        OpenShulkerPacket.registerReceivePacket();
        QuickBundlePacket.registerReceivePacket();
        EventListeners.registerEventListeners();
        UseItemCallback.EVENT.register((player, level, hand) -> {
            ItemStack stack = player.getItemInHand(hand);
            if (QuickShulkerMod.getConfig().rightClickToOpen) {
                if (Util.isOpenableItem(stack) && Util.canOpenInHand(stack)) {
                    if(level.isClientSide()){
                        if (OpenShulkerPacket.canSendOpenPacket()) {
                            int menuSlot = hand == InteractionHand.MAIN_HAND
                                    ? 36 + player.getInventory().getSelectedSlot()
                                    : 45;
                            OpenShulkerPacket.sendOpenPacket(menuSlot, stack);
                            return InteractionResult.FAIL;
                        }
                        // The server does not provide Quick Shulker. Do not consume the
                        // interaction; let vanilla or another mod handle the item normally.
                        return InteractionResult.PASS;
                    }else{
                        int playerInvSlot = hand == InteractionHand.MAIN_HAND
                                ? player.getInventory().getSelectedSlot()
                                : Inventory.SLOT_OFFHAND;
                        if (stack.getItem() instanceof BundleItem) {
                            openPagedBundle(player, stack, playerInvSlot);
                        } else {
                            Util.openItem(player, 0, playerInvSlot);
                        }
                        return InteractionResult.SUCCESS_SERVER;
                    }
                }
            }
            return InteractionResult.PASS;
        });

        PayloadTypeRegistry.clientboundPlay().register(OpenInventoryPacket.OPEN_INV_ID, OpenInventoryPacket.CODEC);
        PayloadTypeRegistry.clientboundPlay().register(EnderChestS2CSyncPacket.S2CEChestContentPacket.S2C_ECHEST_CONTENT_PACKET_ID, EnderChestS2CSyncPacket.S2CEChestContentPacket.CODEC);
        PayloadTypeRegistry.clientboundPlay().register(EnderChestS2CSyncPacket.S2CEChestSlotPacket.S2C_ECHEST_SLOT_PACKET_ID, EnderChestS2CSyncPacket.S2CEChestSlotPacket.CODEC);

        FabricLoader.getInstance().getEntrypoints(MOD_ID, RegisterQuickShulker.class).forEach(RegisterQuickShulker::registerProviders);
    }

    public static ConfigOptions getConfig() {
        return (ConfigOptions) config.getConfig();
    }

    private static void openPagedBundle(Player player, ItemStack stack, int playerInvSlot) {
        Component title = stack.getComponents().has(DataComponents.CUSTOM_NAME)
                ? stack.getHoverName()
                : Component.translatable("item.minecraft.bundle");
        player.openMenu(new SimpleMenuProvider((containerId, playerInventory, menuPlayer) ->
                new PagedBundleItemMenu(containerId, playerInventory, new BundleContainer(stack, 64)), title));
        ((ItemInventoryContainer) player.containerMenu).setUsedSlot(playerInvSlot);
        player.containerMenu.addSlotListener(Util.forceCloseScreenIfNotPresent(player, playerInvSlot, stack.copy()));
    }

    @Override
    public void registerProviders() {
        if (getConfig().quickShulkerBox)
            new QuickOpenableRegistry.Builder()
                    .setItem(ShulkerBoxBlock.class)
                    .supportsBundleing(true)
                    .setOpenAction(((player, stack) -> player.openMenu(new SimpleMenuProvider((i, playerInventory, playerEntity) ->
                            new ShulkerBoxMenu(i, player.getInventory(), new ItemStackInventory(stack, 27)), stack.getComponents().has(DataComponents.CUSTOM_NAME) ? stack.getHoverName() : Component.translatable("container.shulkerBox")))))
                    .register();

        if (getConfig().quickEChest)
            new QuickOpenableRegistry.Builder(new QuickShulkerData.QuickEnderData())
                    .setItem(EnderChestBlock.class)
                    .supportsBundleing(true)
                    .ignoreSingleStackCheck(true)
                    .setOpenAction(((player, stack) -> player.openMenu(new SimpleMenuProvider((i, playerInventory, playerEntity) ->
                            ChestMenu.threeRows(i, playerInventory, player.getEnderChestInventory()), Component.translatable("container.enderchest")))))
                    .register();

        if (getConfig().quickCraftingTables)
            new QuickOpenableRegistry.Builder()
                    .setItem(CraftingTableBlock.class)
                    .ignoreSingleStackCheck(true)
                    .setOpenAction(((player, stack) -> player.openMenu(new SimpleMenuProvider((i, playerInventory, playerEntity) ->
                            new CraftingMenu(i, playerInventory, ContainerLevelAccess.create(player.level(), player.blockPosition())), Component.translatable("container.crafting")))))
                    .register();

        if (getConfig().quickStonecutter)
            new QuickOpenableRegistry.Builder()
                    .setItem(StonecutterBlock.class)
                    .ignoreSingleStackCheck(true)
                    .setOpenAction(((player, stack) -> player.openMenu(new SimpleMenuProvider((i, playerInventory, playerEntity) ->
                            new StonecutterMenu(i, playerInventory, ContainerLevelAccess.create(player.level(), player.blockPosition())), Component.translatable("container.stonecutter")))))
                    .register();

        if(getConfig().quickBundle)
            new QuickOpenableRegistry.Builder()
                    .setItem(BundleItem.class)
                    .setOpenAction((playerEntity, stack) -> playerEntity.openMenu(new SimpleMenuProvider((i, playerInventory, player) ->
                            new BundleItemMenu(i, playerInventory, new BundleContainer(stack, 64)), stack.getComponents().has(DataComponents.CUSTOM_NAME) ? stack.getHoverName() : Component.translatable("item.minecraft.bundle"))))
                    .register();

        if(getConfig().quickAnvil)
            new QuickOpenableRegistry.Builder()
                    .setItem(AnvilBlock.class)
                    .setOpenAction((playerEntity, stack) -> playerEntity.openMenu(new SimpleMenuProvider((i, playerInventory, player) ->
                            new AnvilMenu(i, playerInventory, new ModContainerLevelAccess(playerEntity, stack)), Component.translatable("container.repair"))))
                    .register();
    }

}
