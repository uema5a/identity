package draylar.identity.mixin;

import draylar.identity.api.PlayerIdentity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.cow.Cow;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.Consumable;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FoodProperties.class)
public abstract class CowFoodRestorationMixin {

    // Cancelling at the FoodData.eat() invoke skips only the burp sound (cosmetic, no gameplay impact).
    // Using the method's LivingEntity parameter directly (instead of LocalCapture) avoids brittle
    // LVT ordinal assumptions; if vanilla re-orders locals in a patch this still works.
    @Inject(
        method = "onConsume",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/world/food/FoodData;eat(Lnet/minecraft/world/food/FoodProperties;)V"),
        cancellable = true
    )
    private void identity$cowReducedFoodGain(Level level, LivingEntity entity, ItemStack stack,
                                             Consumable consumable, CallbackInfo ci) {
        if (!(entity instanceof Player player)) return;
        if (!(PlayerIdentity.getIdentity(player) instanceof Cow)) return;

        FoodProperties self = (FoodProperties) (Object) this;
        // nutrition/4 with floor(), clamped to 1 so any edible food still restores at least ½ hunger icon.
        // Saturation scaled uniformly; negative saturation modifiers (e.g. rotten flesh) remain negative.
        player.getFoodData().eat(Math.max(1, self.nutrition() / 4), self.saturation() * 0.25F);
        ci.cancel();
    }
}
