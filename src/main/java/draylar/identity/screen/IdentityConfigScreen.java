package draylar.identity.screen;

import draylar.identity.config.IdentityConfig;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Checkbox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class IdentityConfigScreen extends Screen {

    private final Screen parent;
    private Checkbox enableSwapsBox;
    private Checkbox overlayUnlocksBox;
    private Checkbox equipItemsBox;
    private Checkbox enableFlightBox;
    private Checkbox revokeOnDeathBox;
    private Checkbox showNametagBox;

    public IdentityConfigScreen(Screen parent) {
        super(Component.translatable("identity.config.title"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        super.init();
        IdentityConfig cfg = IdentityConfig.getInstance();
        int cx = this.width / 2 - 100;
        int cy = 40;

        enableSwapsBox = Checkbox.builder(Component.translatable("identity.config.enable_swaps"), font)
                .pos(cx, cy).selected(cfg.enableSwaps()).build();
        addRenderableWidget(enableSwapsBox);

        overlayUnlocksBox = Checkbox.builder(Component.translatable("identity.config.overlay_unlocks"), font)
                .pos(cx, cy += 25).selected(cfg.shouldOverlayIdentityUnlocks()).build();
        addRenderableWidget(overlayUnlocksBox);

        equipItemsBox = Checkbox.builder(Component.translatable("identity.config.equip_items"), font)
                .pos(cx, cy += 25).selected(cfg.identitiesEquipItems()).build();
        addRenderableWidget(equipItemsBox);

        enableFlightBox = Checkbox.builder(Component.translatable("identity.config.enable_flight"), font)
                .pos(cx, cy += 25).selected(cfg.enableFlight()).build();
        addRenderableWidget(enableFlightBox);

        revokeOnDeathBox = Checkbox.builder(Component.translatable("identity.config.revoke_on_death"), font)
                .pos(cx, cy += 25).selected(cfg.revokeIdentityOnDeath()).build();
        addRenderableWidget(revokeOnDeathBox);

        showNametagBox = Checkbox.builder(Component.translatable("identity.config.show_nametag"), font)
                .pos(cx, cy += 25).selected(cfg.showPlayerNametag()).build();
        addRenderableWidget(showNametagBox);

        addRenderableWidget(Button.builder(Component.translatable("gui.done"), btn -> saveAndClose())
                .bounds(this.width / 2 - 50, this.height - 30, 100, 20).build());
    }

    private void saveAndClose() {
        IdentityConfig cfg = IdentityConfig.getInstance();
        cfg.setEnableSwaps(enableSwapsBox.selected());
        cfg.setOverlayIdentityUnlocks(overlayUnlocksBox.selected());
        cfg.setIdentitiesEquipItems(equipItemsBox.selected());
        cfg.setEnableFlight(enableFlightBox.selected());
        cfg.setRevokeIdentityOnDeath(revokeOnDeathBox.selected());
        cfg.setShowPlayerNametag(showNametagBox.selected());
        IdentityConfig.save();
        this.minecraft.setScreen(parent);
    }

    @Override
    public void onClose() {
        this.minecraft.setScreen(parent);
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor extractor, int mouseX, int mouseY, float delta) {
        extractor.fill(0, 0, this.width, this.height, 0x80000000);
        extractor.text(font, title, this.width / 2 - font.width(title) / 2, 15, 0xFFFFFF, true);
        super.extractRenderState(extractor, mouseX, mouseY, delta);
    }
}
