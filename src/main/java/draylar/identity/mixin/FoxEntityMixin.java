package draylar.identity.mixin;

import draylar.identity.api.PlayerIdentity;
import draylar.identity.config.IdentityConfig;
import draylar.identity.registry.IdentityEntityTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.fish.WaterAnimal;
import net.minecraft.world.entity.animal.fox.Fox;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Predicate;

@Mixin(Fox.class)
public abstract class FoxEntityMixin extends Animal {

    @Shadow @Final @Mutable
    private static Predicate<Entity> AVOID_PLAYERS;

    private FoxEntityMixin(EntityType<? extends Animal> entityType, Level level) {
        super(entityType, level);
    }

    // Change the default "flee from player," predicate to ignore players disguised as Foxes.
    // Hopefully nobody else needs to modify fox fleeing behavior.
    static {
        AVOID_PLAYERS = entity -> {
            boolean isIdentityPlayer = false;

            if(entity instanceof Player player) {
                LivingEntity identity = PlayerIdentity.getIdentity(player);
                if(identity instanceof Fox) {
                    isIdentityPlayer = true;
                }
            }

            return !entity.isDiscrete() && EntitySelector.NO_CREATIVE_OR_SPECTATOR.test(entity) && !isIdentityPlayer;
        };
    }

    @Inject(
            method = "registerGoals",
            at = @At("RETURN")
    )
    private void addPlayerTarget(CallbackInfo ci) {
        this.targetSelector.addGoal(7, new NearestAttackableTargetGoal<>(this, Player.class, 10, false, false, (player, serverLevel) -> {
            // ensure foxes can attack players with an identity similar to their normal prey
            if(!IdentityConfig.getInstance().foxesAttackIdentityPrey()) {
                return false;
            }

            // foxes can target players if their identity is in the fox_prey tag, or if they are an entity that extends WaterAnimal
            // todo: add baby turtle targeting
            LivingEntity identity = PlayerIdentity.getIdentity((Player) player);
            return identity != null && identity.getType().builtInRegistryHolder().is(IdentityEntityTags.FOX_PREY) || identity instanceof WaterAnimal;
        }));
    }
}
