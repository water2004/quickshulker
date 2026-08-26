package net.kyrptonaught.quickshulker.api.storage;

import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

/** Serializable, composable-enough stack matching value used by transfer endpoints. */
public record StackMatcher(Mode mode, List<ItemStack> candidates) {
    public enum Mode {
        ANY,
        ITEM,
        EXACT
    }

    public StackMatcher {
        if (mode == null) throw new IllegalArgumentException("mode");
        List<ItemStack> copies = new ArrayList<>();
        if (candidates != null) {
            for (ItemStack candidate : candidates) {
                if (candidate != null && !candidate.isEmpty()) copies.add(candidate.copy());
            }
        }
        candidates = List.copyOf(copies);
    }

    public static StackMatcher any() {
        return new StackMatcher(Mode.ANY, List.of());
    }

    public static StackMatcher item(ItemStack candidate) {
        return new StackMatcher(Mode.ITEM, List.of(candidate));
    }

    public static StackMatcher items(List<ItemStack> candidates) {
        return new StackMatcher(Mode.ITEM, candidates);
    }

    public static StackMatcher exact(ItemStack candidate) {
        return new StackMatcher(Mode.EXACT, List.of(candidate));
    }

    public boolean matches(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return false;
        if (mode == Mode.ANY) return true;
        for (ItemStack candidate : candidates) {
            if (mode == Mode.ITEM && ItemStack.isSameItem(stack, candidate)) return true;
            if (mode == Mode.EXACT
                    && stack.getCount() == candidate.getCount()
                    && ItemStack.isSameItemSameComponents(stack, candidate)) return true;
        }
        return false;
    }
}
