package draylar.identity.mixin;

import net.minecraft.client.renderer.entity.layers.SkeletonClothingLayer;
import org.spongepowered.asm.mixin.Mixin;

// In 1.20.1 this mixin injected at render() HEAD on StrayOverlayFeatureRenderer to copy biped pose
// state (sneaking, arm poses) from the context model into the overlay's inner SkeletonEntityModel.
// In MC 26.1 the renderer pipeline switched to RenderState: all pose/state is captured once in
// extractRenderState() and passed into setupAnim(), so SkeletonClothingLayer receives the correct
// state automatically. No injection is needed; this mixin is retained as a clean no-op so that
// the class is not accidentally re-introduced with stale 1.20.1 logic.
@Mixin(SkeletonClothingLayer.class)
public abstract class StrayOverlayMixin {
}
