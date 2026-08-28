package net.kyrptonaught.quickshulker.api.shulker.client;

import net.kyrptonaught.quickshulker.api.shulker.ShulkerTransferRequest;
import net.kyrptonaught.quickshulker.api.shulker.ShulkerTransferResult;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

/** Freezes the small client facade before the protocol's first release. */
public final class ShulkerClientApiContractTest {
    @Test
    void clientFacadeExposesOnlyCapabilityAndSubmission() {
        assertEquals(Set.of(
                        "isAvailable():boolean",
                        "submit(ShulkerTransferRequest):ShulkerTransferHandle"),
                publicMethods(ShulkerTransferClient.class));
        assertEquals(Set.of(
                        "isDone():boolean",
                        "resultOrNull():ShulkerTransferResult"),
                publicMethods(ShulkerTransferHandle.class));

        for (Constructor<?> constructor :
                ShulkerTransferHandle.class.getDeclaredConstructors()) {
            assertFalse(Modifier.isPublic(constructor.getModifiers()),
                    "Only QuickShulker may create transfer handles");
        }
    }

    private static Set<String> publicMethods(Class<?> type) {
        return Arrays.stream(type.getDeclaredMethods())
                .filter(method -> Modifier.isPublic(method.getModifiers()))
                .filter(method -> !method.isSynthetic())
                .map(ShulkerClientApiContractTest::signature)
                .collect(Collectors.toSet());
    }

    private static String signature(Method method) {
        String parameters = Arrays.stream(method.getParameterTypes())
                .map(Class::getSimpleName)
                .collect(Collectors.joining(","));
        return method.getName() + "(" + parameters + "):"
                + method.getReturnType().getSimpleName();
    }
}
