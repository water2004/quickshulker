package net.kyrptonaught.quickshulker.mixin;

import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(AbstractContainerScreen.class)
public interface HandledScreenInvoker {
    @Invoker("findSlot")
    Slot QS$getSlotAt(double mouseX, double mouseY);

    @Invoker("slotClicked")
    void QS$onMouseClick(Slot slot, int slotId, int button, ClickType actionType);
}
