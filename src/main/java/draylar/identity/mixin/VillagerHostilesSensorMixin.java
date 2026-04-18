package draylar.identity.mixin;

import com.google.common.collect.ImmutableMap;
import draylar.identity.api.PlayerIdentity;
import draylar.identity.config.IdentityConfig;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.sensing.VillagerHostilesSensor;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(VillagerHostilesSensor.class)
public class VillagerHostilesSensorMixin {

    @Shadow @Final private static ImmutableMap<EntityType<?>, Float> ACCEPTABLE_DISTANCE_FROM_HOSTILES;

    @Inject(
            method = "isHostile",
            at = @At("HEAD"),
            cancellable = true
    )
    private void checkHostileIdentity(LivingEntity entity, CallbackInfoReturnable<Boolean> cir) {
        if(entity instanceof Player player) {
            // check if we should be performing this from config
            if(IdentityConfig.getInstance().villagersRunFromIdentities()) {
                LivingEntity identity = PlayerIdentity.getIdentity(player);

                // check if identity is valid & if it is a type villagers run from
                if (identity != null && ACCEPTABLE_DISTANCE_FROM_HOSTILES.containsKey(identity.getType())) {
                    cir.setReturnValue(true);
                }
            }
        }
    }

    @Inject(
            method = "isClose",
            at = @At("HEAD"),
            cancellable = true
    )
    private void checkPlayerDanger(LivingEntity villager, LivingEntity potentialPlayer, CallbackInfoReturnable<Boolean> cir) {
        // should only be called if the above mixin passes, so we can assume the config option is true
        if(potentialPlayer instanceof Player player) {
            LivingEntity identity = PlayerIdentity.getIdentity(player);

            // check if identity is valid & if it is a type villagers run from
            if (identity != null && ACCEPTABLE_DISTANCE_FROM_HOSTILES.containsKey(identity.getType())) {
                float f = ACCEPTABLE_DISTANCE_FROM_HOSTILES.get(identity.getType());
                cir.setReturnValue(potentialPlayer.distanceToSqr(villager) <= (double) (f * f));
            } else {
                cir.setReturnValue(false);
            }
        }
    }
}
