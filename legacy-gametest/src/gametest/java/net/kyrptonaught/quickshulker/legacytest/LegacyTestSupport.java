package net.kyrptonaught.quickshulker.legacytest;

import net.kyrptonaught.quickshulker.api.QuickOpenableRegistry;
import net.kyrptonaught.quickshulker.api.QuickShulkerData;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.ContainerUser;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.level.ItemLike;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

final class LegacyTestSupport {
    private LegacyTestSupport() {
    }

    static ServerPlayer player(GameTestHelper helper) {
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        player.getInventory().clearContent();
        return player;
    }

    static ItemStack box(ItemStack... stacks) {
        ItemStack box = new ItemStack(net.minecraft.world.item.Items.SHULKER_BOX);
        box.set(DataComponents.CONTAINER,
                ItemContainerContents.fromItems(List.of(stacks)));
        return box;
    }

    static List<ItemStack> contents(ItemStack box) {
        ItemContainerContents component = box.get(DataComponents.CONTAINER);
        if (component == null) return List.of();
        List<ItemStack> result = new ArrayList<>();
        component.nonEmptyItemCopyStream().forEach(result::add);
        return result;
    }

    static int count(ItemStack box, Item item) {
        return contents(box).stream()
                .filter(stack -> stack.is(item))
                .mapToInt(ItemStack::getCount)
                .sum();
    }

    static boolean sameContents(ItemStack left, ItemStack right) {
        List<ItemStack> a = contents(left);
        List<ItemStack> b = contents(right);
        if (a.size() != b.size()) return false;
        for (int index = 0; index < a.size(); index++) {
            if (!ItemStack.matches(a.get(index), b.get(index))) return false;
        }
        return true;
    }

    static boolean baseline() {
        return "baseline-3.0.4".equals(
                System.getProperty("quickshulker.legacyBehaviorProfile"));
    }

    static void assertThrows(GameTestHelper helper,
                             Class<? extends Throwable> expected,
                             Runnable action,
                             String message) {
        try {
            action.run();
        } catch (Throwable thrown) {
            if (expected.isInstance(thrown)) return;
            throw helper.assertionException(
                    message + "; expected " + expected.getSimpleName()
                            + " but got " + thrown, thrown);
        }
        helper.fail(message + "; expected " + expected.getSimpleName());
    }

    static RegistrySnapshot registrySnapshot() {
        return new RegistrySnapshot();
    }

    static final class RegistrySnapshot implements AutoCloseable {
        private final Map<Class<? extends ItemLike>, QuickShulkerData> registry;
        private final Map<Class<? extends ItemLike>, QuickShulkerData> snapshot;

        @SuppressWarnings("unchecked")
        private RegistrySnapshot() {
            try {
                Field field = QuickOpenableRegistry.class.getDeclaredField("quickies");
                field.setAccessible(true);
                registry = (Map<Class<? extends ItemLike>, QuickShulkerData>) field.get(null);
                snapshot = new HashMap<>(registry);
            } catch (ReflectiveOperationException exception) {
                throw new AssertionError("Unable to snapshot the legacy registry", exception);
            }
        }

        void remove(Class<? extends ItemLike> type) {
            registry.remove(type);
        }

        @Override
        public void close() {
            registry.clear();
            registry.putAll(snapshot);
        }
    }

    static final class FirstItemLike implements ItemLike {
        @Override
        public Item asItem() {
            return net.minecraft.world.item.Items.STONE;
        }
    }

    static final class SecondItemLike implements ItemLike {
        @Override
        public Item asItem() {
            return net.minecraft.world.item.Items.DIRT;
        }
    }

    static final class DelayedHostContainer extends SimpleContainer {
        private final ItemStack host;
        int closeCount;

        DelayedHostContainer(ItemStack host) {
            super(readSlots(host));
            this.host = host;
        }

        @Override
        public void setChanged() {
            // Deliberately detached: persistence happens only in stopOpen().
        }

        @Override
        public void stopOpen(ContainerUser user) {
            closeCount++;
            List<ItemStack> stacks = new ArrayList<>(getContainerSize());
            for (int slot = 0; slot < getContainerSize(); slot++) {
                stacks.add(getItem(slot).copy());
            }
            host.set(DataComponents.CONTAINER,
                    ItemContainerContents.fromItems(stacks));
        }
    }

    static ItemStack[] readSlots(ItemStack host) {
        NonNullList<ItemStack> stacks = NonNullList.withSize(27, ItemStack.EMPTY);
        ItemContainerContents component = host.get(DataComponents.CONTAINER);
        if (component != null) component.copyInto(stacks);
        return stacks.toArray(ItemStack[]::new);
    }

    static final class TrackingContainer extends SimpleContainer {
        int closeCount;

        TrackingContainer(int size) {
            super(size);
        }

        TrackingContainer(ItemStack... stacks) {
            super(stacks);
        }

        @Override
        public void stopOpen(ContainerUser user) {
            closeCount++;
        }
    }
}
