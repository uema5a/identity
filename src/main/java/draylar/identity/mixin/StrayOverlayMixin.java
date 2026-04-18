package draylar.identity.mixin;

import net.minecraft.client.renderer.entity.layers.SkeletonClothingLayer;
import org.spongepowered.asm.mixin.Mixin;

// TODO: Phase 6 - Fix render method injection (method signature has changed in 26.1 with render states)
@Mixin(SkeletonClothingLayer.class)
public abstract class StrayOverlayMixin {
}
