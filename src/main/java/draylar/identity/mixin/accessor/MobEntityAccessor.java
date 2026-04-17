package draylar.identity.mixin.accessor;

import net.minecraft.world.entity.Mob;
import net.minecraft.sounds.SoundEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(Mob.class)
public interface MobEntityAccessor {
    @Invoker
    SoundEvent callGetAmbientSound();
}
