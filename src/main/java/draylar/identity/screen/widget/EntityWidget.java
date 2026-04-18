package draylar.identity.screen.widget;

import draylar.identity.Identity;
import draylar.identity.api.variant.IdentityType;
import draylar.identity.network.impl.FavoritePackets;
import draylar.identity.network.impl.SwapPackets;
import draylar.identity.screen.IdentityScreen;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;

public class EntityWidget<T extends LivingEntity> extends AbstractWidget {

    /** Vertical offset (in pixels) applied to entity rendering in the identity grid. Adjustable via /identity debug offset. */
    public static int VERTICAL_OFFSET = 0;

    private final IdentityType<T> type;
    private final T entity;
    private final int size;
    private boolean selected;
    private boolean starred;
    private final IdentityScreen parent;

    public EntityWidget(float x, float y, float width, float height, IdentityType<T> type, T entity, IdentityScreen parent, boolean starred, boolean current) {
        super((int) x, (int) y, (int) width, (int) height, Component.empty());
        this.type = type;
        this.entity = entity;
        size = (int) (25 * (1 / (Math.max(entity.getBbHeight(), entity.getBbWidth()))));
        entity.setGlowingTag(true);
        this.parent = parent;
        this.starred = starred;
        this.selected = current;
        setTooltip(Tooltip.create(type.createTooltipText(entity)));
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean forwarded) {
        double mouseX = event.x();
        double mouseY = event.y();
        int button = event.button();
        boolean bl = mouseX >= (double) this.getX() && mouseX < (double) (this.getX() + this.width) && mouseY >= (double) this.getY() && mouseY < (double) (this.getY() + this.height);

        if (bl) {
            // Update current Identity
            if (button == 0) {
                SwapPackets.sendSwapRequest(type);
                parent.disableAll();
                selected = true;
            }

            // Add to favorites
            else if (button == 1) {
                starred = !starred;
                FavoritePackets.sendFavoriteRequest(type, starred);
            }
        }

        return super.mouseClicked(event, forwarded);
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput builder) {
    }

    @Override
    protected void extractWidgetRenderState(GuiGraphicsExtractor extractor, int mouseX, int mouseY, float delta) {
        // Some entities (namely Aether mobs) crash when rendered in a GUI.
        // Unsure as to the cause, but this try/catch should prevent the game from entirely dipping out.
        try {
            InventoryScreen.extractEntityInInventoryFollowsMouse(
                    extractor,
                    getX(), getY(),
                    getX() + this.getWidth(), (int) (getY() + this.getHeight() * .75f),
                    size, 0.0625f, -10, -10, entity);
        } catch (Exception ignored) {
        }

        // Render selected outline
        if (selected) {
            extractor.blit(RenderPipelines.GUI_TEXTURED, Identity.id("textures/gui/selected.png"), getX(), getY(), 0, 0, getWidth(), getHeight(), 48, 32);
        }

        // Render favorite star
        if (starred) {
            extractor.blit(RenderPipelines.GUI_TEXTURED, Identity.id("textures/gui/star.png"), getX(), getY(), 0, 0, 15, 15, 15, 15);
        }
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }
}
