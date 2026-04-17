package draylar.identity.api;

import draylar.identity.config.IdentityConfig;
import draylar.identity.api.variant.IdentityType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;

public class IdentityGranting {

    public static void grantByAttack(Player player, IdentityType<?> granted) {
        if(player instanceof ServerPlayer serverPlayerEntity) {
            int amountKilled = serverPlayerEntity.getStats().getValue(net.minecraft.stats.Stats.ENTITY_KILLED.get(granted.getEntityType()));

            // If the player has to kill a certain number of mobs before unlocking an Identity, check their statistic for the specific type.
            if(IdentityConfig.getInstance().requiresKillsForIdentity()) {
                String id = BuiltInRegistries.ENTITY_TYPE.getKey(granted.getEntityType()).toString();

                // Check against a specific count requirement or the default count.
                int required = IdentityConfig.getInstance().getRequiredKillsForIdentity();
                if(IdentityConfig.getInstance().getRequiredKillsByType() != null && IdentityConfig.getInstance().getRequiredKillsByType().containsKey(id)) {
                    required = IdentityConfig.getInstance().getRequiredKillsByType().get(id);
                }

                // If the amount currently killed is less than the required amount, do not allow the player to unlock.
                if(amountKilled < required) {
                    return;
                }
            }

            boolean isNew = false;
            boolean hadPreviously = PlayerUnlocks.has(serverPlayerEntity, granted);
            boolean result = PlayerUnlocks.unlock(serverPlayerEntity, granted);

            // ensure type has not already been unlocked
            if(result && !hadPreviously) {

                // send unlock message to player if they aren't in creative and the config option is on
                if(IdentityConfig.getInstance().shouldOverlayIdentityUnlocks() && !player.isCreative()) {
                    player.sendOverlayMessage(
                            Component.translatable(
                                    "identity.unlock_entity",
                                    Component.translatable(granted.getEntityType().getDescriptionId())
                            )
                    );
                }

                isNew = true;
            }

            // force-morph player into new type
            Entity instanced = granted.create(player.level());
            if(instanced instanceof LivingEntity) {
                if(IdentityConfig.getInstance().forceChangeNew() && isNew) {
                    PlayerIdentity.updateIdentity(serverPlayerEntity, granted, (LivingEntity) instanced);
                } else if(IdentityConfig.getInstance().forceChangeAlways()) {
                    PlayerIdentity.updateIdentity(serverPlayerEntity, granted, (LivingEntity) instanced);
                }
            }
        }
    }
}
