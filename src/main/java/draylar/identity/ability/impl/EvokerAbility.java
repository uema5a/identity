package draylar.identity.ability.impl;

import draylar.identity.ability.IdentityAbility;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.monster.EvokerFangs;
import net.minecraft.world.entity.monster.illager.Evoker;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class EvokerAbility extends IdentityAbility<Evoker> {

    @Override
    public void onUse(Player player, Evoker identity, Level level) {
        // Spawn 8 Evoker Fangs out from the player.
        Vec3 origin = player.position();
        Vec3 facing = player.getLookAngle().multiply(1, 0, 1); // fangs should not go up/down based on pitch

        // Iterate out 5 blocks
        for (int blockOut = 0; blockOut < 8; blockOut++) {
            origin = origin.add(facing); // we add at the start -- no need to put a fang directly underneath the player!

            // Spawn an Evoker Fang at the given position.
            // For each position, we go up or down at most -+1 block per iteration.
            // If we cannot go up or down 1 block (or stay at the same level), the chain ends.

            // If the block underneath is solid, we are good to go.
            EvokerFangs fangs = new EvokerFangs(level, origin.x, origin.y, origin.z, player.getYRot(), blockOut * 2, player);
            BlockPos underneathPosition = new BlockPos((int) origin.x, (int) origin.y, (int) origin.z).below();
            BlockState underneath = level.getBlockState(underneathPosition);
            if (underneath.isFaceSturdy(level, underneathPosition, Direction.UP) && level.isEmptyBlock(underneathPosition.above())) {
                level.addFreshEntity(fangs);
                continue;
            }

            // Check underneath (2x down) again...
            BlockPos underneath2Position = new BlockPos((int) origin.x, (int) origin.y, (int) origin.z).below(2);
            BlockState underneath2 = level.getBlockState(underneath2Position);
            if (underneath2.isFaceSturdy(level, underneath2Position, Direction.UP) && level.isEmptyBlock(underneath2Position.above())) {
                fangs.moveTo(fangs.getX(), fangs.getY() - 1, fangs.getZ());
                level.addFreshEntity(fangs);
                origin = origin.add(0, -1, 0);
                continue;
            }

            // Check above (1x up)
            BlockPos upPosition = new BlockPos((int) origin.x, (int) origin.y, (int) origin.z).above();
            BlockState up = level.getBlockState(underneath2Position);
            if (up.isFaceSturdy(level, upPosition, Direction.UP) && level.isEmptyBlock(upPosition)) {
                fangs.moveTo(fangs.getX(), fangs.getY() + 1, fangs.getZ());
                level.addFreshEntity(fangs);
                origin = origin.add(0, 1, 0);
                continue;
            }

            break;
        }
    }

    @Override
    public Item getIcon() {
        return Items.EMERALD;
    }
}
