package draylar.identity.mixin;

import draylar.identity.api.PlayerIdentity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@SuppressWarnings("ConstantConditions")
@Mixin(Player.class)
public class PlayerByteStatusMixin {

    // When a player receives a handleEntityEvent byte, pass it on to their Identity.
    @Inject(method = "handleEntityEvent", at = @At("RETURN"))
    private void identity$passByteStatus(byte status, CallbackInfo ci) {
        @Nullable LivingEntity identity = PlayerIdentity.getIdentity((Player) (Object) this);
        if(identity != null) {
            identity.handleEntityEvent(status);
        }
    }
}
