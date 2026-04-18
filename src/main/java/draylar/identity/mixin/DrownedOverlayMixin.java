package draylar.identity.mixin;

import net.minecraft.client.renderer.entity.layers.DrownedOuterLayer;
import org.spongepowered.asm.mixin.Mixin;

// Obsolete in MC 26.1: DrownedOuterLayer.submit() calls coloredCutoutModelCopyLayerRender(),
// which propagates all model state (including pose/crouching) from the parent via the
// ZombieRenderState already populated by the main renderer's extractRenderState. The old
// manual copyBipedStateTo + sneaking flag is no longer needed. Entry removed from
// identity.mixins.json.
@Mixin(DrownedOuterLayer.class)
public abstract class DrownedOverlayMixin {
}
