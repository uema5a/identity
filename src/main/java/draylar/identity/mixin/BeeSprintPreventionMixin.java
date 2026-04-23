package draylar.identity.mixin;

import draylar.identity.api.PlayerIdentity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.bee.Bee;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Blocks sprint entirely while in Bee identity.
 *
 * <p>Sprint is client-authoritative — LocalPlayer.aiStep() re-asserts setSprinting(true) every
 * tick while the sprint key is held. Server-side blocks are overwritten next packet. By cancelling
 * at setSprinting HEAD on LivingEntity (common class, runs on both sides), neither client nor
 * server can flip the flag to true while the conditions hold.
 */
@Mixin(LivingEntity.class)
public abstract class BeeSprintPreventionMixin {

    @Inject(method = "setSprinting(Z)V", at = @At("HEAD"), cancellable = true)
    private void identity$beeNoSprint(boolean sprinting, CallbackInfo ci) {
        if (!sprinting) return;
        LivingEntity self = (LivingEntity) (Object) this;
        if (!(self instanceof Player player)) return;
        if (PlayerIdentity.getIdentity(player) instanceof Bee) {
            ci.cancel();
        }
    }
}
