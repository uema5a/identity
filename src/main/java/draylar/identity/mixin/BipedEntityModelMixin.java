package draylar.identity.mixin;

import net.minecraft.client.model.HumanoidModel;
import org.spongepowered.asm.mixin.Mixin;

// TODO: Port biped model crouching fix to MC 26.1 - model system uses RenderState now
@Mixin(HumanoidModel.class)
public class BipedEntityModelMixin {
}
