package draylar.identity.screen;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.network.chat.Component;

public class IdentityHelpScreen extends Screen {

    public IdentityHelpScreen() {
        super(Component.literal(""));
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor extractor, int mouseX, int mouseY, float delta) {
        // extractBackground triggers blur which can only be called once per frame; use a dark overlay instead.
        extractor.fill(0, 0, this.width, this.height, 0x80000000);

        extractor.pose().pushMatrix();
        extractor.pose().scale(0.75f, 0.75f);
        extractor.text(font, Component.translatable("identity.help.welcome"), 15, 15, 0xffffff, true);
        extractor.text(font, Component.translatable("identity.help.credits"), 15, 30, 0xffffff, true);

        extractor.text(font, Component.translatable("identity.help.support_label").withStyle(ChatFormatting.BOLD), 15, 60, 0xffffff, true);
        extractor.text(font, Component.translatable("identity.help.support_description"), 15, 75, 0xffffff, true);

        extractor.text(font, Component.translatable("identity.help.ability_label").withStyle(ChatFormatting.BOLD), 15, 100, 0xffffff, true);
        extractor.text(font, Component.translatable("identity.help.ability_description_1"), 15, 115, 0xffffff, true);
        extractor.text(font, Component.translatable("identity.help.ability_description_2"), 15, 130, 0xffffff, true);
        extractor.text(font, Component.translatable("identity.help.ability_description_3"), 15, 145, 0xffffff, true);

        extractor.text(font, Component.translatable("identity.help.config_label").withStyle(ChatFormatting.BOLD), 15, 175, 0xffffff, true);
        extractor.text(font, Component.translatable("identity.help.config_description"), 15, 190, 0xffffff, true);

        extractor.text(font, Component.translatable("identity.help.credits_label").withStyle(ChatFormatting.BOLD), 15, 220, 0xffffff, true);
        extractor.text(font, Component.translatable("identity.help.credits_general"), 15, 235, 0xffffff, true);
        extractor.text(font, Component.translatable("identity.help.credits_translators"), 15, 250, 0xffffff, true);

        extractor.text(font, Component.translatable("identity.help.return").withStyle(ChatFormatting.ITALIC), 15, height + 60, 0xffffff, true);

        extractor.pose().popMatrix();

        super.extractRenderState(extractor, mouseX, mouseY, delta);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public boolean keyPressed(KeyEvent keyEvent) {
        Minecraft.getInstance().setScreen(new IdentityScreen());
        return super.keyPressed(keyEvent);
    }
}
