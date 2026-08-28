package net.kyrptonaught.quickshulker.api.shulker.server;

import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.SlottedStorage;
import net.minecraft.server.level.ServerPlayer;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.util.Arrays;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Freezes the deliberately small server API before its first release. */
public final class ShulkerStorageApiContractTest {
    @Test
    void serverApiOnlyResolvesOneCarriedShulkerAsFabricStorage() throws Exception {
        Method method = ShulkerStorages.class.getMethod(
                "findCarried", ServerPlayer.class, int.class);
        assertTrue(Modifier.isPublic(method.getModifiers()));
        assertTrue(Modifier.isStatic(method.getModifiers()));
        assertEquals(Optional.class, method.getReturnType());

        ParameterizedType optional =
                (ParameterizedType) method.getGenericReturnType();
        ParameterizedType slotted =
                (ParameterizedType) optional.getActualTypeArguments()[0];
        assertEquals(SlottedStorage.class, slotted.getRawType());
        assertEquals(ItemVariant.class, slotted.getActualTypeArguments()[0]);

        long publicMethods = Arrays.stream(ShulkerStorages.class.getDeclaredMethods())
                .filter(candidate -> Modifier.isPublic(candidate.getModifiers()))
                .count();
        assertEquals(1, publicMethods,
                "Composition helpers belong in callers, not the storage API");
    }
}
