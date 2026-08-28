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
import java.util.function.BiFunction;

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

        assertPublicConstructor(QuickShulkerData.class);
        assertPublicConstructor(QuickShulkerData.class, BiConsumer.class, Boolean.class);
        assertPublicConstructor(QuickShulkerData.class, BiConsumer.class, Boolean.class,
                Boolean.class);
        assertPublicConstructor(QuickShulkerData.QuickEnderData.class);
        assertPublicConstructor(QuickShulkerData.QuickEnderData.class,
                BiConsumer.class, Boolean.class);
        assertPublicConstructor(QuickShulkerData.QuickEnderData.class,
                BiConsumer.class, Boolean.class, Boolean.class);
    }

    @Test
    void legacyRegistryEntryPointsRemainPublic() throws Exception {
        assertPublicMethod(QuickOpenableRegistry.class, "getQuickie", QuickShulkerData.class,
                ItemLike.class);
        assertPublicMethod(QuickOpenableRegistry.class, "register", void.class,
                Class.class, QuickShulkerData.class);
        assertPublicMethod(QuickOpenableRegistry.class, "register", void.class,
                Class.class, Boolean.class, Boolean.class, BiConsumer.class);
        assertPublicMethod(QuickOpenableRegistry.class, "register", void.class,
                Class.class, Boolean.class, BiConsumer.class);
        assertPublicMethod(QuickOpenableRegistry.class, "register", void.class,
                Class.class, BiConsumer.class);
        assertPublicMethod(QuickOpenableRegistry.class, "register", void.class,
                BiConsumer.class, Class[].class);
        assertPublicMethod(QuickOpenableRegistry.Builder.class, "register", void.class);
        assertPublicMethod(QuickOpenableRegistry.Builder.class, "setItem",
                QuickOpenableRegistry.Builder.class, Class[].class);
        assertPublicMethod(QuickOpenableRegistry.Builder.class, "setOpenAction",
                QuickOpenableRegistry.Builder.class, BiConsumer.class);
        assertPublicMethod(QuickOpenableRegistry.Builder.class, "supportsBundleing",
                QuickOpenableRegistry.Builder.class, Boolean.class);
        assertPublicMethod(QuickOpenableRegistry.Builder.class, "getBundleInv",
                QuickOpenableRegistry.Builder.class, BiFunction.class);
        assertPublicMethod(QuickOpenableRegistry.Builder.class, "canBundleInsertItem",
                QuickOpenableRegistry.Builder.class, CanBundleInsertItemFunction.class);
        assertPublicMethod(QuickOpenableRegistry.Builder.class, "canOpenInHand",
                QuickOpenableRegistry.Builder.class, boolean.class);
        assertPublicMethod(QuickOpenableRegistry.Builder.class, "ignoreSingleStackCheck",
                QuickOpenableRegistry.Builder.class, Boolean.class);
        assertPublicConstructor(QuickOpenableRegistry.Builder.class);
        assertPublicConstructor(QuickOpenableRegistry.Builder.class,
                QuickShulkerData.class);
    }

    @Test
    void legacyExtensionInterfacesAndInventoryWrapperRemainPublic() throws Exception {
        assertPublicMethod(CanBundleInsertItemFunction.class, "canBundleInsertItem",
                boolean.class, Player.class, Container.class, ItemStack.class,
                ItemStack.class);
        assertPublicField(CanBundleInsertItemFunction.class, "ALWAYS",
                CanBundleInsertItemFunction.class);
        assertPublicMethod(RegisterQuickShulker.class, "registerProviders", void.class);
        assertPublicMethod(RegisterQuickShulkerClient.class, "registerClient", void.class);
        assertPublicConstructor(ItemStackInventory.class, ItemStack.class, int.class);
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

    private static void assertPublicConstructor(Class<?> owner, Class<?>... parameters)
            throws Exception {
        assertEquals(true, Modifier.isPublic(
                owner.getConstructor(parameters).getModifiers()));
    }
}
