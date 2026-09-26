package net.kyrptonaught.kyrptconfig.config.screen.items;

import net.kyrptonaught.kyrptconfig.config.screen.NotSuckyButton;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.DyeColor;

public class BooleanItem extends ConfigItem<Boolean> {
    private final NotSuckyButton boolWidget;

    public BooleanItem(Component name, Boolean value, Boolean defaultValue) {
        super(name, value, defaultValue);
        this.boolWidget = new NotSuckyButton(0, 0, 100, 20, Component.literal("BoolButton"), widget -> {
            setValue(!this.value);
        });
        setValue(value);
        useDefaultResetBTN();
    }

    @Override
    public void setValue(Boolean value) {
        super.setValue(value);
        if (value) {
            boolWidget.setMessage(Component.translatable("key.kyrptconfig.config.true"));
            boolWidget.setButtonColor(DyeColor.LIME.getFireworkColor());
        } else {
            boolWidget.setMessage(Component.translatable("key.kyrptconfig.config.false"));
            boolWidget.setButtonColor(DyeColor.RED.getTextColor());
        }
    }

    @Override
    public void mouseClicked(double mouseX, double mouseY, int button) {
        super.mouseClicked(mouseX, mouseY, button);
        boolWidget.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public void render(GuiGraphics context, int x, int y, int mouseX, int mouseY, float delta) {
        super.render(context, x, y, mouseX, mouseY, delta);
        this.boolWidget.setY(y);
        this.boolWidget.setX(resetButton.getX() - resetButton.getWidth() - (boolWidget.getWidth() / 2) - 20);

        boolWidget.render(context, mouseX, mouseY, delta);
    }
}
