package draylar.identity.mixin;

import draylar.identity.api.PlayerIdentity;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.cow.Cow;
import net.minecraft.world.entity.player.Player;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.TridentItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(Player.class)
public abstract class CowTridentDamageMixin {

    // Trident (sprint/spin) or Sword (critical) → 2x final damage.
    // Critical conditions mirror vanilla's crit check in Player.attack: falling while attacking,
    // not on ground/climb/water, not sprinting, not blinded, not passenger. We duplicate because
    // @ModifyArg can't access the target entity, but all of these are player-local state.
    @ModifyArg(
        method = "attack",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;hurtOrSimulate(Lnet/minecraft/world/damagesource/DamageSource;F)Z"),
        index = 1
    )
    private float identity$cowAttackBuff(float damage) {
        Player self = (Player) (Object) this;
        LivingEntity identity = PlayerIdentity.getIdentity(self);
        if (!(identity instanceof Cow)) return damage;

        var mainStack = self.getMainHandItem();
        var main = mainStack.getItem();

        // Trident dash / charge (riptide spin) → 2× damage
        if (main instanceof TridentItem && (self.isSprinting() || self.isAutoSpinAttack())) {
            if (draylar.identity.Identity.LOGGER.isDebugEnabled()) {
                draylar.identity.Identity.LOGGER.debug("[Cow trident 2x] damage {} → {}", damage, damage * 2.0F);
            }
            return damage * 2.0F;
        }

        // Sword + critical → final damage 2× base. Vanilla crit applies ×1.5 before hurtOrSimulate;
        // we multiply by (2 ÷ 1.5) so the player-observed total is 2× base damage.
        if (mainStack.is(ItemTags.SWORDS)) {
            boolean canCrit = self.fallDistance > 0.0F
                    && !self.onGround()
                    && !self.onClimbable()
                    && !self.isInWater()
                    && !self.hasEffect(MobEffects.BLINDNESS)
                    && !self.isPassenger()
                    && !self.isSprinting();
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
