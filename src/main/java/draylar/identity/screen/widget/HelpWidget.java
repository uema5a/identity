package draylar.identity.screen.widget;

import draylar.identity.screen.IdentityHelpScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;

public class HelpWidget extends Button.Plain {

    public HelpWidget(int x, int y, int width, int height) {
        super(x, y, width, height, Component.literal("?"), (widget) -> {
            Minecraft.getInstance().setScreen(new IdentityHelpScreen());
        }, DEFAULT_NARRATION);

        setTooltip(Tooltip.create(Component.translatable("identity.help")));
    }
}
