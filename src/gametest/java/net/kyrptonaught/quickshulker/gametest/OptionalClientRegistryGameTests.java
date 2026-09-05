package net.kyrptonaught.quickshulker.gametest;

import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.fabricmc.fabric.impl.registry.sync.RegistrySyncManager;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTestHelper;

/** The actual Fabric handshake map, not a mock player's chosen menu. */
public final class OptionalClientRegistryGameTests {
    @GameTest
    public void quickShulkerNeverRequiresClientRegistryEntries(GameTestHelper helper) {
        var map = RegistrySyncManager.createAndPopulateRegistryMap();
        if (map != null) {
            map.forEach((registry, entries) -> entries.keySet().forEach(entry ->
                    helper.assertTrue(!entry.getNamespace().equals("quickshulker"),
                            "Quick Shulker makes a client registry mandatory: " + registry + "/" + entry)));
        }
        helper.assertTrue(BuiltInRegistries.MENU.keySet().stream()
                        .noneMatch(id -> id.getNamespace().equals("quickshulker")),
                "Choosing a vanilla menu at open time is too late to make a custom menu optional");
        helper.succeed();
    }
}
