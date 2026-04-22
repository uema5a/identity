package draylar.identity.impl.tick.identity;

import draylar.identity.api.model.EntityUpdater;
import net.minecraft.world.entity.animal.axolotl.Axolotl;
import net.minecraft.world.entity.player.Player;

/**
 * Drives Axolotl's modern AnimationState / BinaryAnimator system off the player's real-world
 * state, since the dummy identity entity is never ticked by the world.
 *
 * <p>Without this, AxolotlRenderer reads all-zero factors and stopped AnimationStates from
 * the identity entity, resulting in a completely static pose.
 */
public class AxolotlUpdater implements EntityUpdater<Axolotl> {

    @Override
    public void update(Player player, Axolotl axolotl) {
        boolean inWater = player.isInWater();
        boolean onGround = player.onGround();
        // walkAnimation.speed() smoothly reflects the player's horizontal motion intensity.
        boolean moving = player.walkAnimation.speed() > 0.01F;

        axolotl.inWaterAnimator.tick(inWater);
        axolotl.onGroundAnimator.tick(onGround);
        axolotl.movingAnimator.tick(moving);

        // tickCount is synced to player.tickCount every frame by PlayerEntityRendererMixin.identity_syncPlayerToIdentity,
        // so AnimationState progression uses the player's monotonic clock.
        int t = axolotl.tickCount;

        axolotl.swimAnimationState.animateWhen(inWater && moving, t);
        axolotl.walkUnderWaterAnimationState.animateWhen(inWater && moving, t);
        axolotl.idleUnderWaterAnimationState.animateWhen(inWater && !moving && !onGround, t);
        axolotl.idleUnderWaterOnGroundAnimationState.animateWhen(inWater && !moving && onGround, t);
        axolotl.walkAnimationState.animateWhen(!inWater && moving, t);
        axolotl.idleOnGroundAnimationState.animateWhen(!inWater && !moving, t);
    }
}
