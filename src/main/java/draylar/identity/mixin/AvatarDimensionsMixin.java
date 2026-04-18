package draylar.identity.mixin;

import draylar.identity.api.PlayerIdentity;
import net.minecraft.world.entity.Avatar;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Avatar.class)
public class AvatarDimensionsMixin {

    @Inject(
            method = "getDefaultDimensions",
            at = @At("HEAD"),
            cancellable = true
    )
    private void identity_getDefaultDimensions(Pose pose, CallbackInfoReturnable<EntityDimensions> cir) {
        if ((Object) this instanceof Player player) {
            LivingEntity identity = PlayerIdentity.getIdentity(player);
            if (identity != null) {
                cir.setReturnValue(identity.getDimensions(pose));
            }
        }
    }
}
