package draylar.identity.mixin;

import com.mojang.authlib.GameProfile;
import draylar.identity.Identity;
import draylar.identity.api.PlayerIdentity;
import draylar.identity.api.PlayerUnlocks;
import draylar.identity.api.FlightHelper;
import draylar.identity.config.IdentityConfig;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayer.class)
public abstract class ServerPlayerEntityMixin extends Player {

    public ServerPlayerEntityMixin(Level level, GameProfile gameProfile) {
        super(level, gameProfile);
    }

    @Inject(
            method = "die",
            at = @At("HEAD")
    )
    private void revokeIdentityOnDeath(DamageSource source, CallbackInfo ci) {
        if (IdentityConfig.getInstance().revokeIdentityOnDeath() && !this.isCreative() && !this.isSpectator()) {
            LivingEntity entity = PlayerIdentity.getIdentity(this);

            // revoke the identity current equipped by the player
            if(entity != null) {
                EntityType<?> type = entity.getType();
                PlayerUnlocks.revoke((ServerPlayer) (Object) this, PlayerIdentity.getIdentityType(this));
                PlayerIdentity.updateIdentity((ServerPlayer) (Object) this, null,null);

                // todo: this option might be server-only given that this method isn't[?] called on the client
                // send revoke message to player if they aren't in creative and the config option is on
                if(IdentityConfig.getInstance().overlayIdentityRevokes()) {
                    ((ServerPlayer) (Object) this).sendSystemMessage(
                            Component.translatable(
                                    "identity.revoke_entity",
                                    type.getDescriptionId()
                            ), true
                    );
                }
            }
        }
    }

    @Inject(
            method = "initInventoryMenu",
            at = @At("HEAD")
    )
    private void onSpawn(CallbackInfo ci) {
        ServerPlayer player = (ServerPlayer) (Object) this;
        if(Identity.hasFlyingPermissions(player)) {
            if(!FlightHelper.hasFlight(player)) {
                FlightHelper.grantFlightTo(player);
                getAbilities().setFlyingSpeed(IdentityConfig.getInstance().flySpeed());
                onUpdateAbilities();
            }

            FlightHelper.grantFlightTo(player);
        }
    }

    @Inject(method = "copyFrom", at = @At("RETURN"))
    private void identity$restoreAfterRespawn(ServerPlayer oldPlayer, boolean alive, CallbackInfo ci) {
        PlayerIdentity.sync((ServerPlayer) (Object) this);
    }
}
