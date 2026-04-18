package draylar.identity.mixin;

import net.minecraft.client.model.HumanoidModel;
import org.spongepowered.asm.mixin.Mixin;

// MC 26.1 propagates isCrouching automatically via HumanoidMobRenderer.extractHumanoidRenderState,
// and identity pose is already synced by PlayerEntityRendererMixin. Mixin removed from JSON.
@Mixin(HumanoidModel.class)
public class BipedEntityModelMixin {
}
