package net.kyrptonaught.quickshulker.internal.storage;

import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.SlottedStorage;
import net.fabricmc.fabric.api.transfer.v1.storage.StoragePreconditions;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleSlotStorage;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.kyrptonaught.quickshulker.api.QuickShulkerData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.function.Function;
import java.util.function.Supplier;

/** Applies Quick Shulker's insertion policy to an existing Fabric storage. */
public final class PolicySlottedStorage implements SlottedStorage<ItemVariant> {
    private final Player player;
    private final QuickShulkerData data;
    private final Supplier<ItemStack> hostSupplier;
    private final Function<ItemStack, Container> policyInventorySupplier;
    private final BooleanSupplier available;
    private final SlottedStorage<ItemVariant> delegate;
    private final List<SingleSlotStorage<ItemVariant>> delegateSlots;
    private final List<SingleSlotStorage<ItemVariant>> slots;

    public PolicySlottedStorage(
            Player player,
            QuickShulkerData data,
            Supplier<ItemStack> hostSupplier,
            Function<ItemStack, Container> policyInventorySupplier,
            BooleanSupplier available,
            SlottedStorage<ItemVariant> delegate) {
        if (player == null) throw new IllegalArgumentException("player");
        if (data == null) throw new IllegalArgumentException("data");
        if (hostSupplier == null) throw new IllegalArgumentException("hostSupplier");
        if (policyInventorySupplier == null) {
            throw new IllegalArgumentException("policyInventorySupplier");
        }
        if (available == null) throw new IllegalArgumentException("available");
        if (delegate == null) throw new IllegalArgumentException("delegate");

        this.player = player;
        this.data = data;
        this.hostSupplier = hostSupplier;
        this.policyInventorySupplier = policyInventorySupplier;
        this.available = available;
        this.delegate = delegate;
        this.delegateSlots = List.copyOf(delegate.getSlots());

        List<SingleSlotStorage<ItemVariant>> wrapped =
                new ArrayList<>(delegateSlots.size());
        for (SingleSlotStorage<ItemVariant> slot : delegateSlots) {
            wrapped.add(new RestrictedSlot(slot));
        }
        this.slots = List.copyOf(wrapped);
    }

    @Override
    public int getSlotCount() {
        requireOwningThread();
        return slots.size();
    }

    @Override
    public SingleSlotStorage<ItemVariant> getSlot(int slot) {
        requireOwningThread();
        return slots.get(slot);
    }

    @Override
    public List<SingleSlotStorage<ItemVariant>> getSlots() {
        requireOwningThread();
        return slots;
    }

    @Override
    public long insert(ItemVariant resource,
                       long maxAmount,
                       TransactionContext transaction) {
        StoragePreconditions.notBlankNotNegative(resource, maxAmount);
        requireOwningThread();
        if (!accepts(resource, maxAmount)) return 0;

        long inserted = 0;
        for (int pass = 0; pass < 2 && inserted < maxAmount; pass++) {
            for (SingleSlotStorage<ItemVariant> slot : delegateSlots) {
                if ((pass == 0) == slot.isResourceBlank()) continue;
                inserted += insertFully(
                        slot, resource, maxAmount - inserted, transaction);
                if (inserted == maxAmount) break;
            }
        }
        return inserted;
    }

    private static long insertFully(
            SingleSlotStorage<ItemVariant> slot,
            ItemVariant resource,
            long maxAmount,
            TransactionContext transaction) {
        long inserted = 0;
        while (inserted < maxAmount) {
            // Component-backed empty slots may only learn the resource's real
            // capacity after accepting their first item.
            long step = slot.insert(
                    resource, maxAmount - inserted, transaction);
            if (step <= 0) break;
            inserted += step;
        }
        return inserted;
    }

    @Override
    public long extract(ItemVariant resource,
                        long maxAmount,
                        TransactionContext transaction) {
        StoragePreconditions.notBlankNotNegative(resource, maxAmount);
        requireOwningThread();
        if (!isAvailable()) return 0;

        long extracted = 0;
        for (SingleSlotStorage<ItemVariant> slot : delegateSlots) {
            extracted += slot.extract(
                    resource, maxAmount - extracted, transaction);
            if (extracted == maxAmount) break;
        }
        return extracted;
    }

    @Override
    public Iterator<StorageView<ItemVariant>> iterator() {
        requireOwningThread();
        @SuppressWarnings({"unchecked", "rawtypes"})
        Iterator<StorageView<ItemVariant>> iterator = (Iterator) slots.iterator();
        return iterator;
    }

    @Override
    public long getVersion() {
        requireOwningThread();
        return delegate.getVersion();
    }

    private boolean accepts(ItemVariant variant, long offeredAmount) {
        if (!isAvailable() || offeredAmount == 0) return false;

        ItemStack host = hostSupplier.get();
        if (host == null || host.isEmpty()) return false;
        Container inventory = policyInventorySupplier.apply(host);
        if (inventory == null) return false;

        int stackAmount = (int) Math.min(offeredAmount, Integer.MAX_VALUE);
        return data.canBundleInsertItem(
                player, inventory, host, variant.toStack(stackAmount));
    }

    private boolean isAvailable() {
        return available.getAsBoolean();
    }

    private long capacity(SingleSlotStorage<ItemVariant> slot) {
        if (!isAvailable()) return 0;
        long delegateCapacity = slot.getCapacity();
        if (!slot.isResourceBlank()) return delegateCapacity;

        ItemStack host = hostSupplier.get();
        if (host == null || host.isEmpty()) return delegateCapacity;
        Container inventory = policyInventorySupplier.apply(host);
        return inventory == null
                ? delegateCapacity
                : Math.max(delegateCapacity, inventory.getMaxStackSize());
    }

    private void requireOwningThread() {
        if (player instanceof ServerPlayer serverPlayer
                && !serverPlayer.level().getServer().isSameThread()) {
            throw new IllegalStateException(
                    "shulker storage access must run on the owning server thread");
        }
    }

    private final class RestrictedSlot implements SingleSlotStorage<ItemVariant> {
        private final SingleSlotStorage<ItemVariant> delegateSlot;

        private RestrictedSlot(SingleSlotStorage<ItemVariant> delegateSlot) {
            this.delegateSlot = delegateSlot;
        }

        @Override
        public long insert(ItemVariant resource,
                           long maxAmount,
                           TransactionContext transaction) {
            StoragePreconditions.notBlankNotNegative(resource, maxAmount);
            requireOwningThread();
            return accepts(resource, maxAmount)
                    ? insertFully(
                    delegateSlot, resource, maxAmount, transaction)
                    : 0;
        }

        @Override
        public long extract(ItemVariant resource,
                            long maxAmount,
                            TransactionContext transaction) {
            StoragePreconditions.notBlankNotNegative(resource, maxAmount);
            requireOwningThread();
            return isAvailable()
                    ? delegateSlot.extract(resource, maxAmount, transaction)
                    : 0;
        }

        @Override
        public boolean isResourceBlank() {
            requireOwningThread();
            return !isAvailable() || delegateSlot.isResourceBlank();
        }

        @Override
        public ItemVariant getResource() {
            requireOwningThread();
            return isAvailable()
                    ? delegateSlot.getResource()
                    : ItemVariant.blank();
        }

        @Override
        public long getAmount() {
            requireOwningThread();
            return isAvailable() ? delegateSlot.getAmount() : 0;
        }

        @Override
        public long getCapacity() {
            requireOwningThread();
            return capacity(delegateSlot);
        }

        @Override
        public StorageView<ItemVariant> getUnderlyingView() {
            return delegateSlot.getUnderlyingView();
        }

        @Override
        public long getVersion() {
            requireOwningThread();
            return delegateSlot.getVersion();
        }
    }
}
