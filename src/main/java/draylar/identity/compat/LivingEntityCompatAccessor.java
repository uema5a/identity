package draylar.identity.compat;

import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.sounds.SoundEvent;

public interface LivingEntityCompatAccessor {
    boolean isJumping();

    SoundEvent callGetHurtSound(DamageSource source);

    SoundEvent callGetDeathSound();

    void callPlayBlockFallSound();

    int callCalculateFallDamage(double fallDistance, float damageMultiplier);

    float callGetSoundVolume();

    float callGetSoundPitch();

    void callSetLivingEntityFlag(int mask, boolean value);

    float callGetEyeHeight(Pose pose, EntityDimensions dimensions);

    int identity$getNextAirOnLand(int air);

    float callGetActiveEyeHeight(Pose pose, EntityDimensions dimensions);

    void callTickActiveItemStack();
}
