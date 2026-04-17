package draylar.identity.mixin.accessor;

import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.monster.spider.SpiderModel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(SpiderModel.class)
public interface SpiderEntityModelAccessor {
    @Accessor
    ModelPart getRightFrontLeg();
}
