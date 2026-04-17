package draylar.identity.impl.tick;

import draylar.identity.IdentityClient;
import draylar.identity.config.IdentityConfig;
import draylar.identity.screen.IdentityScreen;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.Minecraft;

public class MenuKeyPressHandler implements ClientTickEvents.StartTick {

    @Override
    public void onStartTick(Minecraft client) {
        if(client.player == null) return;

        if(IdentityClient.MENU_KEY.consumeClick()) {
            if(IdentityConfig.getInstance().enableClientSwapMenu() ||
                client.player.hasPermissions(3) ||
                IdentityConfig.getInstance().allowedSwappers().stream()
                    .anyMatch(p -> p.equalsIgnoreCase(client.player.getGameProfile().getName()))) {
                Minecraft.getInstance().setScreen(new IdentityScreen());
            }
        }
    }
}
