package draylar.identity.mixin;

import draylar.identity.api.PlayerIdentity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.hoglin.HoglinAi;
import net.minecraft.world.entity.monster.hoglin.Hoglin;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

@Mixin(HoglinAi.class)
public class HoglinBrainMixin {

    @Inject(
            method = "findNearestValidAttackTarget",
            at = @At("RETURN"),
            cancellable = true
    )
    private static void findNearestValidAttackTarget(ServerLevel level, Hoglin hoglin, CallbackInfoReturnable<Optional<? extends LivingEntity>> cir) {
        Optional<? extends LivingEntity> ret = cir.getReturnValue();
        if(ret.isPresent()) {
            LivingEntity target = ret.get();

            if(target instanceof Player player) {
                LivingEntity identity = PlayerIdentity.getIdentity(player);

                if(identity != null) {
                    if(identity instanceof Hoglin) {
                        cir.setReturnValue(Optional.empty());
                    }
                }
            }
        }
    }
}
