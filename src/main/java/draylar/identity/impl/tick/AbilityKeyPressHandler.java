package draylar.identity.impl.tick;

import draylar.identity.IdentityClient;
import draylar.identity.ability.AbilityRegistry;
import draylar.identity.api.PlayerIdentity;
import draylar.identity.network.ClientNetworking;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.LivingEntity;

public class AbilityKeyPressHandler implements ClientTickEvents.StartTick {

    @Override
    public void onStartTick(Minecraft client) {
        if(client.player == null) return;

        if(IdentityClient.ABILITY_KEY.consumeClick()) {
            // TODO: maybe the check should be on the server to allow for ability extension mods?
            // Only send the ability packet if the identity equipped by the player has one
            LivingEntity identity = PlayerIdentity.getIdentity(client.player);

            if(identity != null) {
                if(AbilityRegistry.has(identity.getType())) {
                    ClientNetworking.sendAbilityRequest();
                }
            }
        }
    }
}
