package draylar.identity.mixin;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;

// TODO: Port sonic boom functionality to MC 26.1 - many API changes needed
@Mixin(Player.class)
public abstract class PlayerSonicBoomMixin extends LivingEntity {

    private PlayerSonicBoomMixin(EntityType<? extends LivingEntity> type, Level level) {
        super(type, level);
    }
}
