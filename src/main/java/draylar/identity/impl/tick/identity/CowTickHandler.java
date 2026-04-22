package draylar.identity.impl.tick.identity;

import draylar.identity.api.IdentityTickHandler;
import net.minecraft.world.entity.animal.cow.Cow;
import net.minecraft.world.entity.player.Player;

public class CowTickHandler implements IdentityTickHandler<Cow> {

    @Override
    public void tick(Player player, Cow cow) {
        // No per-tick logic; Cow effects are handled via mixins.
    }
}
