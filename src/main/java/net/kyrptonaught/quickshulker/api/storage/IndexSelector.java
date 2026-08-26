package net.kyrptonaught.quickshulker.api.storage;

/** Selects indexes independently from their traversal order. */
public record IndexSelector(Mode mode, int index, Order order) {
    public enum Mode {
        ANY,
        EXACT,
        PREFERRED_THEN_ANY
    }

    public enum Order {
        FORWARD,
        REVERSE
    }

    public IndexSelector(Mode mode, int index) {
        this(mode, index, Order.FORWARD);
    }

    public IndexSelector {
        if (mode == null) throw new IllegalArgumentException("mode");
        if (order == null) throw new IllegalArgumentException("order");
        if (mode != Mode.ANY && index < 0) throw new IllegalArgumentException("index");
    }

    public static IndexSelector any() {
        return new IndexSelector(Mode.ANY, -1, Order.FORWARD);
    }

    public static IndexSelector anyReverse() {
        return new IndexSelector(Mode.ANY, -1, Order.REVERSE);
    }

    public static IndexSelector exact(int index) {
        return new IndexSelector(Mode.EXACT, index, Order.FORWARD);
    }

    public static IndexSelector preferredThenAny(int index) {
        return new IndexSelector(Mode.PREFERRED_THEN_ANY, index, Order.FORWARD);
    }
}
