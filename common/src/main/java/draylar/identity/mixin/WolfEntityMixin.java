package draylar.identity.mixin;

import draylar.identity.api.PlayerIdentity;
import draylar.identity.config.IdentityConfig;
import draylar.identity.registry.IdentityEntityTags;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.animal.wolf.Wolf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Wolf.class)
public abstract class WolfEntityMixin extends TamableAnimal {

    private WolfEntityMixin(EntityType<? extends TamableAnimal> entityType, Level level) {
        super(entityType, level);
    }

    @Inject(
            method = "registerGoals",
            at = @At("RETURN")
    )
    private void addPlayerTarget(CallbackInfo ci) {
        this.targetSelector.addGoal(7, new NearestAttackableTargetGoal<>(this, Player.class, 10, false, false, (player, serverLevel) -> {
            // ensure wolves can attack players with an identity similar to their normal prey
            if(!IdentityConfig.getInstance().wolvesAttackIdentityPrey()) {
                return false;
            }

            LivingEntity identity = PlayerIdentity.getIdentity((Player) player);

            // wolves should ignore players that look like their prey if they have an owner,
            // unless the config option is turned to true
            LivingEntity owner = this.getOwner();
            if(owner != null || IdentityConfig.getInstance().ownedWolvesAttackIdentityPrey()) {
                return false;
            }

            return identity != null && identity.getType().builtInRegistryHolder().is(IdentityEntityTags.WOLF_PREY);
        }));
    }
}
