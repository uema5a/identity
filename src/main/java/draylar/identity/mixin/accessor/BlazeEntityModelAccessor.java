package draylar.identity.mixin.accessor;

import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.monster.blaze.BlazeModel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(BlazeModel.class)
public interface BlazeEntityModelAccessor {
    @Accessor("upperBodyParts")
    ModelPart[] getRods();
}
