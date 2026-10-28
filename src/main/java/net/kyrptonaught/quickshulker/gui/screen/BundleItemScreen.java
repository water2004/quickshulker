package net.kyrptonaught.quickshulker.gui.screen;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;

@Environment(EnvType.CLIENT)
public class BundleItemScreen extends AbstractContainerScreen<BundleItemMenu> {
    private static final Identifier SCROLLER_TEXTURE = Identifier.withDefaultNamespace("container/creative_inventory/scroller");
    private static final Identifier SCROLLER_DISABLED_TEXTURE = Identifier.withDefaultNamespace("container/creative_inventory/scroller_disabled");
    private static final Identifier CONTAINER_TEXTURE = Identifier.withDefaultNamespace("textures/gui/container/generic_54.png");
    private static final Identifier SCROLLBAR_BACKGROUND_TEXTURE = Identifier.withDefaultNamespace("textures/gui/container/creative_inventory/tab_items.png");
    private static final int SCROLLBAR_WIDTH = 12;
    private static final int SCROLLBAR_HEIGHT = 15;
    private float scrollPosition;
    private boolean scrolling;

    private static final int ROWS_COUNT = 5;
    private static final int COLUMNS_COUNT = 8;

    public BundleItemScreen(BundleItemMenu handler, Inventory inventory, Component title) {
        super(handler, inventory, title);
        this.imageWidth = 176;
        this.imageHeight = 202;
        this.scrollPosition = 0.0f;
        this.scrolling = false;
        this.inventoryLabelY = this.imageHeight - 94;
    }

    @Override
    protected void init(){
        super.init();
    }

    @Override
    public void render(GuiGraphics context, int mouseX, int mouseY, float deltaTicks) {
        super.render(context, mouseX, mouseY, deltaTicks);
        this.renderTooltip(context, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics context, float deltaTicks, int mouseX, int mouseY) {
        context.blit(net.minecraft.client.renderer.RenderPipelines.GUI_TEXTURED, CONTAINER_TEXTURE, this.leftPos, this.topPos, 0.0F, 0.0F, this.imageWidth, ROWS_COUNT * 18 + 17, 256, 256);
        context.blit(net.minecraft.client.renderer.RenderPipelines.GUI_TEXTURED, CONTAINER_TEXTURE, this.leftPos, this.topPos + ROWS_COUNT * 18 + 17, 0.0F, 126.0F, this.imageWidth, 96, 256, 256);
        this.drawScrollbarBackground(context);
        int i = this.leftPos + 156;
        int j = this.topPos + 18;
        int k = j + 88;
        context.blitSprite(net.minecraft.client.renderer.RenderPipelines.GUI_TEXTURED, SCROLLER_TEXTURE, i, j + (int) ((k - j - SCROLLBAR_HEIGHT) * this.scrollPosition), SCROLLBAR_WIDTH, SCROLLBAR_HEIGHT);
    }



    public void drawScrollbarBackground(GuiGraphics context){
        int i = this.leftPos + 8 + COLUMNS_COUNT * 18 - 1;
        int j = this.topPos + 18 - 1;
        context.blit(net.minecraft.client.renderer.RenderPipelines.GUI_TEXTURED, SCROLLBAR_BACKGROUND_TEXTURE, i, j, 170, 17, 18, 72, 256, 256);
        context.blit(net.minecraft.client.renderer.RenderPipelines.GUI_TEXTURED, SCROLLBAR_BACKGROUND_TEXTURE, i, j + 72, 170, 111, 18, 18, 256, 256);
    }

    @Override
    public boolean mouseClicked(net.minecraft.client.input.MouseButtonEvent click, boolean doubleClick) {
        double mouseX = click.x(), mouseY = click.y(); int button = click.button();
        if(button == 0){
            if(this.isClickInScrollbar(mouseX, mouseY)){
                return this.scrolling = true;
            }
        }
        return super.mouseClicked(click, doubleClick);
    }

    protected boolean isClickInScrollbar(double mouseX, double mouseY) {
        int k = this.leftPos + 156;
        int l = this.topPos + 18;
        int m = k + 12;
        int n = l + 88;
        return mouseX >= k && mouseY >= l && mouseX < m && mouseY < n;
    }

    @Override
    public boolean mouseReleased(net.minecraft.client.input.MouseButtonEvent click) {
        double mouseX = click.x(), mouseY = click.y(); int button = click.button();
        if(button == 0){
            this.scrolling = false;
        }

        return super.mouseReleased(click);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        this.scrollPosition = this.menu.getScrollPosition(this.scrollPosition, verticalAmount);
        this.menu.scrollItems(this.scrollPosition);
        return true;
    }

    @Override
    public boolean mouseDragged(net.minecraft.client.input.MouseButtonEvent click, double offsetX, double offsetY) {
        double mouseX = click.x(), mouseY = click.y(); int button = click.button();
        if(this.scrolling){
            int i = this.topPos + 18;
            int j = i + 88;
            this.scrollPosition = ((float) mouseY - i - 7.5F) / (j - i - 15.0F);
            this.scrollPosition = Mth.clamp(this.scrollPosition, 0.0F, 1.0F);
            this.menu.scrollItems(this.scrollPosition);
            return true;
        }else {
            return super.mouseDragged(click, offsetX, offsetY);
        }
    }
}
