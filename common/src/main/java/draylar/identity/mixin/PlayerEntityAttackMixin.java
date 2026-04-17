package draylar.identity.mixin;

import draylar.identity.api.IdentityGranting;
import draylar.identity.api.PlayerIdentity;
import draylar.identity.api.variant.IdentityType;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Player.class)
public abstract class PlayerEntityAttackMixin extends LivingEntity {

    private PlayerEntityAttackMixin(EntityType<? extends LivingEntity> entityType, Level level) {
        super(entityType, level);
    }

    @Inject(
            method = "attack",
            at = @At("HEAD"), cancellable = true)
    private void identityAttack(Entity target, CallbackInfo ci) {
        LivingEntity identity = PlayerIdentity.getIdentity((Player) (Object) this);

        if(identity != null) {
            if(getMainHandItem().isEmpty()) {
                try {
                    if (identity instanceof Mob mob) {
                        mob.doHurtTarget((ServerLevel) this.level(), target);
                    }
                    ci.cancel();

                    // If the target died, grant identity
                    if(!target.isAlive() && target instanceof LivingEntity living) {
                        IdentityGranting.grantByAttack((Player) (Object) this, IdentityType.from(living));
                    }
                } catch (Exception e) {
                    // FALL BACK TO DEFAULT BEHAVIOR.
                    // Some mobs do not override, so it defaults to attack damage attribute, but the identity does not have any
                }
            }
        }
    }
}
