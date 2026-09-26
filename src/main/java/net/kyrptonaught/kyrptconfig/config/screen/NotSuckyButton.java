package net.kyrptonaught.kyrptconfig.config.screen;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;

public class NotSuckyButton extends Button {
    int buttonColor = -1;
    public boolean disableHover = false;
    private static final WidgetSprites TEXTURES = new WidgetSprites(ResourceLocation.parse("widget/button"), ResourceLocation.parse("widget/button_disabled"), ResourceLocation.parse("widget/button_highlighted"));

    public NotSuckyButton(int x, int y, int width, int height, net.minecraft.network.chat.Component message, OnPress onPress) {
        super(x, y, width, height, message, onPress, DEFAULT_NARRATION);
    }

    public void setButtonColor(int color) {
        this.setMessage(ComponentUtils.mergeStyles(this.getMessage().copy(), Style.EMPTY.withColor(color)));
        this.buttonColor = color;
    }

    public boolean detectHover(int mouseX, int mouseY) {
        return mouseX >= this.getX() && mouseY >= this.getY() && mouseX < this.getX() + this.width && mouseY < this.getY() + this.height;
    }

    @Override
    protected void renderWidget(GuiGraphics context, int mouseX, int mouseY, float deltaTicks) {
        if (disableHover) isHovered = false;

        super.renderWidget(context, mouseX, mouseY, deltaTicks);
    }
}
