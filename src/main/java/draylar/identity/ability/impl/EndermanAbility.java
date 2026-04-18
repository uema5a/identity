package draylar.identity.ability.impl;

import draylar.identity.ability.IdentityAbility;
import draylar.identity.config.IdentityConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class EndermanAbility extends IdentityAbility<EnderMan> {

    @Override
    public void onUse(Player player, EnderMan identity, Level level) {
        if (level.isClientSide()) {
            return;
        }

        double maxDistance = IdentityConfig.getInstance().endermanAbilityTeleportDistance();
        HitResult hit = player.pick(maxDistance, 0, true);
        Vec3 targetPos = hit.getLocation();

        // Point de base converti en BlockPos
        BlockPos blockPos = BlockPos.containing(targetPos);

        // Monte tant que le bloc est solide pour éviter de téléporter dans un bloc
        while (!isSafeTeleportSpot(level, blockPos) && blockPos.getY() < level.getMaxY()) {
            blockPos = blockPos.above();
        }

        Vec3 safePos = Vec3.atCenterOf(blockPos);

        // Son départ
        level.playSound(
                null,
                player.getX(),
                player.getY(),
                player.getZ(),
                SoundEvents.ENDERMAN_TELEPORT,
                SoundSource.PLAYERS,
                1.0F,
                1.0F
        );

        // Téléportation
        player.teleportTo(safePos.x, safePos.y, safePos.z);

        // Son arrivée
        level.playSound(
                null,
                safePos.x,
                safePos.y,
                safePos.z,
                SoundEvents.ENDERMAN_TELEPORT,
                SoundSource.PLAYERS,
                1.0F,
                1.0F
        );
    }

    private boolean isSafeTeleportSpot(Level level, BlockPos pos) {
        BlockState blockAtFeet = level.getBlockState(pos);
        BlockState blockAtHead = level.getBlockState(pos.above());
        return blockAtFeet.isAir() && blockAtHead.isAir();
    }

    @Override
    public Item getIcon() {
        return Items.ENDER_PEARL;
    }
}
