package draylar.identity.mixin;

import draylar.identity.api.PlayerIdentity;
import draylar.identity.config.IdentityConfig;
import draylar.identity.impl.DimensionsRefresher;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.server.players.PlayerList;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerList.class)
public class PlayerManagerMixin {

    @Inject(
            method = "respawn",
            at = @At("RETURN")
    )
    private void onRespawn(ServerPlayer player, boolean alive, Entity.RemovalReason reason, CallbackInfoReturnable<ServerPlayer> cir) {
        LivingEntity identity = PlayerIdentity.getIdentity(player);

        ((DimensionsRefresher) player).identity_refreshDimensions();

        if(identity != null && IdentityConfig.getInstance().scalingHealth()) {
            player.setHealth(Math.min(player.getHealth(), identity.getMaxHealth()));
            player.getAttribute(Attributes.MAX_HEALTH).setBaseValue(Math.min(IdentityConfig.getInstance().maxHealth(), identity.getMaxHealth()));
        }
    }
}
