package net.kyrptonaught.quickshulker.internal.shulker.client;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

/** Holds only unfinished requests for one client connection. */
final class SerialRequestQueue<T> {
    private final int capacity;
    private final Deque<T> waiting = new ArrayDeque<>();
    private T active;

    SerialRequestQueue(int capacity) {
        if (capacity <= 0) throw new IllegalArgumentException("capacity");
        this.capacity = capacity;
    }

    boolean offer(T request) {
        if (request == null) throw new IllegalArgumentException("request");
        if (retainedCount() >= capacity) return false;
        waiting.addLast(request);
        return true;
    }

    T active() {
        return active;
    }

    T activateNext() {
        if (active == null) active = waiting.pollFirst();
        return active;
    }

    T completeActive() {
        T completed = active;
        active = null;
        return completed;
    }

    List<T> clear() {
        List<T> abandoned = new ArrayList<>(retainedCount());
        if (active != null) abandoned.add(active);
        active = null;
        abandoned.addAll(waiting);
        waiting.clear();
        return abandoned;
    }

    int retainedCount() {
        return waiting.size() + (active == null ? 0 : 1);
    }
}
