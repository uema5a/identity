package draylar.identity.ability.impl;

import draylar.identity.ability.IdentityAbility;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.hurtingprojectile.DragonFireball;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class EnderDragonAbility extends IdentityAbility<EnderDragon> {

    @Override
    public void onUse(Player player, EnderDragon identity, Level level) {
        if (!level.isClientSide()) {
            Vec3 look = player.getLookAngle();
            Vec3 velocity = look.scale(0.5); // control speed
            Vec3 spawnPos = player.getEyePosition().add(look.scale(2.0)); // mouth-level offset

            DragonFireball dragonFireball = new DragonFireball(level, player, velocity);

            // Manually move fireball to spawn in front of the player's head
            dragonFireball.setPos(spawnPos.x, spawnPos.y, spawnPos.z);
            dragonFireball.setOwner(player);

            level.addFreshEntity(dragonFireball);

            level.playSound(
                    null,
                    player,
                    SoundEvents.ENDER_DRAGON_SHOOT,
                    SoundSource.HOSTILE,
                    3.0F,
                    1.0F
            );
        }
    }

    @Override
    public Item getIcon() {
        return Items.DRAGON_BREATH;
    }
}
