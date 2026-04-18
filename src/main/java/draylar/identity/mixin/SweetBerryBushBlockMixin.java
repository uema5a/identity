package draylar.identity.mixin;

import draylar.identity.api.PlayerIdentity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.SweetBerryBushBlock;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.InsideBlockEffectApplier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.bee.Bee;
import net.minecraft.world.entity.animal.fox.Fox;
import net.minecraft.world.entity.player.Player;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SweetBerryBushBlock.class)
public class SweetBerryBushBlockMixin {

    @Inject(
            method = "entityInside",
            at = @At(value = "HEAD"),
            cancellable = true
    )
    private void onDamage(BlockState state, Level level, BlockPos pos, Entity entity, InsideBlockEffectApplier effectApplier, boolean flag, CallbackInfo ci) {
        if(entity instanceof Player player) {
            LivingEntity identity = PlayerIdentity.getIdentity(player);

            // Cancel damage if the player's identity is a fox
            if(identity instanceof Fox || identity instanceof Bee) {
                ci.cancel();
            }
        }
    }
}
