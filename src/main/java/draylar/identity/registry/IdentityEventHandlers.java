package draylar.identity.registry;

import draylar.identity.api.PlayerHostility;
import draylar.identity.api.PlayerIdentity;
import draylar.identity.api.SafeTagManager;
import draylar.identity.config.IdentityConfig;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.Ravager;

public class IdentityEventHandlers {

    public static void initialize() {
        IdentityEventHandlers.registerHostilityUpdateHandler();
        IdentityEventHandlers.registerRavagerRidingHandler();
    }

    public static void registerHostilityUpdateHandler() {
        UseEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
            if (!world.isClientSide() && entity instanceof Monster) {
                PlayerHostility.set(player, IdentityConfig.getInstance().hostilityTime());
            }
            return InteractionResult.PASS;
        });
    }

    // Players with an equipped Identity inside the `ravager_riding` entity tag should
    //   be able to ride Ravagers.
    public static void registerRavagerRidingHandler() {
        UseEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
            if (entity instanceof Ravager) {
                LivingEntity identity = PlayerIdentity.getIdentity(player);
                if (identity != null) {
                    if (identity.getType().builtInRegistryHolder().is(IdentityEntityTags.RAVAGER_RIDING)
                            || SafeTagManager.isCustomRavagerRiding(identity.getType())) {
                        player.startRiding(entity);
                    }
                }
            }
            return InteractionResult.PASS;
        });
    }
}
