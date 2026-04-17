package draylar.identity.mixin;

import draylar.identity.api.PlayerHostility;
import draylar.identity.api.PlayerIdentity;
import draylar.identity.config.IdentityConfig;
import draylar.identity.registry.IdentityEntityTags;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.piglin.PiglinAi;
import net.minecraft.world.entity.monster.piglin.Piglin;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PiglinAi.class)
public class PiglinBrainMixin {

    @Inject(
            method = "isNearestValidAttackTarget",
            at = @At("RETURN"),
            cancellable = true
    )
    private static void shouldAttackIdentity(ServerLevel serverLevel, Piglin piglin, LivingEntity target, CallbackInfoReturnable<Boolean> cir) {
        boolean shouldAttack = cir.getReturnValue();

        if(shouldAttack && target instanceof Player player) {
            LivingEntity identity = PlayerIdentity.getIdentity(player);
            boolean hasHostility = PlayerHostility.hasHostility(player);

            if(identity != null) {
                // Piglins should not attack Piglins or Piglin Brutes, unless they have hostility
                if (identity.getType().builtInRegistryHolder().is(IdentityEntityTags.PIGLIN_FRIENDLY)) {
                    cir.setReturnValue(false);
                }

                // Player has an Identity but is not a piglin, check config for what to do
                else {
                    if (IdentityConfig.getInstance().hostilesIgnoreHostileIdentityPlayer() && identity instanceof Monster) {

                        // Check hostility for aggro on non-piglin hostiles
                        if(!hasHostility) {
                            cir.setReturnValue(false);
                        } else {
                            cir.setReturnValue(true);
                        }
                    }
                }
            }
        }
    }
}
