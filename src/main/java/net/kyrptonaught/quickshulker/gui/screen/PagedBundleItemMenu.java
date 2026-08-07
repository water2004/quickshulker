package net.kyrptonaught.quickshulker.gui.screen;

import net.kyrptonaught.quickshulker.api.ItemInventoryContainer;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.BundleContents;

/**
 * Server-side bundle menu backed by the vanilla six-row chest protocol.
 * Unmodded clients can therefore use it without knowing any QuickShulker menu type.
 */
public class PagedBundleItemMenu extends AbstractContainerMenu {
    private static final int BUNDLE_SIZE = 64;
    private static final int CONTENT_SLOTS = 45;
    private static final int CONTROL_SLOTS = 9;
    private static final int MENU_SLOTS = CONTENT_SLOTS + CONTROL_SLOTS;
    private static final int PAGE_COUNT = (BUNDLE_SIZE + CONTENT_SLOTS - 1) / CONTENT_SLOTS;
    private static final int PREVIOUS_SLOT = CONTENT_SLOTS + 3;
    private static final int PAGE_SLOT = CONTENT_SLOTS + 4;
    private static final int NEXT_SLOT = CONTENT_SLOTS + 5;

    private final BundleContainer backing;
    private final PagedBundleContainer pageView;
    private final SimpleContainer controls = new SimpleContainer(CONTROL_SLOTS);
    private int page;

    public PagedBundleItemMenu(int containerId, Inventory playerInventory, BundleContainer backing) {
        super(MenuType.GENERIC_9x6, containerId);
        checkContainerSize(backing, BUNDLE_SIZE);
        this.backing = backing;
        this.pageView = new PagedBundleContainer(backing, CONTENT_SLOTS);
        backing.startOpen(playerInventory.player);

        for (int slot = 0; slot < CONTENT_SLOTS; slot++) {
            int column = slot % 9;
            int row = slot / 9;
            addSlot(new BundlePageSlot(pageView, backing, slot, 8 + column * 18, 18 + row * 18));
        }

        for (int slot = 0; slot < CONTROL_SLOTS; slot++) {
            addSlot(new ControlSlot(controls, slot, 8 + slot * 18, 18 + 5 * 18));
        }

        updateControls();
        addStandardInventorySlots(playerInventory, 8, 18 + 6 * 18 + 13);
    }

    @Override
    public void clicked(int slotId, int button, ContainerInput input, Player player) {
        if (slotId >= CONTENT_SLOTS && slotId < MENU_SLOTS) {
            if (input == ContainerInput.PICKUP && slotId == PREVIOUS_SLOT) setPage(page - 1);
            else if (input == ContainerInput.PICKUP && slotId == NEXT_SLOT) setPage(page + 1);
            else sendAllDataToRemote();
            return;
        }

        int usedPlayerSlot = ((ItemInventoryContainer) this).getUsedSlotInPlayerInv();
        if (slotId >= MENU_SLOTS && slotId < slots.size()) {
            Slot slot = slots.get(slotId);
            if (slot.container instanceof Inventory && slot.getContainerSlot() == usedPlayerSlot) {
                sendAllDataToRemote();
                return;
            }
        }
        super.clicked(slotId, button, input, player);
    }

    private void setPage(int requestedPage) {
        int newPage = Math.max(0, Math.min(PAGE_COUNT - 1, requestedPage));
        if (newPage == page) return;
        page = newPage;
        pageView.setPage(page);
        updateControls();
        sendAllDataToRemote();
    }

    private void updateControls() {
        controls.clearContent();
        if (page > 0) controls.setItem(3, namedStack(Items.ARROW, Component.translatable("spectatorMenu.previous_page")));
        controls.setItem(4, namedStack(Items.PAPER, Component.translatable("book.pageIndicator", page + 1, PAGE_COUNT)));
        if (page + 1 < PAGE_COUNT) controls.setItem(5, namedStack(Items.ARROW, Component.translatable("spectatorMenu.next_page")));
    }

    private static ItemStack namedStack(net.minecraft.world.level.ItemLike item, Component name) {
        ItemStack stack = new ItemStack(item);
        stack.set(DataComponents.CUSTOM_NAME, name);
        return stack;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int slotId) {
        if (slotId < 0 || slotId >= slots.size() || slotId >= CONTENT_SLOTS && slotId < MENU_SLOTS) {
            return ItemStack.EMPTY;
        }

        Slot slot = slots.get(slotId);
        if (!slot.hasItem()) return ItemStack.EMPTY;

        ItemStack source = slot.getItem();
        ItemStack original = source.copy();
        if (slotId < CONTENT_SLOTS) {
            if (!moveItemStackTo(source, MENU_SLOTS, slots.size(), true)) return ItemStack.EMPTY;
        } else {
            int moved = insertIntoBundle(source);
            if (moved == 0) return ItemStack.EMPTY;
            source.shrink(moved);
        }

        if (source.isEmpty()) slot.setByPlayer(ItemStack.EMPTY);
        else slot.setChanged();
        return original;
    }

    private int insertIntoBundle(ItemStack source) {
        if (!BundleContents.canItemBeInBundle(source)) return 0;

        int allowed = Math.min(source.getCount(), backing.countCanInsertToBundle(source));
        int remaining = allowed;

        for (int slot = 0; slot < backing.getContainerSize() && remaining > 0; slot++) {
            ItemStack target = backing.getItem(slot);
            if (!target.isEmpty() && ItemStack.isSameItemSameComponents(source, target)) {
                int moved = Math.min(remaining, backing.getMaxStackSize(target) - target.getCount());
                if (moved > 0) {
                    target.grow(moved);
                    remaining -= moved;
                }
            }
        }

        for (int slot = 0; slot < backing.getContainerSize() && remaining > 0; slot++) {
            if (backing.getItem(slot).isEmpty()) {
                int moved = Math.min(remaining, backing.getMaxStackSize(source));
                backing.setItem(slot, source.copyWithCount(moved));
                remaining -= moved;
            }
        }

        int inserted = allowed - remaining;
        if (inserted > 0) backing.setChanged();
        return inserted;
    }

    @Override
    public boolean stillValid(Player player) {
        return backing.stillValid(player);
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        backing.stopOpen(player);
    }

    private static class ControlSlot extends Slot {
        ControlSlot(Container container, int slot, int x, int y) {
            super(container, slot, x, y);
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return false;
        }

        @Override
        public boolean mayPickup(Player player) {
            return false;
        }

        @Override
        public boolean allowModification(Player player) {
            return false;
        }
    }

    private static class BundlePageSlot extends Slot {
        private final BundleContainer backing;

        BundlePageSlot(Container pageView, BundleContainer backing, int slot, int x, int y) {
            super(pageView, slot, x, y);
            this.backing = backing;
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return container.canPlaceItem(index, stack)
                    && BundleContents.canItemBeInBundle(stack)
                    && backing.countCanInsertToBundle(stack) > 0;
        }

        @Override
        public ItemStack safeInsert(ItemStack stack, int count) {
            return super.safeInsert(stack, Math.min(count, backing.countCanInsertToBundle(stack)));
        }
    }
}
