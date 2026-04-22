package draylar.identity.mixin;

import draylar.identity.api.PlayerIdentity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.axolotl.Axolotl;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Player.class)
public abstract class AxolotlWaterImmunityMixin {

    @Inject(method = "hurtServer", at = @At("HEAD"), cancellable = true)
    private void identity$axolotlWaterImmunity(ServerLevel level, DamageSource source, float amount,
                                               CallbackInfoReturnable<Boolean> cir) {
        Player player = (Player) (Object) this;
        LivingEntity identity = PlayerIdentity.getIdentity(player);
        if (identity instanceof Axolotl && player.isInWater()) {
            cir.setReturnValue(false);
        }
    }
}
