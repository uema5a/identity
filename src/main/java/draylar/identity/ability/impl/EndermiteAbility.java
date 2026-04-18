package draylar.identity.ability.impl;

import draylar.identity.ability.IdentityAbility;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.monster.Endermite;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

public class EndermiteAbility extends IdentityAbility<Endermite> {

    @Override
    public void onUse(Player player, Endermite identity, Level level) {
        if (level.isClientSide()) {
            return;
        }

        double startX = player.getX();
        double startY = player.getY();
        double startZ = player.getZ();

        if (player.isPassenger()) {
            player.stopRiding();
        }

        for (int i = 0; i < 16; ++i) {
            // Random target position around the player
            double targetX = startX + (player.getRandom().nextDouble() - 0.5D) * 16.0D;
            double targetY = Mth.clamp(startY + (player.getRandom().nextInt(16) - 8), level.getMinY(), level.getMaxY() - 1);
            double targetZ = startZ + (player.getRandom().nextDouble() - 0.5D) * 16.0D;

            // Try teleporting; returns true if success
            if (player.randomTeleport(targetX, targetY, targetZ, true)) {
                // Departure sound
                level.playSound(null, startX, startY, startZ, SoundEvents.CHORUS_FRUIT_TELEPORT, SoundSource.PLAYERS, 1.0F, 1.0F);

                // Arrival sound
                player.playSound(SoundEvents.CHORUS_FRUIT_TELEPORT, 1.0F, 1.0F);
                break;
            }
        }
    }

    @Override
    public Item getIcon() {
        return Items.CHORUS_FRUIT;
    }
}
