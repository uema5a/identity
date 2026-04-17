package draylar.identity.mixin.accessor;

import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.animal.llama.LlamaModel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(LlamaModel.class)
public interface LlamaEntityModelAccessor {
    @Accessor
    ModelPart getRightFrontLeg();
}
