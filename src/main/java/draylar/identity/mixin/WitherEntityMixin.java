package draylar.identity.mixin;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;

// TODO Phase D: port to 26.1 - LVT in customServerAiStep changed (now has 2 ints before List), needs re-capture
@Mixin(WitherBoss.class)
public abstract class WitherEntityMixin extends Monster {

    private WitherEntityMixin(EntityType<? extends Monster> entityType, Level level) {
        super(entityType, level);
    }
}
