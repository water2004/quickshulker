package net.kyrptonaught.quickshulker.mixin;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.kyrptonaught.quickshulker.QuickShulkerMod;
import net.kyrptonaught.quickshulker.client.ClientUtil;
import net.kyrptonaught.quickshulker.client.QuickShulkerModClient;
import net.kyrptonaught.quickshulker.util.MouseDraggedHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractContainerScreen.class)
@Environment(EnvType.CLIENT)
public abstract class ScreenMixin {
    @Shadow
    protected Slot hoveredSlot;

    @Shadow
    @Final
    protected AbstractContainerMenu menu;

    @Shadow private boolean skipNextRelease;

    @Inject(method = "init", at = @At("TAIL"))
    private void fixMouse(CallbackInfo ci) {
        if (QuickShulkerMod.lastMouseX != 0 && QuickShulkerMod.lastMouseY != 0) {
            InputConstants.grabOrReleaseMouse(Minecraft.getInstance().getWindow().getWindow(), org.lwjgl.glfw.GLFW.GLFW_CURSOR_NORMAL, QuickShulkerMod.lastMouseX, QuickShulkerMod.lastMouseY);
            QuickShulkerMod.lastMouseY = 0;
            QuickShulkerMod.lastMouseX = 0;
        }
    }


    @Inject(method = "keyPressed", at = @At("HEAD"), cancellable = true)
    private void QS$keyPressed(int keyCode, int scanCode, int modifiers, CallbackInfoReturnable<Boolean> cir) {
        if (QuickShulkerMod.getConfig().keybingInInv) {
            if (QuickShulkerModClient.getKeybinding().matches(keyCode, InputConstants.Type.KEYSYM)) {
                if (handleTrigger())
                    cir.setReturnValue(true);
            }
        }
    }

    @Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true)
    private void QS$mousePressed(double mouseX, double mouseY, int button, CallbackInfoReturnable<Boolean> cir) {
        if (QuickShulkerMod.getConfig().rightClickInv) {
            if (this.menu.getCarried().isEmpty() && button == 1 && this.hoveredSlot != null && this.hoveredSlot.getItem().getCount() == 1) {
                if (handleTrigger()) {
                    this.skipNextRelease = true;
                    cir.setReturnValue(true);
                    return;
                }
            }
        }
        if (QuickShulkerMod.getConfig().keybingInInv) {
            if (QuickShulkerModClient.getKeybinding().matches(button, InputConstants.Type.MOUSE)) {
                if (handleTrigger()) {
                    this.skipNextRelease = true;
                    cir.setReturnValue(true);
                    return;
                }
            }
        }
    }

    @Inject(
            method = "render(Lnet/minecraft/client/gui/GuiGraphics;IIF)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/screens/inventory/AbstractContainerScreen;renderLabels(Lnet/minecraft/client/gui/GuiGraphics;II)V",
                    shift = At.Shift.AFTER
            )
    )
    private void QS$drawForeground(GuiGraphics context, int mouseX, int mouseY, float deltaTicks, CallbackInfo ci){
        AbstractContainerScreen<?> screen  = (AbstractContainerScreen<?>) (Object) this;
        MouseDraggedHandler.beforeDrawForeground(screen, context, mouseX, mouseY);
    }

    @Unique
    private boolean handleTrigger() {
        if (this.hoveredSlot != null) {
            return isValid(this.hoveredSlot.getItem(), ClientUtil.getSlotId(menu, this.hoveredSlot));
        }
        return false;
    }

    @Unique
    private boolean isValid(ItemStack stack, int id) {
        if (this.hoveredSlot.container instanceof Inventory)
            if (ClientUtil.CheckAndSend(stack, id)) {
                QuickShulkerMod.lastMouseX = Minecraft.getInstance().mouseHandler.xpos();
                QuickShulkerMod.lastMouseY = Minecraft.getInstance().mouseHandler.ypos();
                return true;
            }
        return false;
    }
}
