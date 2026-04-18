package draylar.identity.mixin;

import draylar.identity.impl.SonicBoomUser;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.wolf.Wolf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Player.class)
public abstract class PlayerSonicBoomMixin extends LivingEntity implements SonicBoomUser {

    @Unique
    private int identity$ability_wardenBoomDelay = -1;

    protected PlayerSonicBoomMixin(EntityType<? extends LivingEntity> type, Level level) {
        super(type, level);
    }

    @Inject(method = "tick", at = @At("HEAD"))
    private void identity$wardenSonicBoomTick(CallbackInfo ci) {
        if (identity$ability_wardenBoomDelay > 0) {
            identity$ability_wardenBoomDelay--;
        } else if (identity$ability_wardenBoomDelay == 0) {
            identity$ability_wardenBoomDelay = -1;
            identity$ability_performSonicBoom();
        }
    }

    @Override
    public void identity$ability_startSonicBoom() {
        identity$ability_wardenBoomDelay = 40;
    }

    @Unique
    private void identity$ability_performSonicBoom() {
        if (level().isClientSide()) return;
        ServerLevel serverLevel = (ServerLevel) this.level();
        Vec3 origin = this.position().add(0, this.getEyeHeight(), 0);
        Vec3 direction = this.getViewVector(1.0F);

        for (int i = 1; i <= 16; i++) {
            Vec3 pos = origin.add(direction.scale(i));
            serverLevel.sendParticles(ParticleTypes.SONIC_BOOM, pos.x, pos.y, pos.z, 1, 0, 0, 0, 0);
        }

        serverLevel.playSound(null, this.getX(), this.getY(), this.getZ(),
                SoundEvents.WARDEN_SONIC_BOOM, SoundSource.HOSTILE, 3.0F, 1.0F);

        AABB box = new AABB(origin, origin.add(direction.scale(16))).inflate(3);
        var sonicBoomSource = serverLevel.damageSources().sonicBoom(this);
        for (LivingEntity target : serverLevel.getEntitiesOfClass(LivingEntity.class, box,
                e -> e != this && !(e instanceof Wolf) && e.isAlive())) {
            target.hurt(sonicBoomSource, 10.0F);
            Vec3 kb = target.position().subtract(this.position()).normalize().scale(1.5);
            target.setDeltaMovement(target.getDeltaMovement().add(kb.x, 0.3, kb.z));
        }
    }
}
