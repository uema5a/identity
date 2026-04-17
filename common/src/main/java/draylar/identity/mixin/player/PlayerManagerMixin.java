package draylar.identity.mixin.player;

import draylar.identity.api.PlayerFavorites;
import draylar.identity.api.PlayerIdentity;
import draylar.identity.api.PlayerUnlocks;
import net.minecraft.world.entity.Entity;
import net.minecraft.server.players.PlayerList;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerList.class)
public class PlayerManagerMixin {

    @Inject(method = "respawn", at = @At(value = "RETURN"))
    private void sendResyncPacketOnRespawn(ServerPlayer player, boolean alive, Entity.RemovalReason reason, CallbackInfoReturnable<ServerPlayer> cir) {
        PlayerUnlocks.sync(player);
        PlayerFavorites.sync(player);
        PlayerIdentity.sync(player);
    }
}
