package net.kyrptonaught.quickshulker.api.shulker;

import net.minecraft.world.item.ItemStack;

/** Item identity filter used by shulker transfer requests. Stack counts are ignored. */
public record ShulkerItemFilter(Mode mode, ItemStack template) {
    public enum Mode {
        ANY,
        SAME_ITEM,
        SAME_ITEM_AND_COMPONENTS
    }

    public ShulkerItemFilter {
        if (mode == null) throw new IllegalArgumentException("mode");
        template = template == null ? ItemStack.EMPTY : template.copy();
        if (mode != Mode.ANY && template.isEmpty()) {
            throw new IllegalArgumentException("matching filters need a template");
        }
        if (!template.isEmpty()) template.setCount(1);
    }

    public static ShulkerItemFilter any() {
        return new ShulkerItemFilter(Mode.ANY, ItemStack.EMPTY);
    }

    public static ShulkerItemFilter sameItem(ItemStack template) {
        return new ShulkerItemFilter(Mode.SAME_ITEM, template);
    }

    public static ShulkerItemFilter sameItemAndComponents(ItemStack template) {
        return new ShulkerItemFilter(Mode.SAME_ITEM_AND_COMPONENTS, template);
    }

    @Override
    public ItemStack template() {
        return template.copy();
    }

    public boolean matches(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return false;
        return switch (mode) {
            case ANY -> true;
            case SAME_ITEM -> ItemStack.isSameItem(stack, template);
            case SAME_ITEM_AND_COMPONENTS -> ItemStack.isSameItemSameComponents(stack, template);
        };
    }
}
