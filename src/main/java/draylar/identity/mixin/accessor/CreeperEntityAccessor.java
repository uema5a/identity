package draylar.identity.mixin.accessor;

import net.minecraft.world.entity.monster.Creeper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Creeper.class)
public interface CreeperEntityAccessor {
    @Accessor("swell")
    void setCurrentFuseTime(int swell);
}
