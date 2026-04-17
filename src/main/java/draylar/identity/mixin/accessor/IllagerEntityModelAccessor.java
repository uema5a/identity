package draylar.identity.mixin.accessor;

import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.monster.illager.IllagerModel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(IllagerModel.class)
public interface IllagerEntityModelAccessor {
    @Accessor
    ModelPart getRightArm();
}
