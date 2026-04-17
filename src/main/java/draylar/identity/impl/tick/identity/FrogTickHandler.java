package draylar.identity.impl.tick.identity;

import draylar.identity.api.IdentityTickHandler;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.animal.frog.Frog;
import net.minecraft.world.entity.player.Player;

public class FrogTickHandler implements IdentityTickHandler<Frog> {

    @Override
    public void tick(Player player, Frog frog) {
        if(player.level().isClientSide()) {
            boolean walk = player.onGround() && player.getDeltaMovement().horizontalDistanceSqr() > 1.0E-6 && !player.isInWater();
            boolean swim = player.getDeltaMovement().horizontalDistanceSqr() > 1.0E-6 && player.isInWater();

            // Walking implementation
            if (walk) {
//                frog.limbAnimator.startIfNotRunning(frog.tickCount);
            } else {
//                frog.walkingAnimationState.stop();
            }

            // Jumping
            if(!player.onGround() && !swim && !walk && !player.isInWater()) {
                frog.jumpAnimationState.startIfStopped(frog.tickCount);
            } else {
                frog.jumpAnimationState.stop();
            }

            // Swimming
            if (swim) {
                frog.swimIdleAnimationState.stop();
//                frog.swimmingAnimationState.startIfStopped(frog.tickCount);
            } else if (player.isInWater()) {
//                frog.swimmingAnimationState.stop();
                frog.swimIdleAnimationState.startIfStopped(frog.tickCount);
            } else {
//                frog.swimmingAnimationState.stop();
                frog.swimIdleAnimationState.stop();
            }

            // Random croaking
            if(player.level().getRandom().nextDouble() <= 0.001) {
                frog.croakAnimationState.start(player.tickCount);
            }

            // Tongue
            if(player.swinging) {
                frog.tongueAnimationState.startIfStopped(player.tickCount);
            } else {
                frog.tongueAnimationState.stop();
            }
        } else {
            // Buffs - jump boost
            player.addEffect(new MobEffectInstance(MobEffects.JUMP_BOOST, 20 * 2, 2, true, false));
        }
    }
}
