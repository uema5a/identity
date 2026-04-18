package draylar.identity.mixin;

import draylar.identity.api.PlayerIdentity;
import draylar.identity.registry.IdentityEntityTags;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.tags.TagKey;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public class PlayerSwimmingMixin {

    @Inject(
            method = "jumpInLiquid", at = @At("HEAD"), cancellable = true)
    private void onGolemSwimUp(TagKey<Fluid> fluid, CallbackInfo ci) {
        LivingEntity thisEntity = (LivingEntity) (Object) this;
        if(thisEntity instanceof Player player) {
            LivingEntity identity = PlayerIdentity.getIdentity(player);

            if(identity != null && identity.getType().builtInRegistryHolder().is(IdentityEntityTags.CANT_SWIM)) {
                ci.cancel();
            }
        }
    }
}
