package draylar.identity.screen.widget;

import draylar.identity.Identity;
import draylar.identity.network.impl.SwapPackets;
import draylar.identity.screen.IdentityScreen;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;

public class PlayerWidget extends AbstractWidget {

    private final IdentityScreen parent;

    public PlayerWidget(float x, float y, float width, float height, IdentityScreen parent) {
        super((int) x, (int) y, (int) width, (int) height, Component.empty());
        this.parent = parent;
    }

    @Override
    protected void extractWidgetRenderState(GuiGraphicsExtractor extractor, int mouseX, int mouseY, float delta) {
        extractor.blit(RenderPipelines.GUI_TEXTURED, Identity.id("textures/gui/player.png"), getX(), getY(), 0, 0, 16, 16, 8, 8);
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput builder) {
    }

    @Override
    public void onClick(MouseButtonEvent event, boolean forwarded) {
        SwapPackets.sendSwapRequest(null);
        parent.disableAll();
    }
}
