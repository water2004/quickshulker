package net.kyrptonaught.quickshulker.legacytest;

import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.kyrptonaught.quickshulker.api.QuickOpenableRegistry;
import net.kyrptonaught.quickshulker.api.QuickShulkerData;
import net.kyrptonaught.quickshulker.api.Util;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.ShulkerBoxBlock;

import java.util.concurrent.atomic.AtomicInteger;

public final class LegacyRegistryGameTests {
    @GameTest
    public void selectedProfileMatchesLoadedQuickShulker(GameTestHelper helper) {
        String version = FabricLoader.getInstance()
                .getModContainer("quickshulker")
                .orElseThrow()
                .getMetadata()
                .getVersion()
                .getFriendlyString();
        if (LegacyTestSupport.baseline()) {
            helper.assertTrue(version.startsWith("3.0.4"),
                    "The baseline profile must load QuickShulker 3.0.4, got " + version);
        } else {
            helper.assertTrue(!version.startsWith("3.0.4"),
                    "The current profile must not accidentally load the baseline jar");
        }
        helper.succeed();
    }

    @GameTest
    public void quickShulkerDataDefaultsAreStable(GameTestHelper helper) {
        QuickShulkerData data = new QuickShulkerData();
        helper.assertTrue(!data.supportsBundleing,
                "Bundling must remain opt-in");
        helper.assertTrue(!data.ignoreSingleStackCheck,
                "Singular-stack enforcement must remain the default");
        helper.assertTrue(data.canOpenInHand,
                "Hand opening must remain enabled by default");
        helper.succeed();
    }

    @GameTest
    public void constructorArgumentsPopulateObservableFields(GameTestHelper helper) {
        AtomicInteger opens = new AtomicInteger();
        QuickShulkerData data = new QuickShulkerData(
                (player, stack) -> opens.incrementAndGet(), true, true);
        data.openConsumer.accept(LegacyTestSupport.player(helper), ItemStack.EMPTY);

        helper.assertValueEqual(opens.get(), 1,
                "The supplied opening callback must be retained");
        helper.assertTrue(data.supportsBundleing,
                "The constructor must retain bundling support");
        helper.assertTrue(data.ignoreSingleStackCheck,
                "The constructor must retain the stack-count policy");
        helper.succeed();
    }

    @GameTest
    public void registrationsOverwriteByExactRuntimeClass(GameTestHelper helper) {
        try (LegacyTestSupport.RegistrySnapshot ignored =
                     LegacyTestSupport.registrySnapshot()) {
            QuickShulkerData first = new QuickShulkerData();
            QuickShulkerData second = new QuickShulkerData();
            QuickOpenableRegistry.register(
                    LegacyTestSupport.FirstItemLike.class, first);
            QuickOpenableRegistry.register(
                    LegacyTestSupport.FirstItemLike.class, second);

            helper.assertTrue(QuickOpenableRegistry.getQuickie(
                            new LegacyTestSupport.FirstItemLike()) == second,
                    "A later exact-class registration must replace the earlier one");
        }
        helper.succeed();
    }

    @GameTest
    @SuppressWarnings("unchecked")
    public void blockRegistrationPrecedesBlockItemRegistration(GameTestHelper helper) {
        try (LegacyTestSupport.RegistrySnapshot snapshot =
                     LegacyTestSupport.registrySnapshot()) {
            QuickShulkerData blockData = new QuickShulkerData();
            QuickShulkerData itemData = new QuickShulkerData();
            Class<? extends ItemLike> itemClass =
                    (Class<? extends ItemLike>) Items.SHULKER_BOX.getClass();
            QuickOpenableRegistry.register(itemClass, itemData);
            QuickOpenableRegistry.register(ShulkerBoxBlock.class, blockData);

            helper.assertTrue(QuickOpenableRegistry.getQuickie(
                            Items.SHULKER_BOX) == blockData,
                    "A concrete block-class registration must take precedence");
            snapshot.remove(ShulkerBoxBlock.class);
            helper.assertTrue(QuickOpenableRegistry.getQuickie(
                            Items.SHULKER_BOX) == itemData,
                    "The item runtime class must remain the fallback");
        }
        helper.succeed();
    }

    @GameTest
    public void builderSharesOneLiveRegistrationAcrossTypes(GameTestHelper helper) {
        try (LegacyTestSupport.RegistrySnapshot ignored =
                     LegacyTestSupport.registrySnapshot()) {
            QuickShulkerData data = new QuickShulkerData();
            new QuickOpenableRegistry.Builder(data)
                    .setItem(LegacyTestSupport.FirstItemLike.class,
                            LegacyTestSupport.SecondItemLike.class)
                    .supportsBundleing(true)
                    .canOpenInHand(false)
                    .register();

            QuickShulkerData first = QuickOpenableRegistry.getQuickie(
                    new LegacyTestSupport.FirstItemLike());
            QuickShulkerData second = QuickOpenableRegistry.getQuickie(
                    new LegacyTestSupport.SecondItemLike());
            helper.assertTrue(first == data && second == data,
                    "All builder target types must reference the supplied data object");
            data.canOpenInHand = true;
            helper.assertTrue(first.canOpenInHand && second.canOpenInHand,
                    "Post-registration field changes remain live legacy behavior");
        }
        helper.succeed();
    }

    @GameTest
    public void bundlingSupportGatesInventoryResolution(GameTestHelper helper) {
        ServerPlayer player = LegacyTestSupport.player(helper);
        ItemStack stone = new ItemStack(Items.STONE);
        AtomicInteger resolutions = new AtomicInteger();
        try (LegacyTestSupport.RegistrySnapshot ignored =
                     LegacyTestSupport.registrySnapshot()) {
            new QuickOpenableRegistry.Builder()
                    .setItem(Items.STONE.getClass())
                    .supportsBundleing(false)
                    .getBundleInv((ignoredPlayer, ignoredStack) -> {
                        resolutions.incrementAndGet();
                        return new SimpleContainer(1);
                    })
                    .register();
            helper.assertTrue(Util.getQuickItemInventory(player, stone) == null,
                    "A registered item without bundling support has no quick inventory");
            helper.assertValueEqual(resolutions.get(), 0,
                    "The inventory callback must not run while bundling is disabled");

            QuickOpenableRegistry.getQuickie(Items.STONE).supportsBundleing = true;
            helper.assertTrue(Util.getQuickItemInventory(player, stone) != null,
                    "Enabling the live registration must expose its inventory");
            helper.assertValueEqual(resolutions.get(), 1,
                    "The inventory callback must run exactly once for one lookup");
        }
        helper.succeed();
    }

    @GameTest
    public void openabilityAndHandPolicyRemainIndependent(GameTestHelper helper) {
        try (LegacyTestSupport.RegistrySnapshot ignored =
                     LegacyTestSupport.registrySnapshot()) {
            new QuickOpenableRegistry.Builder()
                    .setItem(Items.STONE.getClass())
                    .canOpenInHand(false)
                    .ignoreSingleStackCheck(false)
                    .register();
            ItemStack stacked = new ItemStack(Items.STONE, 2);
            helper.assertTrue(!Util.isOpenableItem(stacked),
                    "Stacked registered items are not openable by default");
            helper.assertTrue(!Util.canOpenInHand(stacked),
                    "Hand-opening policy is queried independently");

            QuickOpenableRegistry.getQuickie(Items.STONE)
                    .ignoreSingleStackCheck = true;
            helper.assertTrue(Util.isOpenableItem(stacked),
                    "The explicit stack-count override must be honored");
        }
        helper.succeed();
    }

    @GameTest
    public void defaultInsertionPolicyRejectsNestedShulkers(GameTestHelper helper) {
        ServerPlayer player = LegacyTestSupport.player(helper);
        QuickShulkerData data = new QuickShulkerData();
        SimpleContainer inventory = new SimpleContainer(27);
        ItemStack host = LegacyTestSupport.box();

        helper.assertTrue(data.canBundleInsertItem(
                        player, inventory, host, new ItemStack(Items.STONE, 64)),
                "Ordinary items must pass the default insertion policy");
        helper.assertTrue(!data.canBundleInsertItem(
                        player, inventory, host, LegacyTestSupport.box()),
                "Nested shulker boxes must fail the default insertion policy");
        helper.succeed();
    }

    @GameTest
    public void customInsertionPolicyReceivesFullStackCount(GameTestHelper helper) {
        ServerPlayer player = LegacyTestSupport.player(helper);
        AtomicInteger observed = new AtomicInteger(-1);
        QuickShulkerData data = new QuickShulkerData();
        try (LegacyTestSupport.RegistrySnapshot ignored =
                     LegacyTestSupport.registrySnapshot()) {
            new QuickOpenableRegistry.Builder(data)
                    .setItem(Items.SHULKER_BOX.getClass(),
                            ShulkerBoxBlock.class)
                    .canBundleInsertItem((ignoredPlayer, ignoredInventory,
                                         ignoredHost, offered) -> {
                        observed.set(offered.getCount());
                        return true;
                    })
                    .register();
            data.canBundleInsertItem(player, new SimpleContainer(27),
                    LegacyTestSupport.box(), new ItemStack(Items.STONE, 37));
            helper.assertValueEqual(observed.get(), 37,
                    "The legacy callback must observe the caller's full stack count");
        }
        helper.succeed();
    }

    @GameTest
    public void itemEqualityIgnoresCountButNotComponents(GameTestHelper helper) {
        ItemStack one = new ItemStack(Items.STONE, 1);
        ItemStack many = new ItemStack(Items.STONE, 42);
        ItemStack other = new ItemStack(Items.DIRT, 1);

        helper.assertTrue(Util.areItemsEqualExactly(one, many),
                "Exact legacy equality intentionally ignores stack count");
        helper.assertTrue(Util.areItemsEqualOnlyType(one, many),
                "Type equality must ignore stack count and components");
        helper.assertTrue(!Util.areItemsEqualOnlyType(one, other),
                "Different item types must not compare equal");
        helper.succeed();
    }
}
