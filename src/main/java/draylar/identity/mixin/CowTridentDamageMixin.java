package draylar.identity.mixin;

import draylar.identity.api.PlayerIdentity;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.cow.Cow;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

/**
 * Cow identity offensive buffs:
 * <ul>
 *   <li>Sprint (dash) attack → 2x damage (regardless of weapon)</li>
 *   <li>Sword critical → 2x damage (replaces vanilla 1.5x crit)</li>
 * </ul>
 *
 * Sprint and critical are mutually exclusive in vanilla (crit requires {@code !isSprinting()}),
 * so the two branches never overlap.
 */
@Mixin(Player.class)
public abstract class CowTridentDamageMixin {

    @ModifyArg(
        method = "attack",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;hurtOrSimulate(Lnet/minecraft/world/damagesource/DamageSource;F)Z"),
        index = 1
    )
    private float identity$cowAttackBuff(float damage) {
        Player self = (Player) (Object) this;
        LivingEntity identity = PlayerIdentity.getIdentity(self);
        if (!(identity instanceof Cow)) return damage;

        // Sprint (dash) attack → 2x damage
        if (self.isSprinting()) {
            if (draylar.identity.Identity.LOGGER.isDebugEnabled()) {
                draylar.identity.Identity.LOGGER.debug("[Cow dash attack 2x] damage {} → {}", damage, damage * 2.0F);
            }
            return damage * 2.0F;
        }

        // Sword + critical → final damage 2x base. Vanilla crit multiplies by 1.5 before
        // hurtOrSimulate, so we multiply by (2 / 1.5) to land on 2x base overall.
        if (self.getMainHandItem().is(ItemTags.SWORDS)) {
            boolean canCrit = self.fallDistance > 0.0F
                    && !self.onGround()
                    && !self.onClimbable()
                    && !self.isInWater()
                    && !self.hasEffect(MobEffects.BLINDNESS)
                    && !self.isPassenger();
            // (isSprinting is already false here — vanilla crit check excludes sprinting and we
            // handled the sprint branch above, so no need to re-check.)
            if (canCrit) {
                float out = damage * (2.0F / 1.5F);
                if (draylar.identity.Identity.LOGGER.isDebugEnabled()) {
                    draylar.identity.Identity.LOGGER.debug("[Cow sword crit 2x] damage {} → {}", damage, out);
                }
                return out;
            }
        }
        return damage;
    }
}
