package net.kyrptonaught.kyrptconfig.config.screen.items;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

public class SubItem<E> extends ConfigItem<E> {
    protected boolean expanded = false;
    protected int subStart = 0;
    protected List<ConfigItem<?>> configs = new ArrayList<>();

    public SubItem(Component name, boolean isExpanded) {
        super(name, null, null);
        this.expanded = isExpanded;
    }

    public SubItem(Component name) {
        this(name, false);
    }

    public boolean requiresRestart() {
        for (ConfigItem<?> item : configs) {
            if (item.requiresRestart())
                return true;
        }
        return super.requiresRestart();
    }

    public void save() {
        for (ConfigItem<?> item : configs)
            item.save();
        super.save();
    }

    public boolean isValueDefault() {
        for (ConfigItem<?> item : configs) {
            if (!item.isValueDefault())
                return false;
        }
        return true;
    }

    public void tick() {
        for (ConfigItem<?> item : configs) {
            if (item.isHidden()) continue;
            item.tick();
        }
    }

    @Override
    public void mouseClicked(net.minecraft.client.input.MouseButtonEvent click, boolean doubleClick) {
        double mouseX = click.x(), mouseY = click.y(); int button = click.button();
        super.mouseClicked(click, doubleClick);
        if (!isHidden() && mouseY > subStart && mouseY < subStart + 20)
            expanded = !expanded;

        if (expanded && !isHidden()) {
            for (ConfigItem<?> item : configs) {
                if (item.isHidden()) continue;
                item.mouseClicked(click, doubleClick);
            }
        }
    }

    @Override
    public boolean charTyped(net.minecraft.client.input.CharacterEvent characterEvent) {
        char codePoint = (char) characterEvent.codepoint(); int modifiers = characterEvent.modifiers();
        if (expanded && !isHidden()) {
            for (ConfigItem<?> item : configs) {
                if (item.isHidden()) continue;
                if (item.charTyped(characterEvent))
                    return true;
            }
        }
        return false;
    }

    @Override
    public boolean keyPressed(net.minecraft.client.input.KeyEvent keyEvent) {
        int keyCode = keyEvent.key(), scanCode = keyEvent.scancode(), modifiers = keyEvent.modifiers();
        if (expanded && !isHidden()) {
            for (ConfigItem<?> item : configs) {
                if (item.isHidden()) continue;
                if (item.keyPressed(keyEvent))
                    return true;
            }
        }
        return false;
    }

    public int getContentSize() {
        if (expanded && !isHidden()) {
            int size = 0;
            for (ConfigItem<?> item : configs) {
                if (item.isHidden()) continue;
                size += item.getSize() + 3;
            }
            return size;
        }
        return 0;
    }

    public void clearConfigItems() {
        configs.clear();
    }

    public ConfigItem<?> addConfigItem(ConfigItem<?> item) {
        this.configs.add(item);
        return item;
    }

    @Override
    public void render(GuiGraphics context, int x, int y, int mouseX, int mouseY, float delta) {
        super.render(context, x, y, mouseX, mouseY, delta);
        if (isHidden()) return;
        context.drawString(Minecraft.getInstance().font, expanded ? "-" : "+", x - 10, y + 5, -1, false);
        subStart = y;
        if (expanded) {
            int runningY = subStart + 23;
            for (ConfigItem<?> item : configs) {
                if (item.isHidden()) continue;
                item.render(context, 30, runningY, mouseX, mouseY, delta);
                runningY += item.getSize() + 3;
            }
        }
    }

    @Override
    public void render2(GuiGraphics context, int x, int y, int mouseX, int mouseY, float delta) {
        super.render2(context, x, y, mouseX, mouseY, delta);
        if (isHidden()) return;

        if (expanded) {
            int runningY = y + 23;
            for (ConfigItem<?> item : configs) {
                if (item.isHidden()) continue;
                item.render2(context, 30, runningY, mouseX, mouseY, delta);
                runningY += item.getSize() + 3;
            }
        }
    }
}
