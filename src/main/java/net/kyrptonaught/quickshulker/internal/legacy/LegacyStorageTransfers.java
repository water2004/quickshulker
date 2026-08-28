package net.kyrptonaught.quickshulker.internal.legacy;

import net.fabricmc.fabric.api.transfer.v1.item.ContainerStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.SlottedStorage;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageUtil;
import net.fabricmc.fabric.api.transfer.v1.storage.base.CombinedStorage;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleSlotStorage;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.kyrptonaught.quickshulker.api.QuickOpenableRegistry;
import net.kyrptonaught.quickshulker.api.QuickShulkerData;
import net.kyrptonaught.quickshulker.internal.storage.PolicySlottedStorage;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Transactional implementation behind the pre-4.0 bundling entry points.
 *
 * <p>This adapter deliberately resolves a short-lived legacy {@link Container}
 * for each interaction. It preserves the old registry contract while sharing
 * insertion policy enforcement with the public Fabric storage API.</p>
 */
public final class LegacyStorageTransfers {
    private LegacyStorageTransfers() {
    }

    public static boolean moveAll(Player player,
                                  ItemStack sourceHost,
                                  ItemStack destinationHost) {
        LegacyStorage source = open(player, sourceHost);
        LegacyStorage destination = open(player, destinationHost);
        if (source == null || destination == null) return false;

        try (Transaction transaction = Transaction.openOuter()) {
            long moved = StorageUtil.move(
                    reversed(source.storage), destination.storage, ignored -> true,
                    Long.MAX_VALUE, transaction);
            if (moved <= 0) return false;
            transaction.commit();
        }
        source.finish();
        destination.finish();
        return true;
    }

    public static ItemStack extractToSlot(Player player,
                                          ItemStack sourceHost,
                                          Slot destinationSlot) {
        LegacyStorage source = open(player, sourceHost);
        if (source == null) return null;
        MenuSlotStorage destination = new MenuSlotStorage(player, destinationSlot);

        List<SingleSlotStorage<ItemVariant>> slots =
                new ArrayList<>(source.storage.getSlots());
        Collections.reverse(slots);
        try (Transaction transaction = Transaction.openOuter()) {
            for (SingleSlotStorage<ItemVariant> slot : slots) {
                long moved = StorageUtil.move(
                        slot, destination, ignored -> true,
                        Long.MAX_VALUE, transaction);
                if (moved <= 0) continue;
                transaction.commit();
                source.finish();
                return destinationSlot.getItem().copy();
            }
        }
        return null;
    }

    public static ItemStack insert(Player player,
                                   ItemStack destinationHost,
                                   ItemStack sourceStack) {
        LegacyStorage destination = open(player, destinationHost);
        if (destination == null || sourceStack.isEmpty()) return null;

        long inserted;
        try (Transaction transaction = Transaction.openOuter()) {
            inserted = destination.storage.insert(
                    ItemVariant.of(sourceStack), sourceStack.getCount(), transaction);
            if (inserted <= 0) return null;
            transaction.commit();
        }
        destination.finish();
        sourceStack.shrink(Math.toIntExact(inserted));
        return sourceStack;
    }

    public static ItemStack insertFromSlot(Player player,
                                           ItemStack destinationHost,
                                           Slot sourceSlot) {
        LegacyStorage destination = open(player, destinationHost);
        if (destination == null) return null;
        MenuSlotStorage source = new MenuSlotStorage(player, sourceSlot);

        try (Transaction transaction = Transaction.openOuter()) {
            long moved = StorageUtil.move(
                    source, destination.storage, ignored -> true,
                    Long.MAX_VALUE, transaction);
            if (moved <= 0) return null;
            transaction.commit();
        }
        destination.finish();
        return sourceSlot.getItem();
    }

    private static LegacyStorage open(Player player, ItemStack host) {
        if (player == null || host == null || host.isEmpty()) return null;
        QuickShulkerData data = QuickOpenableRegistry.getQuickie(host.getItem());
        if (data == null || !data.supportsBundleing
                || (!data.ignoreSingleStackCheck && host.getCount() != 1)) {
            return null;
        }

        Container container = data.getInventory(player, host);
        if (container == null) return null;
        PolicySlottedStorage storage = new PolicySlottedStorage(
                player,
                data,
                () -> host,
                ignored -> container,
                () -> !host.isEmpty()
                        && QuickOpenableRegistry.getQuickie(host.getItem()) == data
                        && (data.ignoreSingleStackCheck || host.getCount() == 1),
                ContainerStorage.of(container, null));
        return new LegacyStorage(player, container, storage);
    }

    private static Storage<ItemVariant> reversed(
            SlottedStorage<ItemVariant> storage) {
        List<SingleSlotStorage<ItemVariant>> slots =
                new ArrayList<>(storage.getSlots());
        Collections.reverse(slots);
        return new CombinedStorage<>(slots);
    }

    private record LegacyStorage(Player player,
                                 Container container,
                                 PolicySlottedStorage storage) {
        private void finish() {
            container.stopOpen(player);
        }
    }
}
