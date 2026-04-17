package draylar.identity.mixin;

import net.minecraft.world.entity.WalkAnimationState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(WalkAnimationState.class)
public interface LimbAnimatorAccessor {

    @Accessor("speedOld")
    float getPrevSpeed();

    @Accessor("speedOld")
    void setPrevSpeed(float prevSpeed);

    @Accessor
    float getSpeed();

    @Accessor
    void setSpeed(float speed);

    @Accessor("position")
    float getPos();

    @Accessor("position")
    void setPos(float pos);
}
