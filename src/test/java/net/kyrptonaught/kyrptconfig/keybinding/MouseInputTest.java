package net.kyrptonaught.kyrptconfig.keybinding;

import com.mojang.blaze3d.platform.InputConstants;
import net.kyrptonaught.quickshulker.mixin.HandledScreenInvoker;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.Slot;
import org.junit.jupiter.api.Test;
import org.lwjgl.sdl.SDLMouse;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

final class MouseInputTest {
    @Test
    void mousePollingUsesMinecraftButtonCodes() {
        assertEquals(SDLMouse.SDL_BUTTON_LMASK,
                CustomKeyBinding.mouseButtonMask(InputConstants.MOUSE_BUTTON_LEFT));
        assertEquals(SDLMouse.SDL_BUTTON_MMASK,
                CustomKeyBinding.mouseButtonMask(InputConstants.MOUSE_BUTTON_MIDDLE));
        assertEquals(SDLMouse.SDL_BUTTON_RMASK,
                CustomKeyBinding.mouseButtonMask(InputConstants.MOUSE_BUTTON_RIGHT));
        assertEquals(SDLMouse.SDL_BUTTON_X1MASK,
                CustomKeyBinding.mouseButtonMask(InputConstants.MOUSE_BUTTON_4));
        assertEquals(SDLMouse.SDL_BUTTON_X2MASK,
                CustomKeyBinding.mouseButtonMask(InputConstants.MOUSE_BUTTON_5));
        assertEquals(0, CustomKeyBinding.mouseButtonMask(Integer.MIN_VALUE));
    }

    @Test
    void namedMouseBindingsMatchNativeEvents() {
        assertBinding("key.mouse.left", InputConstants.MOUSE_BUTTON_LEFT, SDLMouse.SDL_BUTTON_LMASK);
        assertBinding("key.mouse.middle", InputConstants.MOUSE_BUTTON_MIDDLE, SDLMouse.SDL_BUTTON_MMASK);
        assertBinding("key.mouse.right", InputConstants.MOUSE_BUTTON_RIGHT, SDLMouse.SDL_BUTTON_RMASK);
        assertBinding("key.mouse.4", InputConstants.MOUSE_BUTTON_4, SDLMouse.SDL_BUTTON_X1MASK);
        assertBinding("key.mouse.5", InputConstants.MOUSE_BUTTON_5, SDLMouse.SDL_BUTTON_X2MASK);
    }

    @Test
    void dragBridgeUsesVanillaEventToContainerButtonConversion() throws Exception {
        Class<?>[] parameters = {Slot.class, int.class, MouseButtonEvent.class, ContainerInput.class};
        assertNotNull(AbstractContainerScreen.class.getDeclaredMethod("slotClicked", parameters));
        assertNotNull(HandledScreenInvoker.class.getMethod("QS$onMouseClick", parameters));
    }

    private static void assertBinding(String name, int button, int mask) {
        var binding = CustomKeyBinding.configDefault("quickshulker", name);
        assertEquals(true, binding.matches(button, InputConstants.Type.MOUSE));
        assertEquals(mask, CustomKeyBinding.mouseButtonMask(binding.parsedKey.getValue()));
    }
}
