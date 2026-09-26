package net.kyrptonaught.quickshulker.mixin;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.kyrptonaught.quickshulker.util.MouseDraggedHandler;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Intercept container gestures directly; 1.21.1 dispatches mouse input through lambdas. */
@Mixin(value = AbstractContainerScreen.class, priority = 1001)
@Environment(EnvType.CLIENT)
public class MouseMixin {
    @Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true)
    private void QS$mouseClicked(double x, double y, int button, CallbackInfoReturnable<Boolean> cir) {
        if (MouseDraggedHandler.beforeMouseClick((AbstractContainerScreen<?>) (Object) this, x, y, button)) {
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "mouseReleased", at = @At("HEAD"), cancellable = true)
    private void QS$mouseReleased(double x, double y, int button, CallbackInfoReturnable<Boolean> cir) {
        if (MouseDraggedHandler.beforeMouseReleased((AbstractContainerScreen<?>) (Object) this, x, y, button)) {
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "mouseDragged", at = @At("HEAD"), cancellable = true)
    private void QS$mouseDragged(double x, double y, int button, double offsetX, double offsetY,
                                CallbackInfoReturnable<Boolean> cir) {
        if (MouseDraggedHandler.beforeMouseDragged((AbstractContainerScreen<?>) (Object) this, x, y, button)) {
            cir.setReturnValue(true);
        }
    }
}
