package net.kyrptonaught.quickshulker.api.storage;

/** A direction-neutral description of an item transfer. */
public record TransferSpec(TransferEndpoint source,
                           TransferEndpoint destination,
                           StackMatcher matcher,
                           TransferLimit limit) {
    public TransferSpec {
        if (source == null || destination == null || matcher == null || limit == null) {
            throw new IllegalArgumentException("transfer spec fields must not be null");
        }
    }

    public boolean isNetworkSafe() {
        return isNetworkEndpoint(source) && isNetworkEndpoint(destination);
    }

    private static boolean isNetworkEndpoint(TransferEndpoint endpoint) {
        return endpoint instanceof PlayerInventoryEndpoint
                || endpoint instanceof CarriedStorageEndpoint;
    }
}
