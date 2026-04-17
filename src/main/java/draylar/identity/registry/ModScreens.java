package draylar.identity.registry;

import net.minecraft.client.gui.screens.Screen;
import org.jetbrains.annotations.Nullable;

public class ModScreens {

    // ModMenu integration was dropped during the 26.1 migration.
    // The method is kept as a stub so any lingering references compile;
    // a future Phase C/D pass can either wire up a real config screen or
    // delete this class outright if nothing ends up using it.
    @Nullable
    public static Screen getConfigScreen(Screen parent) {
        return null;
    }

    private ModScreens() {
        // NO-OP
    }
}
