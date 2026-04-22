package draylar.identity.mixin;

import draylar.identity.api.PlayerIdentity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.cow.Cow;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.TridentItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(Player.class)
public abstract class CowTridentDamageMixin {

    // Targets the final composite damage float (base + enchant) passed to hurtOrSimulate in attack().
    @ModifyArg(
        method = "attack",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;hurtOrSimulate(Lnet/minecraft/world/damagesource/DamageSource;F)Z"),
        index = 1
    )
    private float identity$cowTridentBuff(float damage) {
        Player self = (Player) (Object) this;
        LivingEntity identity = PlayerIdentity.getIdentity(self);
        if (identity instanceof Cow
                && self.getMainHandItem().getItem() instanceof TridentItem
                && (self.isSprinting() || self.isAutoSpinAttack())) {
            return damage * 2.0F;
        }
        return damage;
    }
}
