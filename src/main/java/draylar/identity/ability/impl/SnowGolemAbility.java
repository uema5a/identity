package draylar.identity.ability.impl;

import draylar.identity.ability.IdentityAbility;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.animal.golem.SnowGolem;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Snowball;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class SnowGolemAbility extends IdentityAbility<SnowGolem> {

    @Override
    public void onUse(Player player, SnowGolem identity, Level level) {
        // Play throw sound
        level.playSound(
                null,
                player.getX(),
                player.getY(),
                player.getZ(),
                SoundEvents.SNOWBALL_THROW,
                SoundSource.NEUTRAL,
                0.5F,
                0.4F / (level.random.nextFloat() * 0.4F + 0.8F)
        );

        if (!level.isClientSide) {
            Vec3 look = player.getLookAngle();
            Vec3 spawnPos = player.getEyePosition().add(look.scale(0.8)); // Slightly forward from eyes

            for (int i = 0; i < 10; i++) {
                Snowball snowball = new Snowball(level, player);

                // Randomize direction slightly for spread
                float pitchOffset = (float) (player.getXRot() + level.random.nextGaussian() * 5.0);
                float yawOffset = (float) (player.getYRot() + level.random.nextGaussian() * 5.0);

                snowball.shootFromRotation(player, pitchOffset, yawOffset, 0.0F, 1.5F, 1.0F);
                snowball.moveTo(spawnPos.x, spawnPos.y, spawnPos.z, yawOffset, pitchOffset);

                level.addFreshEntity(snowball);
            }
        }
    }

    @Override
    public Item getIcon() {
        return Items.SNOWBALL;
    }
}
