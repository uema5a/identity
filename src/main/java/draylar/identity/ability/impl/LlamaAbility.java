package draylar.identity.ability.impl;

import draylar.identity.ability.IdentityAbility;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.equine.Llama;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.LlamaSpit;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class LlamaAbility<T extends Llama> extends IdentityAbility<T> {

    @Override
    public void onUse(Player player, Llama identity, Level level) {
        if (level.isClientSide()) {
            return;
        }

        Vec3 look = player.getLookAngle();

        // Create and configure the spit
        LlamaSpit spit = new LlamaSpit(EntityType.LLAMA_SPIT, level);
        spit.setOwner(player);

        // Spawn position: a bit in front of the face to prevent self-hit
        Vec3 spawnPos = player.getEyePosition().add(look);
        spit.setPos(spawnPos.x, spawnPos.y, spawnPos.z);

        // Set trajectory and speed
        spit.shoot(look.x, look.y, look.z, 1.5F, 10.0F);

        // Play llama spit sound
        level.playSound(
                null,
                player.getX(),
                player.getY(),
                player.getZ(),
                SoundEvents.LLAMA_SPIT,
                player.getSoundSource(),
                1.0F,
                1.0F + (level.getRandom().nextFloat() - level.getRandom().nextFloat()) * 0.2F
        );

        // Spawn entity
        level.addFreshEntity(spit);
    }

    @Override
    public Item getIcon() {
        return Items.LEAD;
    }
}
