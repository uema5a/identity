package draylar.identity.compat;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import draylar.identity.screen.IdentityConfigScreen;

public class IdentityModMenuIntegration implements ModMenuApi {

    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return IdentityConfigScreen::new;
    }
}
