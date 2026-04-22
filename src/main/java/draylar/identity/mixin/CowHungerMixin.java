package draylar.identity.mixin;

import draylar.identity.api.PlayerIdentity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.cow.Cow;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(Player.class)
public abstract class CowHungerMixin {

    @ModifyArg(
        method = "causeFoodExhaustion",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/world/food/FoodData;addExhaustion(F)V")
    )
    private float identity$cow4xHunger(float exhaustion) {
        LivingEntity identity = PlayerIdentity.getIdentity((Player) (Object) this);
        if (identity instanceof Cow) return exhaustion * 4.0F;
        return exhaustion;
    }
}
