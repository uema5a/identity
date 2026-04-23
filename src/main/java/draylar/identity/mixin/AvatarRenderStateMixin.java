package draylar.identity.mixin;

import draylar.identity.api.IdentityStateHolder;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(AvatarRenderState.class)
public class AvatarRenderStateMixin implements IdentityStateHolder {
    @Unique private LivingEntity identity$cachedIdentity;
    @Unique private Player identity$cachedPlayer;
    @Unique private float identity$cachedPartialTick;

    @Override public LivingEntity identity$getCachedIdentity() { return identity$cachedIdentity; }
    @Override public void identity$setCachedIdentity(LivingEntity id) { identity$cachedIdentity = id; }
    @Override public Player identity$getCachedPlayer() { return identity$cachedPlayer; }
    @Override public void identity$setCachedPlayer(Player p) { identity$cachedPlayer = p; }
    @Override public float identity$getCachedPartialTick() { return identity$cachedPartialTick; }
    @Override public void identity$setCachedPartialTick(float t) { identity$cachedPartialTick = t; }
}
