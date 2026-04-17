package draylar.identity.mixin;

import draylar.identity.api.PlayerHostility;
import draylar.identity.api.PlayerIdentity;
import draylar.identity.config.IdentityConfig;
// MobType was removed in MC 26.1
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(NearestAttackableTargetGoal.class)
public abstract class ActiveTargetGoalMixin extends TrackTargetGoalMixin {

    @Shadow protected LivingEntity target;

    @Inject(
            method = "start",
            at = @At("HEAD"),
            cancellable = true
    )
    private void ignoreMorphedPlayers(CallbackInfo ci) {
        if (IdentityConfig.getInstance().hostilesIgnoreHostileIdentityPlayer() && this.mob instanceof Monster && this.target instanceof Player) {
            Player player = (Player) this.target;
            LivingEntity identity = PlayerIdentity.getIdentity(player);

            if(identity != null) {
                boolean hasHostility = PlayerHostility.hasHostility(player);

                // only cancel if the player does not have hostility
                if (!hasHostility) {
                    // creepers should ignore cats
                    if (this.mob instanceof Creeper && identity.getType().equals(EntityType.OCELOT)) {
                        this.stop();
                        ci.cancel();
                    }

                    // withers should ignore undead
                    else if (this.mob instanceof WitherBoss && identity.isInvertedHealAndHarm()) {
                        this.stop();
                        ci.cancel();
                    }

                    // hostile mobs (besides wither) should not target players morphed as hostile mobs
                    else if (!(this.mob instanceof WitherBoss) && identity instanceof Monster) {
                        this.stop();
                        ci.cancel();
                    }
                }
            }
        }
    }

    @Override
    protected void identity_shouldContinue(CallbackInfoReturnable<Boolean> cir) {
        // check cancelling for hostiles
        if(IdentityConfig.getInstance().hostilesIgnoreHostileIdentityPlayer() && IdentityConfig.getInstance().hostilesForgetNewHostileIdentityPlayer() && this.mob instanceof Monster && this.target instanceof Player player) {
            LivingEntity identity = PlayerIdentity.getIdentity(player);

            if (identity != null) {
                boolean hasHostility = PlayerHostility.hasHostility(player);

                // only cancel if the player does not have hostility
                if (!hasHostility) {
                    // creepers should ignore cats
                    if (this.mob instanceof Creeper && identity.getType().equals(EntityType.OCELOT)) {
                        cir.setReturnValue(false);
                    }

                    // withers should ignore undead
                    else if (this.mob instanceof WitherBoss && identity.isInvertedHealAndHarm()) {
                        cir.setReturnValue(false);
                    }

                    // hostile mobs (besides wither) should not target players morphed as hostile mobs
                    else if (!(this.mob instanceof WitherBoss) && identity instanceof Monster) {
                        cir.setReturnValue(false);
                    }
                }
            }
        }
    }
}
