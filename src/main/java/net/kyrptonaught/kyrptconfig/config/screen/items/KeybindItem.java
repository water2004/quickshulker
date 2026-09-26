package net.kyrptonaught.kyrptconfig.config.screen.items;

import com.mojang.blaze3d.platform.InputConstants;
import net.kyrptonaught.kyrptconfig.api.ConflictHandler;
import net.kyrptonaught.kyrptconfig.config.screen.NotSuckyButton;
import net.kyrptonaught.quickshulker.event.KeyBindingRegister;
import net.minecraft.ChatFormatting;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

public class KeybindItem extends ConfigItem<String> {
    private final NotSuckyButton keyButton;
    private Boolean isListening = false;
    private boolean duplicate = false;

    public KeybindItem(Component name, String key, String defaultKey) {
        super(name, key, defaultKey);
        this.keyButton = new NotSuckyButton(0, 0, 100, 20, getCleanName(key), widget -> {
            this.isListening = !this.isListening;
            updateMessage();
        });
        useDefaultResetBTN();
        ConflictHandler.updateMap(this);
    }

    public void setValue(String value) {
        super.setValue(value);
        isListening = false;
        ConflictHandler.updateCustomConflicts();
    }

    public MutableComponent getCleanName(String str) {
        if (Language.getInstance().has(value))
            return Component.translatable(str);
        if (str == null || str.isBlank() || str.isEmpty())
            return Component.translatable("key.keyboard.unknown");
        return Component.literal(str.substring(str.length() - 1).toUpperCase());
    }

    public void updateMessage(){
        if(!isListening){
            duplicate = false;
            MutableComponent mutableText = Component.empty();
            if(isInvalidKeyValue()) {
                for (KeyMapping keyBinding : Minecraft.getInstance().options.keyMappings) {
                    if(KeyBindingRegister.MAIN.equals(keyBinding.getCategory())){
                        continue;
                    }
                    Component t1 = Component.translatable(keyBinding.getName());
                    Component t2 = this.getTitleText();
                    if (!t1.equals(t2) && keyBinding.saveString().equals(this.value)) {
                        duplicate = true;
                        mutableText.append("\n  - ").append(Component.translatable(keyBinding.getName()));
                    }
                }
                for (KeybindItem item : ConflictHandler.CUSTOM_KEYBIND_ITEMS) {
                    if (item != this && this.value.equals(item.value)) {
                        duplicate = true;
                        mutableText.append("\n  - ").append(item.getTitleText());
                    }
                }
            }
            if(duplicate){
                keyButton.setMessage(Component.literal("[ ").append(getCleanName(this.value).withStyle(ChatFormatting.WHITE)).append(Component.literal(" ]")).withStyle(ChatFormatting.YELLOW));
                keyButton.setTooltip(Tooltip.create(Component.translatable("key.quickshulker.config.savedValue", Component.literal(this.value)).append(Component.translatable("key.quickshulker.config.keybindinsConflict", mutableText))));
            }else{
                keyButton.setMessage(this.getCleanName(this.value));
                keyButton.setTooltip(Tooltip.create(Component.translatable("key.quickshulker.config.savedValue", Component.literal(this.value))));
            }
        }else{
            keyButton.setMessage(Component.literal("> ").append(getCleanName(this.value)).append(Component.literal(" <")));
        }
    }

    private boolean isInvalidKeyValue(){
        return this.value != null && !this.value.isEmpty() && !this.value.isBlank() && !this.value.equals("key.keyboard.unknown");
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (isListening) {
            if (keyCode == InputConstants.KEY_ESCAPE) {
                setValue("");
                return true;
            }
            setValue(InputConstants.getKey(keyCode, scanCode).getName());
            return true;
        }
        return false;
    }

    @Override
    public void mouseClicked(double mouseX, double mouseY, int button) {
        super.mouseClicked(mouseX, mouseY, button);
        boolean handled;
        handled = (keyButton.mouseClicked(mouseX, mouseY, button) || resetButton.mouseClicked(mouseX, mouseY, button));
        if (isListening && !handled) {
            setValue(InputConstants.Type.MOUSE.getOrCreate(button).getName());
        }
    }

    @Override
    public void render(GuiGraphics context, int x, int y, int mouseX, int mouseY, float delta) {
        super.render(context, x, y, mouseX, mouseY, delta);
        this.keyButton.setY(y);

        this.keyButton.setX(resetButton.getX() - resetButton.getWidth() - (keyButton.getWidth() / 2) - 20);

        keyButton.render(context, mouseX, mouseY, delta);

        if(duplicate){
            int m = keyButton.getX() - 6;
            context.fill(m, y, m + 3, keyButton.getBottom(), -256);
        }
    }
}
