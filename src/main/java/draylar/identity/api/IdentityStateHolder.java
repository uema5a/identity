package draylar.identity.api;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

public interface IdentityStateHolder {
    LivingEntity identity$getCachedIdentity();
    void identity$setCachedIdentity(LivingEntity identity);

    Player identity$getCachedPlayer();
    void identity$setCachedPlayer(Player player);

    float identity$getCachedPartialTick();
    void identity$setCachedPartialTick(float tick);
}
