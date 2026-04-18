package draylar.identity.mixin;

import draylar.identity.api.PlayerIdentity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.List;

@Mixin(WitherBoss.class)
public abstract class WitherEntityMixin extends Monster {

    private WitherEntityMixin(EntityType<? extends Monster> entityType, Level level) {
        super(entityType, level);
    }

    // MC 26.1 LVT for customServerAiStep: 3 ints precede the List<LivingEntity> target list
    @Inject(
            method = "customServerAiStep",
            at = @At(value = "INVOKE", target = "Ljava/util/List;isEmpty()Z"),
            locals = LocalCapture.CAPTURE_FAILHARD
    )
    private void removeInvalidPlayerTargets(CallbackInfo ci, int j, int someInt, int b, List<LivingEntity> list) {
        LivingEntity currentTarget = this.getTarget();
        list.removeIf(entity -> {
            if (!(entity instanceof Player player)) return false;
            LivingEntity identity = PlayerIdentity.getIdentity(player);
            if (identity == null || !identity.isInvertedHealAndHarm()) return false;
            return currentTarget == null || !currentTarget.getUUID().equals(entity.getUUID());
        });
    }
}
