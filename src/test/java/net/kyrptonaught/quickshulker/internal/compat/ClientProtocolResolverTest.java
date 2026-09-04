package net.kyrptonaught.quickshulker.internal.compat;

import org.junit.jupiter.api.Test;

import static net.kyrptonaught.quickshulker.internal.compat.ClientProtocolResolver.ClientProtocol.ORIGINAL_V3;
import static net.kyrptonaught.quickshulker.internal.compat.ClientProtocolResolver.ClientProtocol.V4;
import static net.kyrptonaught.quickshulker.internal.compat.ClientProtocolResolver.ClientProtocol.VANILLA;
import static org.junit.jupiter.api.Assertions.assertEquals;

public final class ClientProtocolResolverTest {
    @Test
    void resolvesOnlyAdvertisedConnectionCapabilities() {
        assertEquals(VANILLA, ClientProtocolResolver.resolve(false, false));
        assertEquals(ORIGINAL_V3, ClientProtocolResolver.resolve(true, false));
        assertEquals(V4, ClientProtocolResolver.resolve(true, true));
        assertEquals(V4, ClientProtocolResolver.resolve(false, true));
    }
}
