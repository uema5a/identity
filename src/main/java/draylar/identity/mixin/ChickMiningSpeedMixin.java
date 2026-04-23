package draylar.identity.mixin;

import draylar.identity.api.PlayerIdentity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.chicken.Chicken;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Player.class)
public abstract class ChickMiningSpeedMixin {

    @Inject(method = "getDestroySpeed", at = @At("RETURN"), cancellable = true)
    private void identity$chickMiningPenalty(BlockState state, CallbackInfoReturnable<Float> cir) {
        Player self = (Player) (Object) this;
        LivingEntity identity = PlayerIdentity.getIdentity(self);
        boolean babyChick = identity instanceof Chicken c && c.isBaby();
        if (draylar.identity.Identity.LOGGER.isDebugEnabled() && identity instanceof Chicken) {
            draylar.identity.Identity.LOGGER.debug("[Chick mining] side={} identity={} isBaby={} in={} out={}",
                    self.level().isClientSide() ? "C" : "S",
                    identity == null ? "null" : identity.getType(),
                    identity instanceof Chicken c2 ? c2.isBaby() : "N/A",
                    cir.getReturnValue(),
                    babyChick ? cir.getReturnValue() * 0.5F : cir.getReturnValue());
        }
        if (babyChick) {
            cir.setReturnValue(cir.getReturnValue() * 0.5F);
        }
    }
}
