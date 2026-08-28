package net.kyrptonaught.quickshulker.internal.shulker.client;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public final class SerialRequestQueueTest {
    @Test
    void completedRequestsAreNeverRetained() {
        SerialRequestQueue<Integer> queue = new SerialRequestQueue<>(64);

        for (int sequence = 0; sequence < 1_000; sequence++) {
            assertTrue(queue.offer(sequence));
            assertEquals(sequence, queue.activateNext());
            assertEquals(sequence, queue.completeActive());
            assertEquals(0, queue.retainedCount());
        }
    }

    @Test
    void capacityIncludesTheActiveRequest() {
        SerialRequestQueue<Integer> queue = new SerialRequestQueue<>(2);

        assertTrue(queue.offer(1));
        assertEquals(1, queue.activateNext());
        assertTrue(queue.offer(2));
        assertFalse(queue.offer(3));

        assertEquals(1, queue.completeActive());
        assertTrue(queue.offer(3));
    }

    @Test
    void separateClientConnectionsHaveIndependentQueues() {
        SerialRequestQueue<Integer> first = new SerialRequestQueue<>(1);
        SerialRequestQueue<Integer> second = new SerialRequestQueue<>(1);

        assertTrue(first.offer(1));
        assertFalse(first.offer(2));
        assertTrue(second.offer(2));
        assertEquals(1, first.retainedCount());
        assertEquals(1, second.retainedCount());
    }
}
