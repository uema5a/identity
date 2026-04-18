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

import java.util.ArrayList;
import java.util.List;

@Mixin(WitherBoss.class)
public abstract class WitherEntityMixin extends Monster {

    private WitherEntityMixin(EntityType<? extends Monster> entityType, Level level) {
        super(entityType, level);
    }

    @Inject(
            method = "customServerAiStep",
            at = @At(value = "INVOKE", target = "Ljava/util/List;isEmpty()Z"), // TODO descriptor verify
            locals = LocalCapture.CAPTURE_FAILHARD
    )
    private void removeInvalidPlayerTargets(CallbackInfo ci, int j, List<LivingEntity> list) {
        List<LivingEntity> toRemove = new ArrayList<>();

        list.forEach(entity -> {
            if(entity instanceof Player player) {
                LivingEntity identity = PlayerIdentity.getIdentity(player);

                // potentially ignore undead identity players
                if(identity != null && identity.isInvertedHealAndHarm()) {
                    if(this.getTarget() != null) {
                        // if this wither's target is not equal to the current entity
                        if(!this.getTarget().getUUID().equals(entity.getUUID())) {
                            toRemove.add(entity);
                        }
                    } else {
                        toRemove.add(entity);
                    }
                }
            }
        });

        list.removeAll(toRemove);
    }
}
