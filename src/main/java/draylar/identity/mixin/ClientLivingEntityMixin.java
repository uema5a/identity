package draylar.identity.mixin;

import draylar.identity.api.PlayerIdentity;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// ClientLivingEntityMixin.java
@Environment(EnvType.CLIENT)
@Mixin(LivingEntity.class)
public abstract class ClientLivingEntityMixin {
    @Unique
    private boolean identity$isAquatic(LivingEntity identity) {
        if (identity == null) return false;

        MobCategory group = identity.getType().getCategory();

        return switch (group) {
            case WATER_CREATURE, WATER_AMBIENT, UNDERGROUND_WATER_CREATURE -> true;
            default -> false;
        };
    }

    @Inject(method = "travel", at = @At("HEAD"))
    private void identity$handleAquaticMovement(Vec3 movementInput, CallbackInfo ci) {
        if ((Object) this instanceof Player player) {
            LivingEntity identity = PlayerIdentity.getIdentity(player);

            if (identity != null && identity$isAquatic(identity)) {
                boolean inWater = player.isInWater();
                boolean inBubbleColumn = player.level().getBlockState(player.blockPosition()).is(Blocks.BUBBLE_COLUMN);

                if (inWater || inBubbleColumn) {
                    double speedMultiplier = identity.getType() == EntityType.DOLPHIN ? 0.4 : 0.25;

                    Vec3 input = movementInput;

                    if (Minecraft.getInstance().options.keyJump.isDown()) {
                        input = input.add(0, -1.0, 0);
                    }
                    if (Minecraft.getInstance().options.keyShift.isDown()) {
                        input = input.add(0, 1.0, 0);
                    }

                    Vec3 look = player.getLookAngle();
                    Vec3 up = new Vec3(0, 1, 0);
                    Vec3 right = up.cross(look).normalize();
                    Vec3 adjustedUp = right.cross(look).normalize();
                    Vec3 worldInput =
                            right.scale(input.x)
                                    .add(adjustedUp.scale(input.y))
                                    .add(look.scale(input.z));

                    if (worldInput.lengthSqr() > 0.0001) {
                        player.setDeltaMovement(worldInput.normalize().scale(speedMultiplier));
                    }
                }
            }
        }
    }
}
