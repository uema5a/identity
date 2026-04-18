package draylar.identity.mixin;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Ravager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.raid.Raider;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(Ravager.class)
public abstract class RavagerEntityMixin extends Raider {

    protected RavagerEntityMixin(EntityType<? extends Raider> type, Level level) {
        super(type, level);
    }

    @Override
    protected void tickRidden(Player rider, Vec3 movementInput) {
        float yaw = rider.getYRot();
        float pitch = rider.getXRot() * 0.5F;
        this.setYRot(yaw);
        this.yRotO = yaw;
        this.setXRot(pitch);
        this.setRot(yaw, pitch);
        this.yBodyRot = yaw;
        this.yHeadRot = yaw;
    }

    @Override
    protected Vec3 getRiddenInput(Player rider, Vec3 movementInput) {
        float forward = rider.zza;
        float strafe = rider.xxa * 0.5F;
        if (forward <= 0.0F) forward *= 0.25F;
        return new Vec3(strafe, movementInput.y, forward);
    }

    @Override
    protected float getRiddenSpeed(Player rider) {
        return (float) this.getAttributeValue(Attributes.MOVEMENT_SPEED);
    }
}
