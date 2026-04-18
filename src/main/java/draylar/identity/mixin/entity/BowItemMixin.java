package draylar.identity.mixin.entity;

import net.minecraft.world.item.BowItem;
import org.spongepowered.asm.mixin.Mixin;

// TODO Phase D: port to 26.1 - BowItem.releaseUsing rewritten; no longer calls addFreshEntity directly.
// Arrow flame injection needs new approach via shootProjectile override or ProjectileWeaponItem.shoot hook.
@Mixin(BowItem.class)
public class BowItemMixin {
}
