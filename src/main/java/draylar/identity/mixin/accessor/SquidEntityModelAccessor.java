package draylar.identity.mixin.accessor;

import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.animal.squid.SquidModel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(SquidModel.class)
public interface SquidEntityModelAccessor {
    @Accessor
    ModelPart[] getTentacles();
}
