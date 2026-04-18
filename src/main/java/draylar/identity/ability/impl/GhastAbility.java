package draylar.identity.ability.impl;

import draylar.identity.ability.IdentityAbility;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.monster.Ghast;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.hurtingprojectile.LargeFireball;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class GhastAbility extends IdentityAbility<Ghast> {

    @Override
    public void onUse(Player player, Ghast identity, Level level) {
        if (level.isClientSide) {
            return;
        }

        Vec3 look = player.getLookAngle();

        // Position the fireball slightly forward and at head height
        Vec3 spawnPos = player.getEyePosition().add(look.scale(4.0));

        LargeFireball fireball = new LargeFireball(
                level,
                player,
                look,
                1 // explosion power (same as ghast)
        );

        // Move the fireball to appear at mouth level
        fireball.moveTo(spawnPos.x, spawnPos.y, spawnPos.z, player.getYRot(), player.getXRot());
        fireball.setOwner(player);

        level.addFreshEntity(fireball);

        // Ghast sound effects
        level.playSound(null, player, SoundEvents.GHAST_SHOOT, SoundSource.HOSTILE, 10.0F, 1.0F);
        level.playSound(null, player, SoundEvents.GHAST_WARN, SoundSource.HOSTILE, 10.0F, 1.0F);
    }

    @Override
    public Item getIcon() {
        return Items.FIRE_CHARGE;
    }
}
