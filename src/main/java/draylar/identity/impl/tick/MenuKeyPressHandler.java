package draylar.identity.impl.tick;

import draylar.identity.IdentityClient;
import draylar.identity.config.IdentityConfig;
import draylar.identity.screen.IdentityScreen;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.server.permissions.Permissions;

public class MenuKeyPressHandler implements ClientTickEvents.StartTick {

    @Override
    public void onStartTick(Minecraft client) {
        if(client.player == null) return;

        if(IdentityClient.MENU_KEY.consumeClick()) {
            if(IdentityConfig.getInstance().enableClientSwapMenu() ||
                client.player.permissions().hasPermission(Permissions.COMMANDS_GAMEMASTER) ||
                IdentityConfig.getInstance().allowedSwappers().stream()
                    .anyMatch(p -> client.player.getProfile().name().map(p::equalsIgnoreCase).orElse(false))) {
                Minecraft.getInstance().setScreen(new IdentityScreen());
            }
        }
    }
}
