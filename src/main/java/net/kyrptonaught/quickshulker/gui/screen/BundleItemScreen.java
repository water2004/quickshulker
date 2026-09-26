package net.kyrptonaught.quickshulker.gui.screen;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;

@Environment(EnvType.CLIENT)
public class BundleItemScreen extends AbstractContainerScreen<BundleItemMenu> {
    private static final ResourceLocation SCROLLER_TEXTURE = ResourceLocation.withDefaultNamespace("container/creative_inventory/scroller");
    private static final ResourceLocation SCROLLER_DISABLED_TEXTURE = ResourceLocation.withDefaultNamespace("container/creative_inventory/scroller_disabled");
    private static final ResourceLocation CONTAINER_TEXTURE = ResourceLocation.withDefaultNamespace("textures/gui/container/generic_54.png");
    private static final ResourceLocation SCROLLBAR_BACKGROUND_TEXTURE = ResourceLocation.withDefaultNamespace("textures/gui/container/creative_inventory/tab_items.png");
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
        context.blit(CONTAINER_TEXTURE, this.leftPos, this.topPos, 0.0F, 0.0F, this.imageWidth, ROWS_COUNT * 18 + 17, 256, 256);
        context.blit(CONTAINER_TEXTURE, this.leftPos, this.topPos + ROWS_COUNT * 18 + 17, 0.0F, 126.0F, this.imageWidth, 96, 256, 256);
        this.drawScrollbarBackground(context);
        int i = this.leftPos + 156;
        int j = this.topPos + 18;
        int k = j + 88;
        context.blitSprite(SCROLLER_TEXTURE, i, j + (int) ((k - j - SCROLLBAR_HEIGHT) * this.scrollPosition), SCROLLBAR_WIDTH, SCROLLBAR_HEIGHT);
    }



    public void drawScrollbarBackground(GuiGraphics context){
        int i = this.leftPos + 8 + COLUMNS_COUNT * 18 - 1;
        int j = this.topPos + 18 - 1;
        context.blit(SCROLLBAR_BACKGROUND_TEXTURE, i, j, 170, 17, 18, 72, 256, 256);
        context.blit(SCROLLBAR_BACKGROUND_TEXTURE, i, j + 72, 170, 111, 18, 18, 256, 256);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if(button == 0){
            if(this.isClickInScrollbar(mouseX, mouseY)){
                return this.scrolling = true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    protected boolean isClickInScrollbar(double mouseX, double mouseY) {
        int k = this.leftPos + 156;
        int l = this.topPos + 18;
        int m = k + 12;
        int n = l + 88;
        return mouseX >= k && mouseY >= l && mouseX < m && mouseY < n;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if(button == 0){
            this.scrolling = false;
        }

        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        this.scrollPosition = this.menu.getScrollPosition(this.scrollPosition, verticalAmount);
        this.menu.scrollItems(this.scrollPosition);
        return true;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double offsetX, double offsetY) {
        if(this.scrolling){
            int i = this.topPos + 18;
            int j = i + 88;
            this.scrollPosition = ((float) mouseY - i - 7.5F) / (j - i - 15.0F);
            this.scrollPosition = Mth.clamp(this.scrollPosition, 0.0F, 1.0F);
            this.menu.scrollItems(this.scrollPosition);
            return true;
        }else {
            return super.mouseDragged(mouseX, mouseY, button, offsetX, offsetY);
        }
    }
}
