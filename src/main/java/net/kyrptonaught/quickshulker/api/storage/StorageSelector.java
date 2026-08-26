package net.kyrptonaught.quickshulker.api.storage;

/** Selects carried storage host stacks independently from their internal slots. */
public record StorageSelector(IndexSelector inventorySlots, StackMatcher hostMatcher) {
    public StorageSelector {
        if (inventorySlots == null) throw new IllegalArgumentException("inventorySlots");
        if (hostMatcher == null) throw new IllegalArgumentException("hostMatcher");
    }

    public static StorageSelector any() {
        return new StorageSelector(IndexSelector.any(), StackMatcher.any());
    }

    public static StorageSelector at(int inventorySlot) {
        return new StorageSelector(IndexSelector.exact(inventorySlot), StackMatcher.any());
    }

    public static StorageSelector matching(StackMatcher matcher) {
        return new StorageSelector(IndexSelector.any(), matcher);
    }

    public static StorageSelector preferredThenMatching(int preferredSlot, StackMatcher matcher) {
        return new StorageSelector(IndexSelector.preferredThenAny(preferredSlot), matcher);
    }
}
