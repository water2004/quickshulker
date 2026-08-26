package net.kyrptonaught.quickshulker.api;

import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.function.BiConsumer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/** Guards binary source-facing signatures that integrations used before the storage API. */
public final class LegacyApiCompatibilityTest {
    @Test
    void legacyQuickShulkerDataFieldsAndMethodsRemainPublic() throws Exception {
        assertPublicField(QuickShulkerData.class, "openConsumer", BiConsumer.class);
        assertPublicField(QuickShulkerData.class, "supportsBundleing", boolean.class);
        assertPublicField(QuickShulkerData.class, "ignoreSingleStackCheck", boolean.class);
        assertPublicField(QuickShulkerData.class, "canOpenInHand", boolean.class);

        assertPublicMethod(QuickShulkerData.class, "getInventory", Container.class,
                Player.class, ItemStack.class);
        assertPublicMethod(QuickShulkerData.class, "canBundleInsertItem", boolean.class,
                Player.class, Container.class, ItemStack.class, ItemStack.class);
    }

    @Test
    void legacyRegistryEntryPointsRemainPublic() throws Exception {
        assertPublicMethod(QuickOpenableRegistry.class, "getQuickie", QuickShulkerData.class,
                ItemLike.class);
        assertPublicMethod(QuickOpenableRegistry.class, "register", void.class,
                Class.class, QuickShulkerData.class);
    }

    private static void assertPublicField(Class<?> owner, String name, Class<?> type)
            throws Exception {
        Field field = owner.getField(name);
        assertEquals(type, field.getType());
        assertEquals(true, Modifier.isPublic(field.getModifiers()));
    }

    private static void assertPublicMethod(Class<?> owner, String name, Class<?> returnType,
                                           Class<?>... parameters) throws Exception {
        Method method = owner.getMethod(name, parameters);
        assertNotNull(method);
        assertEquals(returnType, method.getReturnType());
        assertEquals(true, Modifier.isPublic(method.getModifiers()));
    }
}
