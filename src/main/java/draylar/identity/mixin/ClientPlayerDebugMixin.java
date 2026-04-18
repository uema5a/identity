package draylar.identity.mixin;

import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Mixin;

// TODO: re-implement gaboulibs PlayerDebugUtils functionality locally
@Mixin(LocalPlayer.class)
public class ClientPlayerDebugMixin {

//    @Inject(method = "tick", at = @At("TAIL"))
//    private void onClientTick(CallbackInfo ci) {
//        // TODO: re-implement gaboulibs PlayerDebugUtils functionality locally
//        // PlayerDebugUtils.logPlayerDebug((Player)(Object)this, "client");
//    }
}
