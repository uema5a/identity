package draylar.identity.ability.impl;

import draylar.identity.ability.IdentityAbility;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.WitherSkull;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class WitherEntityAbility extends IdentityAbility<WitherBoss> {

    @Override
    public void onUse(Player player, WitherBoss identity, Level level) {
        if (level.isClientSide) {
            return;
        }

        // Play shoot sound
        level.playSound(
                null,
                player.getX(),
                player.getY(),
                player.getZ(),
                SoundEvents.WITHER_SHOOT,
                SoundSource.HOSTILE,
                1.0F,
                0.8F + level.random.nextFloat() * 0.4F
        );

        // Direction of fire
        Vec3 look = player.getLookAngle();

        // Spawn position: in front of the player's eyes (~2 blocks forward)
        Vec3 spawnPos = player.getEyePosition().add(look.scale(2.0));

        // Create skull entity
        WitherSkull skull = new WitherSkull(level, player, look);
        skull.moveTo(spawnPos.x, spawnPos.y, spawnPos.z, player.getYRot(), player.getXRot());

        // Adjust velocity and accuracy
        skull.shoot(look.x, look.y, look.z, 1.5F, 0.0F);

        // Spawn it
        level.addFreshEntity(skull);
    }

    @Override
    public Item getIcon() {
        return Items.WITHER_SKELETON_SKULL;
    }
}
