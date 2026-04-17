package draylar.identity.ability.impl;

import draylar.identity.ability.IdentityAbility;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.monster.Blaze;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.hurtingprojectile.SmallFireball;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class BlazeAbility extends IdentityAbility<Blaze> {

    @Override
    public void onUse(Player player, Blaze identity, Level level) {
        if (!level.isClientSide) {
            Vec3 look = player.getLookAngle();
            Vec3 spawnPos = player.getEyePosition().add(look.scale(0.6)); // near head, slightly forward
            Vec3 velocity = look.scale(0.5); // optional: slower for vanilla-like feel

            SmallFireball smallFireball = new SmallFireball(
                    level,
                    spawnPos.x,
                    spawnPos.y,
                    spawnPos.z,
                    velocity.x,
                    velocity.y,
                    velocity.z
            );

            smallFireball.setOwner(player);
            level.addFreshEntity(smallFireball);

            level.playSound(
                    null,
                    player,
                    SoundEvents.BLAZE_SHOOT,
                    SoundSource.HOSTILE,
                    2.0F,
                    (level.random.nextFloat() - level.random.nextFloat()) * 0.2F + 1.0F
            );
        }
    }

    @Override
    public Item getIcon() {
        return Items.BLAZE_POWDER;
    }
}
