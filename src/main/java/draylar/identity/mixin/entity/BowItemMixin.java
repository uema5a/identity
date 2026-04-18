package draylar.identity.mixin.entity;

import draylar.identity.api.PlayerIdentity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.skeleton.WitherSkeleton;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.item.BowItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BowItem.class)
public class BowItemMixin {

    @Inject(method = "shootProjectile", at = @At("TAIL"))
    private void identity$flamingArrows(
            LivingEntity shooter,
            Projectile projectileEntity,
            int index,
            float power,
            float uncertainty,
            float angle,
            LivingEntity targetOverride,
            CallbackInfo ci) {
        if (shooter instanceof Player player
                && PlayerIdentity.getIdentity(player) instanceof WitherSkeleton
                && projectileEntity instanceof AbstractArrow arrow) {
            arrow.igniteForSeconds(100);
        }
    }
}
