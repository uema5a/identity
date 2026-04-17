package draylar.identity.api;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

/**
 * Shared render cache for communicating between the PlayerEntityRendererMixin
 * (which runs during {@code extractRenderState} on {@code AvatarRenderer}) and
 * the LivingEntitySubmitMixin (which runs during {@code submit} on {@code LivingEntityRenderer}).
 *
 * <p>Since rendering is single-threaded, simple static fields are safe here.
 * This class lives outside the mixin package so it can be referenced directly
 * by mixin classes without triggering IllegalClassLoadError.</p>
 */
public final class IdentityRenderCache {

    public static LivingEntity cachedIdentity;
    public static Player cachedPlayer;
    public static float cachedPartialTick;

    private IdentityRenderCache() {}
}
