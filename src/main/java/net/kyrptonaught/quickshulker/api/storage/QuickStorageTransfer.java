package net.kyrptonaught.quickshulker.api.storage;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** Executes direction-neutral storage transfers on the server thread. */
public final class QuickStorageTransfer {
    private QuickStorageTransfer() {
    }

    public static TransferResult execute(Player player, TransferSpec spec) {
        if (player == null || spec == null) return TransferResult.empty(TransferStatus.INVALID_ENDPOINT);

        ResolutionContext context = new ResolutionContext(player);
        List<SlotRef> sources;
        List<SlotRef> destinations;
        try {
            sources = context.resolve(spec.source());
            destinations = context.resolve(spec.destination());
        } catch (RuntimeException error) {
            context.close();
            return TransferResult.empty(TransferStatus.ERROR);
        }

        if (sources.isEmpty() || destinations.isEmpty()) {
            context.close();
            return TransferResult.empty(TransferStatus.INVALID_ENDPOINT);
        }

        Map<SlotRef, ItemStack> snapshots = new IdentityHashMap<>();
        List<PendingTransfer> pending = new ArrayList<>();
        Set<SlotRef> usedDestinations = Collections.newSetFromMap(new IdentityHashMap<>());
        int moved = 0;
        int usedSources = 0;
        boolean matchedSource = false;

        try {
            for (SlotRef source : sources) {
                if (moved >= spec.limit().maxAmount()
                        || usedSources >= spec.limit().maxSourceSlots()) break;

                ItemStack sourceStack = source.get();
                if (!source.matchesCondition() || !spec.matcher().matches(sourceStack)
                        || !source.mayExtract(sourceStack)) continue;

                matchedSource = true;
                usedSources++;

                for (SlotRef destination : destinations) {
                    if (moved >= spec.limit().maxAmount()) break;
                    if (source.sameLocation(destination)) continue;
                    if (!usedDestinations.contains(destination)
                            && usedDestinations.size() >= spec.limit().maxDestinationSlots()) continue;

                    sourceStack = source.get();
                    if (sourceStack.isEmpty()) break;
                    if (!destination.matchesCondition() || !destination.mayInsert(sourceStack)) continue;

                    ItemStack targetStack = destination.get();
                    int capacity = capacity(targetStack, sourceStack,
                            destination.maxStackSize(sourceStack));
                    if (capacity <= 0) continue;

                    int amount = Math.min(capacity, sourceStack.getCount());
                    amount = Math.min(amount, spec.limit().maxAmount() - moved);
                    if (amount <= 0) break;

                    snapshots.putIfAbsent(source, sourceStack.copy());
                    snapshots.putIfAbsent(destination, targetStack.copy());

                    ItemStack movedStack = sourceStack.copyWithCount(amount);
                    ItemStack newTarget = targetStack.isEmpty()
                            ? movedStack.copy()
                            : targetStack.copyWithCount(targetStack.getCount() + amount);
                    ItemStack newSource = sourceStack.copy();
                    newSource.shrink(amount);

                    destination.set(newTarget);
                    source.set(newSource);
                    source.markDirty();
                    destination.markDirty();
                    usedDestinations.add(destination);
                    moved += amount;
                    pending.add(new PendingTransfer(source, destination, movedStack));
                }
            }

            context.commit();
            player.getInventory().setChanged();
            if (player instanceof ServerPlayer serverPlayer) {
                serverPlayer.inventoryMenu.broadcastChanges();
            }

            if (moved == 0) {
                return TransferResult.empty(matchedSource ? TransferStatus.NO_SPACE : TransferStatus.NO_MATCH);
            }

            List<ResolvedTransfer> resolved = new ArrayList<>(pending.size());
            for (PendingTransfer transfer : pending) {
                resolved.add(new ResolvedTransfer(
                        transfer.source().resolved(),
                        transfer.destination().resolved(),
                        transfer.movedStack()));
            }
            return new TransferResult(TransferStatus.SUCCESS, moved, resolved);
        } catch (RuntimeException error) {
            for (Map.Entry<SlotRef, ItemStack> entry : snapshots.entrySet()) {
                try {
                    entry.getKey().set(entry.getValue().copy());
                    entry.getKey().markDirty();
                } catch (RuntimeException ignored) {
                }
            }
            context.commit();
            return TransferResult.empty(TransferStatus.ERROR);
        } finally {
            context.close();
        }
    }

    private static int capacity(ItemStack target, ItemStack source, int destinationMaximum) {
        int maximum = Math.max(0, Math.min(destinationMaximum, source.getMaxStackSize()));
        if (target.isEmpty()) return maximum;
        if (!ItemStack.isSameItemSameComponents(target, source)) return 0;
        return Math.max(0, Math.min(maximum, target.getMaxStackSize()) - target.getCount());
    }

    private record PendingTransfer(SlotRef source, SlotRef destination, ItemStack movedStack) {
    }

    private static final class ResolutionContext {
        private final Player player;
        private final Map<ItemStack, OpenedStorage> storages = new IdentityHashMap<>();

        private ResolutionContext(Player player) {
            this.player = player;
        }

        private List<SlotRef> resolve(TransferEndpoint endpoint) {
            if (endpoint instanceof PlayerInventoryEndpoint playerEndpoint) {
                return resolvePlayerInventory(playerEndpoint);
            }
            if (endpoint instanceof CarriedStorageEndpoint storageEndpoint) {
                return resolveCarriedStorage(storageEndpoint);
            }
            if (endpoint instanceof MutableStackEndpoint stackEndpoint) {
                return List.of(SlotRef.mutableStack(stackEndpoint.stack()));
            }
            if (endpoint instanceof StorageItemEndpoint storageEndpoint) {
                OpenedStorage storage = openStorage(storageEndpoint.hostStack(), -1);
                return storage == null ? List.of() : storageSlots(storage, storageEndpoint.slots());
            }
            if (endpoint instanceof MenuSlotEndpoint menuEndpoint) {
                return List.of(SlotRef.menuSlot(player, menuEndpoint.slot()));
            }
            return List.of();
        }

        private List<SlotRef> resolvePlayerInventory(PlayerInventoryEndpoint endpoint) {
            Inventory inventory = player.getInventory();
            int size = Math.min(36, inventory.getContainerSize());
            List<SlotRef> result = new ArrayList<>();
            for (int slot : indexes(endpoint.slots().indexes(), size)) {
                SlotRef ref = SlotRef.playerInventory(inventory, slot, endpoint.slots().condition());
                if (ref.matchesCondition()) result.add(ref);
            }
            return result;
        }

        private List<SlotRef> resolveCarriedStorage(CarriedStorageEndpoint endpoint) {
            Inventory inventory = player.getInventory();
            int size = Math.min(36, inventory.getContainerSize());
            List<SlotRef> result = new ArrayList<>();
            for (int hostSlot : indexes(endpoint.storage().inventorySlots(), size)) {
                ItemStack host = inventory.getItem(hostSlot);
                if (!endpoint.storage().hostMatcher().matches(host)) continue;
                OpenedStorage storage = openStorage(host, hostSlot);
                if (storage != null) result.addAll(storageSlots(storage, endpoint.slots()));
            }
            return result;
        }

        private OpenedStorage openStorage(ItemStack host, int hostInventorySlot) {
            if (host == null || host.isEmpty()) return null;
            OpenedStorage existing = storages.get(host);
            if (existing != null) return existing;

            QuickStorageProvider provider = QuickStorageRegistry.get(host.getItem());
            if (provider == null || !provider.canAccess(player, host)) return null;
            StorageAccess access = provider.open(player, host);
            if (access == null || access.container() == null) return null;

            OpenedStorage opened = new OpenedStorage(host, hostInventorySlot, access);
            storages.put(host, opened);
            return opened;
        }

        private List<SlotRef> storageSlots(OpenedStorage storage, SlotSelector selector) {
            List<SlotRef> result = new ArrayList<>();
            int size = storage.access.container().getContainerSize();
            for (int slot : indexes(selector.indexes(), size)) {
                SlotRef ref = SlotRef.storage(storage, slot, selector.condition());
                if (ref.matchesCondition()) result.add(ref);
            }
            return result;
        }

        private void commit() {
            for (OpenedStorage storage : new LinkedHashSet<>(storages.values())) {
                if (storage.dirty) storage.access.commit();
            }
        }

        private void close() {
            for (OpenedStorage storage : new LinkedHashSet<>(storages.values())) {
                try {
                    storage.access.close();
                } catch (RuntimeException ignored) {
                }
            }
        }
    }

    private static List<Integer> indexes(IndexSelector selector, int size) {
        if (size <= 0) return List.of();
        if (selector.mode() == IndexSelector.Mode.EXACT) {
            return selector.index() < size ? List.of(selector.index()) : List.of();
        }

        List<Integer> result = new ArrayList<>(size);
        if (selector.mode() == IndexSelector.Mode.PREFERRED_THEN_ANY
                && selector.index() < size) {
            result.add(selector.index());
        }
        for (int i = 0; i < size; i++) {
            if (!result.contains(i)) result.add(i);
        }
        if (selector.order() == IndexSelector.Order.REVERSE) {
            Collections.reverse(result);
        }
        return result;
    }

    private static final class OpenedStorage {
        private final ItemStack host;
        private final int hostInventorySlot;
        private final StorageAccess access;
        private boolean dirty;

        private OpenedStorage(ItemStack host, int hostInventorySlot, StorageAccess access) {
            this.host = host;
            this.hostInventorySlot = hostInventorySlot;
            this.access = access;
        }
    }

    private abstract static class SlotRef {
        private final SlotSelector.Condition condition;
        private final Object locationOwner;
        private final int locationSlot;

        private SlotRef(SlotSelector.Condition condition, Object locationOwner, int locationSlot) {
            this.condition = condition;
            this.locationOwner = locationOwner;
            this.locationSlot = locationSlot;
        }

        abstract ItemStack get();

        abstract void set(ItemStack stack);

        abstract boolean mayInsert(ItemStack stack);

        int maxStackSize(ItemStack stack) {
            return stack.getMaxStackSize();
        }

        boolean mayExtract(ItemStack stack) {
            return true;
        }

        void markDirty() {
        }

        abstract ResolvedEndpoint resolved();

        boolean matchesCondition() {
            ItemStack stack = get();
            return switch (condition) {
                case ANY -> true;
                case EMPTY -> stack.isEmpty();
                case NON_EMPTY -> !stack.isEmpty();
            };
        }

        boolean sameLocation(SlotRef other) {
            return locationOwner == other.locationOwner && locationSlot == other.locationSlot;
        }

        static SlotRef playerInventory(Inventory inventory, int slot,
                                       SlotSelector.Condition condition) {
            return new SlotRef(condition, inventory, slot) {
                @Override
                ItemStack get() {
                    return inventory.getItem(slot);
                }

                @Override
                void set(ItemStack stack) {
                    inventory.setItem(slot, stack);
                }

                @Override
                boolean mayInsert(ItemStack stack) {
                    return inventory.canPlaceItem(slot, stack);
                }

                @Override
                int maxStackSize(ItemStack stack) {
                    return inventory.getMaxStackSize(stack);
                }

                @Override
                ResolvedEndpoint resolved() {
                    return new ResolvedEndpoint(ResolvedEndpoint.Kind.PLAYER_INVENTORY,
                            -1, slot, ItemStack.EMPTY, get());
                }

            };
        }

        static SlotRef mutableStack(ItemStack stack) {
            return new SlotRef(SlotSelector.Condition.ANY, stack, 0) {
                @Override
                ItemStack get() {
                    return stack;
                }

                @Override
                void set(ItemStack replacement) {
                    if (replacement.isEmpty()) {
                        stack.setCount(0);
                    } else if (ItemStack.isSameItemSameComponents(stack, replacement)) {
                        stack.setCount(replacement.getCount());
                    } else {
                        throw new IllegalArgumentException("mutable stack endpoints cannot change item type");
                    }
                }

                @Override
                boolean mayInsert(ItemStack inserted) {
                    return ItemStack.isSameItemSameComponents(stack, inserted)
                            && stack.getCount() < stack.getMaxStackSize();
                }

                @Override
                ResolvedEndpoint resolved() {
                    return new ResolvedEndpoint(ResolvedEndpoint.Kind.MUTABLE_STACK,
                            -1, -1, ItemStack.EMPTY, stack);
                }
            };
        }

        static SlotRef menuSlot(Player player, Slot slot) {
            return new SlotRef(SlotSelector.Condition.ANY, slot, 0) {
                @Override
                ItemStack get() {
                    return slot.getItem();
                }

                @Override
                void set(ItemStack replacement) {
                    ItemStack current = slot.getItem();
                    if (!current.isEmpty()
                            && ItemStack.isSameItemSameComponents(current, replacement)
                            && replacement.getCount() < current.getCount()) {
                        int removed = current.getCount() - replacement.getCount();
                        slot.safeTake(removed, removed, player);
                        return;
                    }
                    if ((current.isEmpty()
                            || ItemStack.isSameItemSameComponents(current, replacement))
                            && replacement.getCount() > current.getCount()) {
                        int inserted = replacement.getCount() - current.getCount();
                        ItemStack addition = replacement.copyWithCount(inserted);
                        ItemStack remainder = slot.safeInsert(addition, inserted);
                        if (!remainder.isEmpty()) {
                            throw new IllegalStateException("menu slot accepted fewer items than planned");
                        }
                        return;
                    }
                    slot.set(replacement);
                }

                @Override
                boolean mayInsert(ItemStack stack) {
                    return slot.mayPlace(stack);
                }

                @Override
                int maxStackSize(ItemStack stack) {
                    return slot.getMaxStackSize(stack);
                }

                @Override
                boolean mayExtract(ItemStack stack) {
                    return slot.mayPickup(player);
                }

                @Override
                ResolvedEndpoint resolved() {
                    return new ResolvedEndpoint(ResolvedEndpoint.Kind.MENU_SLOT,
                            -1, slot.index, ItemStack.EMPTY, get());
                }
            };
        }

        static SlotRef storage(OpenedStorage storage, int slot,
                               SlotSelector.Condition condition) {
            return new SlotRef(condition, storage, slot) {
                @Override
                ItemStack get() {
                    return storage.access.container().getItem(slot);
                }

                @Override
                void set(ItemStack stack) {
                    storage.access.container().setItem(slot, stack);
                }

                @Override
                boolean mayInsert(ItemStack stack) {
                    return storage.access.canInsert(slot, stack);
                }

                @Override
                int maxStackSize(ItemStack stack) {
                    return storage.access.container().getMaxStackSize(stack);
                }

                @Override
                boolean mayExtract(ItemStack stack) {
                    return storage.access.canExtract(slot, stack);
                }

                @Override
                void markDirty() {
                    storage.dirty = true;
                }

                @Override
                ResolvedEndpoint resolved() {
                    ResolvedEndpoint.Kind kind = storage.hostInventorySlot >= 0
                            ? ResolvedEndpoint.Kind.CARRIED_STORAGE
                            : ResolvedEndpoint.Kind.STORAGE_ITEM;
                    return new ResolvedEndpoint(kind, storage.hostInventorySlot, slot,
                            storage.host, get());
                }

            };
        }
    }
}
