package draylar.identity.mixin.accessor;

import draylar.identity.compat.LivingEntityCompatAccessor;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.sounds.SoundEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(LivingEntity.class)
public interface LivingEntityAccessor extends LivingEntityCompatAccessor {

    @Accessor
    boolean isJumping();

    @Invoker("updatingUsingItem")
    void callTickActiveItemStack();

    @Invoker
    SoundEvent callGetHurtSound(DamageSource source);

    @Invoker
    SoundEvent callGetDeathSound();

    @Invoker
    void callPlayBlockFallSound();

    @Invoker
    int callCalculateFallDamage(double fallDistance, float damageMultiplier);

    @Invoker
    float callGetSoundVolume();

    @Invoker("getVoicePitch")
    float callGetSoundPitch();

    @Invoker
    void callSetLivingEntityFlag(int mask, boolean value);

    @Invoker("getNextAirOnLand")
    int identity$getNextAirOnLand(int air);
}
