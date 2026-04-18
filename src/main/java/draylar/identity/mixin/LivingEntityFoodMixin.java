package draylar.identity.mixin;

import draylar.identity.api.PlayerIdentity;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.wolf.Wolf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Set;

@Mixin(LivingEntity.class)
public abstract class LivingEntityFoodMixin extends Entity {

    @Shadow
    public abstract ItemStack getUseItem();

    // Lazy-init: Items.* would be null if evaluated during LivingEntity.<clinit>
    @Unique
    private static Set<Item> identity$wolfHarmful;

    @Unique
    private static Set<Item> identity$getWolfHarmful() {
        Set<Item> set = identity$wolfHarmful;
        if (set == null) {
            set = Set.of(Items.CHICKEN, Items.PUFFERFISH, Items.ROTTEN_FLESH);
            identity$wolfHarmful = set;
        }
        return set;
    }

    public LivingEntityFoodMixin(EntityType<?> type, Level level) {
        super(type, level);
    }

    @Inject(method = "completeUsingItem", at = @At("HEAD"), cancellable = true)
    private void identity$wolfFoodImmunity(CallbackInfo ci) {
        if (!((LivingEntity) (Object) this instanceof Player player)) {
            return;
        }
        if (!(PlayerIdentity.getIdentity(player) instanceof Wolf)) {
            return;
        }
        ItemStack useItem = getUseItem();
        if (identity$getWolfHarmful().contains(useItem.getItem())) {
            ci.cancel();
        }
    }
}
