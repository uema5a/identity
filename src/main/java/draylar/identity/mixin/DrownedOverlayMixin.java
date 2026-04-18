package draylar.identity.mixin;

import net.minecraft.client.renderer.entity.layers.DrownedOuterLayer;
import org.spongepowered.asm.mixin.Mixin;

// TODO: Phase 6 - Fix render method injection (method signature has changed in 26.1)
@Mixin(DrownedOuterLayer.class)
public abstract class DrownedOverlayMixin {
}
