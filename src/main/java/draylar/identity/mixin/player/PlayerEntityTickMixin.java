package draylar.identity.mixin.player;

import draylar.identity.Identity;
import draylar.identity.api.FlightHelper;
import draylar.identity.api.IdentityTickHandler;
import draylar.identity.api.IdentityTickHandlers;
import draylar.identity.api.PlayerAbilities;
import draylar.identity.api.PlayerIdentity;
import draylar.identity.config.IdentityConfig;
import draylar.identity.impl.PlayerDataProvider;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Player.class)
public abstract class PlayerEntityTickMixin extends LivingEntity {

    private PlayerEntityTickMixin(EntityType<? extends LivingEntity> type, Level level) {
        super(type, level);
    }

    @SuppressWarnings({"unchecked", "rawtypes", "ConstantConditions"})
    @Inject(method = "tick", at = @At("HEAD"))
    private void serverTick(CallbackInfo info) {
        // Tick IdentityTickHandlers on the client & server.
        @Nullable LivingEntity identity = PlayerIdentity.getIdentity((Player) (Object) this);
        if (identity != null) {
            @Nullable IdentityTickHandler handler = IdentityTickHandlers.getHandlers().get(identity.getType());
            if (handler != null) {
                handler.tick((Player) (Object) this, identity);
            }
        }

        // Update misc. server-side entity properties for the player.
        if (!level().isClientSide()) {
            PlayerDataProvider data = (PlayerDataProvider) this;
            data.setRemainingHostilityTime(Math.max(0, data.getRemainingHostilityTime() - 1));

            // Update cooldown & Sync
            ServerPlayer player = (ServerPlayer) (Object) this;
            PlayerAbilities.setCooldown(player, Math.max(0, data.getAbilityCooldown() - 1));
            PlayerAbilities.sync(player);

            // Sync flight abilities with identity state
            boolean shouldAllowFlight = Identity.hasFlyingPermissions(player);
            if (shouldAllowFlight != player.getAbilities().mayfly) {
                if (shouldAllowFlight) {
                    FlightHelper.grantFlightTo(player);
                    player.getAbilities().setFlyingSpeed(IdentityConfig.getInstance().flySpeed());
                } else {
                    FlightHelper.revokeFlight(player);
                    player.getAbilities().setFlyingSpeed(0.05f);
                }
                player.onUpdateAbilities();
            }

            // Validate villager profession bindings periodically
            draylar.identity.profession.ProfessionLifecycle.tickValidate(player, player.tickCount);
        }
    }
}
