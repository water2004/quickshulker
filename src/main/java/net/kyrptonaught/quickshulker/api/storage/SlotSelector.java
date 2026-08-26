package net.kyrptonaught.quickshulker.api.storage;

/** Combines positional selection with a destination/source slot condition. */
public record SlotSelector(IndexSelector indexes, Condition condition) {
    public enum Condition {
        ANY,
        EMPTY,
        NON_EMPTY
    }

    public SlotSelector {
        if (indexes == null) throw new IllegalArgumentException("indexes");
        if (condition == null) throw new IllegalArgumentException("condition");
    }

    public static SlotSelector any() {
        return new SlotSelector(IndexSelector.any(), Condition.ANY);
    }

    public static SlotSelector empty() {
        return new SlotSelector(IndexSelector.any(), Condition.EMPTY);
    }

    public static SlotSelector empty(int index) {
        return new SlotSelector(IndexSelector.exact(index), Condition.EMPTY);
    }

    public static SlotSelector nonEmpty() {
        return new SlotSelector(IndexSelector.any(), Condition.NON_EMPTY);
    }

    public static SlotSelector nonEmpty(int index) {
        return new SlotSelector(IndexSelector.exact(index), Condition.NON_EMPTY);
    }

    public static SlotSelector anyReverse() {
        return new SlotSelector(IndexSelector.anyReverse(), Condition.ANY);
    }

    public static SlotSelector exact(int index) {
        return new SlotSelector(IndexSelector.exact(index), Condition.ANY);
    }
}
