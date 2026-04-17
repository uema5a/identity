package draylar.identity.mixin;

import draylar.identity.Identity;
import draylar.identity.api.IdentityGranting;
import draylar.identity.api.PlayerIdentity;
import draylar.identity.api.variant.IdentityType;
import draylar.identity.impl.NearbySongAccessor;
import draylar.identity.mixin.accessor.LivingEntityAccessor;
import draylar.identity.registry.IdentityEntityTags;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.world.entity.*;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.monster.spider.Spider;
import net.minecraft.world.entity.animal.fish.WaterAnimal;
import net.minecraft.world.entity.ambient.Bat;
import net.minecraft.world.entity.animal.dolphin.Dolphin;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.tags.FluidTags;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends Entity implements NearbySongAccessor {

    @Shadow
    protected abstract int increaseAirSupply(int air);

    @Shadow
    public abstract boolean hasEffect(Holder<MobEffect> effect);

    protected LivingEntityMixin(EntityType<?> type, Level level) {
        super(type, level);
    }

    // Note: getDefaultDimensions inject is in PlayerEntityMixin (targets Player/Avatar level, not LivingEntity)

    @Inject(
            method = "die",
            at = @At("RETURN")
    )
    private void onDeath(DamageSource source, CallbackInfo ci) {
        Entity attacker = source.getEntity();
        @Nullable IdentityType<?> thisType = IdentityType.from((LivingEntity) (Object) this);

        // check if attacker is a player to grant identity
        if (attacker instanceof Player && thisType != null) {
            IdentityGranting.grantByAttack((Player) attacker, thisType);
        }
    }

    @Redirect(
            method = "baseTick",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;setAirSupply(I)V", ordinal = 3)
    )
    private void cancelAirIncrement(LivingEntity livingEntity, int air) {
        // Aquatic creatures should not regenerate breath on land
        if ((Object) this instanceof Player player) {
            LivingEntity identity = PlayerIdentity.getIdentity(player);

            if (identity != null) {
                if (Identity.isAquatic(identity)) {
                    return;
                }
            }
        }

        this.setAirSupply(this.increaseAirSupply(this.getAirSupply()));
    }

    // Disabled: travel() no longer calls hasEffect() in 26.1
    // @Redirect(
    //         method = "travel",
    //         at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;hasEffect(Lnet/minecraft/core/Holder;)Z", ordinal = 0)
    // )
    // private boolean slowFall(LivingEntity livingEntity, Holder<MobEffect> effect) { ... }

    // Disabled: travel() no longer calls hasEffect() in 26.1
    // @ModifyVariable(method = "travel", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;hasEffect(Lnet/minecraft/core/Holder;)Z", ordinal = 1), ordinal = 0)
    // public float applyWaterCreatureSwimSpeedBoost(float j) { ... }

    @Inject(
            method = "causeFallDamage",
            at = @At(value = "HEAD"),
            cancellable = true
    )
    private void handleFallDamage(double fallDistance, float damageMultiplier, DamageSource damageSource, CallbackInfoReturnable<Boolean> cir) {
        if ((Object) this instanceof Player player) {
            LivingEntity identity = PlayerIdentity.getIdentity(player);

            if (identity != null) {
                boolean takesFallDamage = identity.causeFallDamage(fallDistance, damageMultiplier, damageSource);
                int damageAmount = ((LivingEntityAccessor) identity).callCalculateFallDamage(fallDistance, damageMultiplier);

                if (takesFallDamage && damageAmount > 0) {
                    LivingEntity.Fallsounds fallSounds = identity.getFallSounds();
                    this.playSound(damageAmount > 4 ? fallSounds.big() : fallSounds.small(), 1.0F, 1.0F);
                    ((LivingEntityAccessor) identity).callPlayBlockFallSound();
                    this.hurt(damageSources().fall(), (float) damageAmount);
                    cir.setReturnValue(true);
                } else {
                    cir.setReturnValue(false);
                }
            }
        }
    }

    @Inject(
            method = "hasEffect",
            at = @At("HEAD"),
            cancellable = true
    )
    private void returnHasNightVision(Holder<MobEffect> effect, CallbackInfoReturnable<Boolean> cir) {
        if ((Object) this instanceof Player player) {
            if (effect.equals(MobEffects.NIGHT_VISION)) {
                LivingEntity identity = PlayerIdentity.getIdentity(player);

                // Apply 'Night Vision' status effect to player if they are a Bat
                if (identity instanceof Bat) {
                    cir.setReturnValue(true);
                }
            }
        }
    }

    @Inject(
            method = "getEffect",
            at = @At("HEAD"),
            cancellable = true
    )
    private void returnNightVisionInstance(Holder<MobEffect> effect, CallbackInfoReturnable<MobEffectInstance> cir) {
        if ((Object) this instanceof Player player) {
            if (effect.equals(MobEffects.NIGHT_VISION)) {
                LivingEntity identity = PlayerIdentity.getIdentity(player);

                // Apply 'Night Vision' status effect to player if they are a Bat
                if (identity instanceof Bat) {
                    cir.setReturnValue(new MobEffectInstance(MobEffects.NIGHT_VISION, 100000, 0, false, false));
                }
            }
        }
    }

    @Inject(method = "isSensitiveToWater", at = @At("HEAD"), cancellable = true)
    protected void identity_hurtByWater(CallbackInfoReturnable<Boolean> cir) {
        if((LivingEntity) (Object) this instanceof Player player) {
            LivingEntity entity = PlayerIdentity.getIdentity(player);

            if (entity != null) {
                cir.setReturnValue(entity.isSensitiveToWater());
            }
        }
    }

    @Inject(method = "canBreatheUnderwater", at = @At("HEAD"), cancellable = true)
    protected void identity_canBreatheInWater(CallbackInfoReturnable<Boolean> cir) {
        if((LivingEntity) (Object) this instanceof Player player) {
            LivingEntity entity = PlayerIdentity.getIdentity(player);

            if (entity != null) {
                cir.setReturnValue(entity.canBreatheUnderwater() || entity instanceof Dolphin || entity.getType().builtInRegistryHolder().is(IdentityEntityTags.UNDROWNABLE));
            }
        }
    }

    @Unique
    private boolean nearbySongPlaying = false;

    @Environment(EnvType.CLIENT)
    @Inject(method = "setRecordPlayingNearby", at = @At("RETURN"))
    protected void identity_setNearbySongPlaying(BlockPos songPosition, boolean playing, CallbackInfo ci) {
        if((LivingEntity) (Object) this instanceof Player player) {
            nearbySongPlaying = playing;
        }
    }

    @Override
    public boolean identity_isNearbySongPlaying() {
        return nearbySongPlaying;
    }

    @Inject(method = "isInvertedHealAndHarm", at = @At("HEAD"), cancellable = true)
    protected void identity_isUndead(CallbackInfoReturnable<Boolean> cir) {
        if((LivingEntity) (Object) this instanceof Player player) {
            LivingEntity identity = PlayerIdentity.getIdentity(player);

            if (identity != null) {
                cir.setReturnValue(identity.isInvertedHealAndHarm());
            }
        }
    }

    @Inject(method = "canStandOnFluid", at = @At("HEAD"), cancellable = true)
    protected void identity_canWalkOnFluid(FluidState state, CallbackInfoReturnable<Boolean> cir) {
        if((LivingEntity) (Object) this instanceof Player player) {
            LivingEntity identity = PlayerIdentity.getIdentity(player);

            if (identity != null && identity.getType().builtInRegistryHolder().is(IdentityEntityTags.LAVA_WALKING) && state.is(FluidTags.LAVA)) {
                cir.setReturnValue(true);
            }
        }
    }

    @Inject(
            method = "onClimbable",
            at = @At("HEAD"),
            cancellable = true
    )
    protected void identity_allowSpiderClimbing(CallbackInfoReturnable<Boolean> cir) {
        if((LivingEntity) (Object) this instanceof Player player) {
            LivingEntity identity = PlayerIdentity.getIdentity(player);

            if (identity instanceof Spider) {
                cir.setReturnValue(this.horizontalCollision);
            }
        }
    }
}
