package draylar.identity.command;

import com.mojang.brigadier.arguments.StringArgumentType;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import draylar.identity.api.PlayerIdentity;
import draylar.identity.config.IdentityConfig;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.npc.Villager;

import java.util.Map;

public class VillagerCommand {

    public static void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            dispatcher.register(
                    Commands.literal("identity_villager")
                            .requires(src -> true)
                            .then(Commands.literal("list")
                                    .executes(ctx -> {
                                        ServerPlayer player = ctx.getSource().getPlayer();
                                        Map<String, CompoundTag> map = PlayerIdentity.getVillagerIdentities(player);
                                        if (map.isEmpty()) {
                                            player.sendSystemMessage(Component.literal("You have no saved villager professions."));
                                            return 1;
                                        }
                                        player.sendSystemMessage(Component.literal("Saved villager professions:"));
                                        map.forEach((name, tag) -> {
                                            String prof = tag.getString("ProfessionId");
                                            String dim = tag.getString("WorkstationDim");
                                            long posLong = tag.contains("WorkstationPos") ? tag.getLong("WorkstationPos") : Long.MIN_VALUE;
                                            BlockPos blockPos = posLong == Long.MIN_VALUE ? null : BlockPos.of(posLong);
                                            String location = blockPos == null ? "?" : (blockPos.getX() + ", " + blockPos.getY() + ", " + blockPos.getZ());
                                            player.sendSystemMessage(Component.literal("- " + name + " -> " + prof + " @ " + dim + " " + location));
                                        });
                                        return 1;
                                    }))
                            .then(Commands.literal("show")
                                    .then(Commands.argument("name", StringArgumentType.string())
                                            .executes(ctx -> {
                                                ServerPlayer player = ctx.getSource().getPlayer();
                                                String name = StringArgumentType.getString(ctx, "name");
                                                Map<String, CompoundTag> map = PlayerIdentity.getVillagerIdentities(player);
                                                if (!map.containsKey(name)) {
                                                    player.sendSystemMessage(Component.literal("No villager saved under name: " + name));
                                                    return 0;
                                                }
                                                CompoundTag tag = map.get(name);
                                                String prof = tag.getString("ProfessionId");
                                                String dim = tag.getString("WorkstationDim");
                                                long posLong = tag.contains("WorkstationPos") ? tag.getLong("WorkstationPos") : Long.MIN_VALUE;
                                                BlockPos blockPos = posLong == Long.MIN_VALUE ? null : BlockPos.of(posLong);
                                                String location = blockPos == null ? "?" : (blockPos.getX() + ", " + blockPos.getY() + ", " + blockPos.getZ());
                                                player.sendSystemMessage(Component.literal("Villager '" + name + "' profession: " + prof + " @ " + dim + " " + location));
                                                return 1;
                                            })))
                            .then(Commands.literal("trade")
                                    .requires(src -> src.hasPermission(Commands.LEVEL_GAMEMASTERS))
                                    .then(Commands.literal("myself")
                                            .executes(ctx -> {
                                                ServerPlayer player = ctx.getSource().getPlayer();
                                                if (!IdentityConfig.getInstance().allowSelfTrading()) {
                                                    player.sendSystemMessage(Component.translatable("identity.profession.trade.self_disabled"));
                                                    return 0;
                                                }

                                                LivingEntity identity = PlayerIdentity.getIdentity(player);
                                                if (!(identity instanceof Villager villager)) {
                                                    player.sendSystemMessage(Component.translatable("identity.profession.trade.require_villager"));
                                                    return 0;
                                                }

                                                villager.mobInteract(player, InteractionHand.MAIN_HAND);
                                                return 1;
                                            })))
            );
        });
    }

    private VillagerCommand() {
    }
}
