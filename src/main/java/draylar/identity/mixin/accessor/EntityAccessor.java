package draylar.identity.mixin.accessor;

import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(Entity.class)
public interface EntityAccessor {
    @Accessor
    void setWasTouchingWater(boolean wasTouchingWater);

    @Accessor
    void setVehicle(Entity vehicle);

    @Invoker("setSharedFlag")
    void identity_callSetFlag(int index, boolean value);
}
