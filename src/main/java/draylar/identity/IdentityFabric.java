package draylar.identity;

import draylar.identity.config.IdentityConfig;
import net.fabricmc.api.ModInitializer;

public class IdentityFabric implements ModInitializer {

    @Override
    public void onInitialize() {
        IdentityConfig.load();
        new Identity().initialize();
    }
}
