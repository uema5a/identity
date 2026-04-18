package draylar.identity.registry;

import draylar.identity.screen.IdentityConfigScreen;
import net.minecraft.client.gui.screens.Screen;

public class ModScreens {

    public static Screen getConfigScreen(Screen parent) {
        return new IdentityConfigScreen(parent);
    }

    private ModScreens() {
        // NO-OP
    }
}
