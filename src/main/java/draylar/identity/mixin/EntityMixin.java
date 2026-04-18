package draylar.identity.mixin;

import draylar.identity.api.PlayerIdentity;
import draylar.identity.impl.DimensionsRefresher;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public abstract class EntityMixin implements DimensionsRefresher {

    @Shadow
    public abstract void refreshDimensions();

    @Override
    public void identity_refreshDimensions() {
        // Delegate to vanilla refreshDimensions().
        // This works because LivingEntityMixin intercepts getDefaultDimensions()
        // to return identity dimensions, which propagates through:
        //   refreshDimensions() -> fixupDimensions() -> getDimensions() -> getDefaultDimensions()
        // Setting dimensions, eyeHeight, and bounding box correctly.
        this.refreshDimensions();
    }

    @Inject(
            method = "fireImmune",
            at = @At("HEAD"),
            cancellable = true
    )
    private void isFireImmune(CallbackInfoReturnable<Boolean> cir) {
        if((Object) this instanceof Player player) {
            LivingEntity identity = PlayerIdentity.getIdentity(player);

            if(identity != null) {
                cir.setReturnValue(identity.getType().fireImmune());
            }
        }
    }
}
