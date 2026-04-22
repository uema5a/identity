package draylar.identity.mixin;

import draylar.identity.api.PlayerIdentity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.cow.Cow;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodData;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.Consumable;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import net.minecraft.util.RandomSource;

@Mixin(FoodProperties.class)
public abstract class CowFoodRestorationMixin {

    // @Redirect cannot expose enclosing-method params, so LocalCapture is used to reach the Player.
    // Cancelling at the eat() invoke skips only the burp sound (cosmetic; no gameplay impact).
    @Inject(
        method = "onConsume",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/world/food/FoodData;eat(Lnet/minecraft/world/food/FoodProperties;)V"),
        cancellable = true,
        locals = LocalCapture.CAPTURE_FAILHARD
    )
    private void identity$cowReducedFoodGain(Level level, LivingEntity entity, ItemStack stack,
            Consumable consumable, CallbackInfo ci,
            RandomSource random, Player player) {
        if (!(PlayerIdentity.getIdentity(player) instanceof Cow)) return;

        FoodProperties self = (FoodProperties) (Object) this;
        FoodData foodData = player.getFoodData();
        foodData.eat(Math.max(1, self.nutrition() / 4), self.saturation() * 0.25F);
        ci.cancel();
    }
}
