package draylar.identity.mixin.accessor;

import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.QuadrupedModel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(QuadrupedModel.class)
public interface QuadrupedEntityModelAccessor {
    @Accessor
    ModelPart getRightFrontLeg();
}
