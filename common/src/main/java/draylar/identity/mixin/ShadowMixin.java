package draylar.identity.mixin;

import net.minecraft.client.renderer.entity.EntityRenderer;
import org.spongepowered.asm.mixin.Mixin;

// TODO: Port shadow rendering fix to MC 26.1 - EntityRenderer generics changed
@Mixin(EntityRenderer.class)
public class ShadowMixin {
}
