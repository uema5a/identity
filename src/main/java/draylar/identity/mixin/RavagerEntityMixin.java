package draylar.identity.mixin;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Ravager;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;

// TODO: Port ravager attack functionality to MC 26.1 - many API changes needed
@Mixin(Ravager.class)
public abstract class RavagerEntityMixin extends net.minecraft.world.entity.Mob {

    private RavagerEntityMixin(EntityType<? extends net.minecraft.world.entity.Mob> type, Level level) {
        super(type, level);
    }
}
