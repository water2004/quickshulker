package net.kyrptonaught.quickshulker.api;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import net.kyrptonaught.quickshulker.api.storage.QuickStorageProvider;
import net.kyrptonaught.quickshulker.api.storage.QuickStorageRegistry;
import net.kyrptonaught.quickshulker.internal.StorageRegistryBackend;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

public class QuickOpenableRegistry {
    public static QuickShulkerData getQuickie(ItemLike item) {
        return StorageRegistryBackend.legacyData(item);
    }

    public static void register(Class<? extends ItemLike> quickItem, QuickShulkerData quickShulkerData) {
        QuickStorageProvider provider = quickShulkerData.supportsBundleing
                ? QuickStorageRegistry.legacyProvider(quickShulkerData)
                : null;
        StorageRegistryBackend.registerLegacy(quickItem, quickShulkerData, provider);
    }

    @Deprecated
    public static void register(Class<? extends ItemLike> quickItem, Boolean requiresSingularStack, Boolean supportsBundleing, BiConsumer<Player, ItemStack> consumer) {
        register(quickItem, new QuickShulkerData(consumer, supportsBundleing));
    }

    @Deprecated
    public static void register(Class<? extends ItemLike> quickItem, Boolean supportsBundleing, BiConsumer<Player, ItemStack> consumer) {
        register(quickItem, new QuickShulkerData(consumer, supportsBundleing));
    }

    @Deprecated
    public static void register(Class<? extends ItemLike> quickItem, BiConsumer<Player, ItemStack> consumer) {
        register(quickItem, new QuickShulkerData(consumer, false));
    }

    @SafeVarargs
    @Deprecated
    public static void register(BiConsumer<Player, ItemStack> consumer, Class<? extends ItemLike>... quickItems) {
        for (Class<? extends ItemLike> block : quickItems) {
            register(block, consumer);
        }
    }

    public static class Builder {
        private final List<Class<? extends ItemLike>> quickItems = new ArrayList<>();
        private final QuickShulkerData qsdata;

        public Builder() {
            qsdata = new QuickShulkerData();
        }

        public Builder(QuickShulkerData qsdata) {
            this.qsdata = qsdata;
        }

        public void register() {
            for (Class<? extends ItemLike> quickItem : quickItems)
                QuickOpenableRegistry.register(quickItem, qsdata);
        }

        @SafeVarargs
        public final Builder setItem(Class<? extends ItemLike>... quickItems) {
            this.quickItems.addAll(List.of(quickItems));
            return this;
        }

        public Builder setOpenAction(BiConsumer<Player, ItemStack> openAction) {
            qsdata.openConsumer = openAction;
            return this;
        }

        public Builder supportsBundleing(Boolean supportsBundleing) {
            qsdata.supportsBundleing = supportsBundleing;
            return this;
        }

        public Builder getBundleInv(BiFunction<Player, ItemStack, Container> getBundleInv) {
            qsdata.bundleInvGetter = getBundleInv;
            return this;
        }

        public Builder canBundleInsertItem(CanBundleInsertItemFunction canBundleInsertItem) {
            qsdata.canBundleInsertItem = canBundleInsertItem;
            return this;
        }

        public Builder canOpenInHand(boolean canOpenInHand) {
            qsdata.canOpenInHand = canOpenInHand;
            return this;
        }

        public Builder ignoreSingleStackCheck(Boolean ignoreSingleStackCheck) {
            qsdata.ignoreSingleStackCheck = ignoreSingleStackCheck;
            return this;
        }
    }
}
