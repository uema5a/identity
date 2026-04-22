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
        LivingEntity identity = PlayerIdentity.getIdentity((Player) (Object) this);
        if (identity instanceof Chicken c && c.isBaby()) {
            cir.setReturnValue(cir.getReturnValue() * 0.5F);
        }
    }
}
