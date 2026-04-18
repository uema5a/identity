package draylar.identity.command;

import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import draylar.identity.api.PlayerIdentity;
import draylar.identity.api.PlayerUnlocks;
import draylar.identity.config.IdentityConfig;
import draylar.identity.api.variant.IdentityType;
import draylar.identity.screen.widget.EntityWidget;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.Commands;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.CompoundTagArgument;
import net.minecraft.commands.arguments.ResourceArgument;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.IntConsumer;
import java.util.function.Supplier;

public class IdentityCommand {

    private static final Map<String, Consumer<Boolean>> BOOLEAN_SETTERS = new LinkedHashMap<>();
    private static final Map<String, IntConsumer> INT_SETTERS = new LinkedHashMap<>();
    private static final Map<String, Consumer<Float>> FLOAT_SETTERS = new LinkedHashMap<>();
    private static final List<String> STRING_OPTIONS = new ArrayList<>();

    private static final SuggestionProvider<CommandSourceStack> BOOLEAN_OPTION_SUGGESTIONS = (context, builder) -> SharedSuggestionProvider.suggest(BOOLEAN_SETTERS.keySet(), builder);
    private static final SuggestionProvider<CommandSourceStack> INT_OPTION_SUGGESTIONS = (context, builder) -> SharedSuggestionProvider.suggest(INT_SETTERS.keySet(), builder);
    private static final SuggestionProvider<CommandSourceStack> FLOAT_OPTION_SUGGESTIONS = (context, builder) -> SharedSuggestionProvider.suggest(FLOAT_SETTERS.keySet(), builder);
    private static final SuggestionProvider<CommandSourceStack> STRING_OPTION_SUGGESTIONS = (context, builder) -> SharedSuggestionProvider.suggest(STRING_OPTIONS, builder);

    private static final SuggestionProvider<CommandSourceStack> FORCED_IDENTITY_SUGGESTIONS = (context, builder) -> {
        List<String> suggestions = new ArrayList<>();
        suggestions.add("none");
        BuiltInRegistries.ENTITY_TYPE.keySet().forEach(id -> suggestions.add(id.toString()));
        return SharedSuggestionProvider.suggest(suggestions, builder);
    };

    static {
        BOOLEAN_SETTERS.put("overlay_identity_unlocks", value -> IdentityConfig.getInstance().setOverlayIdentityUnlocks(value));
        BOOLEAN_SETTERS.put("overlay_identity_revokes", value -> IdentityConfig.getInstance().setOverlayIdentityRevokes(value));
        BOOLEAN_SETTERS.put("revoke_identity_on_death", value -> IdentityConfig.getInstance().setRevokeIdentityOnDeath(value));
        BOOLEAN_SETTERS.put("identities_equip_items", value -> IdentityConfig.getInstance().setIdentitiesEquipItems(value));
        BOOLEAN_SETTERS.put("identities_equip_armor", value -> IdentityConfig.getInstance().setIdentitiesEquipArmor(value));
        BOOLEAN_SETTERS.put("show_player_nametag", value -> IdentityConfig.getInstance().setShowPlayerNametag(value));
        BOOLEAN_SETTERS.put("render_own_nametag", value -> IdentityConfig.getInstance().setRenderOwnNameTag(value));
        BOOLEAN_SETTERS.put("hostiles_ignore_hostile_identity_player", value -> IdentityConfig.getInstance().setHostilesIgnoreHostileIdentityPlayer(value));
        BOOLEAN_SETTERS.put("hostiles_forget_new_hostile_identity_player", value -> IdentityConfig.getInstance().setHostilesForgetNewHostileIdentityPlayer(value));
        BOOLEAN_SETTERS.put("wolves_attack_identity_prey", value -> IdentityConfig.getInstance().setWolvesAttackIdentityPrey(value));
        BOOLEAN_SETTERS.put("owned_wolves_attack_identity_prey", value -> IdentityConfig.getInstance().setOwnedWolvesAttackIdentityPrey(value));
        BOOLEAN_SETTERS.put("villagers_run_from_identities", value -> IdentityConfig.getInstance().setVillagersRunFromIdentities(value));
        BOOLEAN_SETTERS.put("foxes_attack_identity_prey", value -> IdentityConfig.getInstance().setFoxesAttackIdentityPrey(value));
        BOOLEAN_SETTERS.put("use_identity_sounds", value -> IdentityConfig.getInstance().setUseIdentitySounds(value));
        BOOLEAN_SETTERS.put("play_ambient_sounds", value -> IdentityConfig.getInstance().setPlayAmbientSounds(value));
        BOOLEAN_SETTERS.put("hear_self_ambient", value -> IdentityConfig.getInstance().setHearSelfAmbient(value));
        BOOLEAN_SETTERS.put("enable_flight", value -> IdentityConfig.getInstance().setEnableFlight(value));
        BOOLEAN_SETTERS.put("enable_client_swap_menu", value -> IdentityConfig.getInstance().setEnableClientSwapMenu(value));
        BOOLEAN_SETTERS.put("enable_swaps", value -> IdentityConfig.getInstance().setEnableSwaps(value));
        BOOLEAN_SETTERS.put("allow_self_trading", value -> IdentityConfig.getInstance().setAllowSelfTrading(value));
        BOOLEAN_SETTERS.put("force_change_new", value -> IdentityConfig.getInstance().setForceChangeNew(value));
        BOOLEAN_SETTERS.put("force_change_always", value -> IdentityConfig.getInstance().setForceChangeAlways(value));
        BOOLEAN_SETTERS.put("log_commands", value -> IdentityConfig.getInstance().setLogCommands(value));
        BOOLEAN_SETTERS.put("kill_for_identity", value -> IdentityConfig.getInstance().setKillForIdentity(value));
        BOOLEAN_SETTERS.put("scaling_health", value -> IdentityConfig.getInstance().setScalingHealth(value));
        BOOLEAN_SETTERS.put("warden_is_blinded", value -> IdentityConfig.getInstance().setWardenIsBlinded(value));
        BOOLEAN_SETTERS.put("warden_blinds_nearby", value -> IdentityConfig.getInstance().setWardenBlindsNearby(value));

        INT_SETTERS.put("hostility_time", value -> IdentityConfig.getInstance().setHostilityTime(value));
        INT_SETTERS.put("max_health", value -> IdentityConfig.getInstance().setMaxHealth(value));
        INT_SETTERS.put("enderman_ability_teleport_distance", value -> IdentityConfig.getInstance().setEndermanAbilityTeleportDistance(value));
        INT_SETTERS.put("required_kills_for_identity", value -> IdentityConfig.getInstance().setRequiredKillsForIdentity(value));

        FLOAT_SETTERS.put("fly_speed", value -> IdentityConfig.getInstance().setFlySpeed(value));

        STRING_OPTIONS.add("forced_identity");
    }

    private static LiteralArgumentBuilder<CommandSourceStack> createListCommand(CommandBuildContext registryAccess) {
        LiteralArgumentBuilder<CommandSourceStack> listBuilder = Commands.literal("list");

        listBuilder.then(createStringListNode("allowed_swappers", () -> IdentityConfig.getInstance().allowedSwappers(), true, "player"));
        listBuilder.then(createStringListNode("advancements_required_for_flight", () -> IdentityConfig.getInstance().advancementsRequiredForFlight(), false, "advancement"));
        listBuilder.then(createEntityListNode("extra_aquatic_entities", () -> IdentityConfig.getInstance().extraAquaticEntities(), registryAccess));
        listBuilder.then(createEntityListNode("removed_aquatic_entities", () -> IdentityConfig.getInstance().removedAquaticEntities(), registryAccess));
        listBuilder.then(createEntityListNode("extra_flying_entities", () -> IdentityConfig.getInstance().extraFlyingEntities(), registryAccess));
        listBuilder.then(createEntityListNode("removed_flying_entities", () -> IdentityConfig.getInstance().removedFlyingEntities(), registryAccess));

        return listBuilder;
    }

    private static LiteralArgumentBuilder<CommandSourceStack> createMapCommand(CommandBuildContext registryAccess) {
        LiteralArgumentBuilder<CommandSourceStack> mapBuilder = Commands.literal("map");

        mapBuilder.then(createAbilityCooldownCommands(registryAccess));
        mapBuilder.then(createRequiredKillCommands(registryAccess));

        return mapBuilder;
    }

    private static LiteralArgumentBuilder<CommandSourceStack> createStringListNode(String literal, Supplier<List<String>> listSupplier, boolean caseInsensitive, String valueArgumentName) {
        return Commands.literal(literal)
                .then(Commands.literal("add")
                        .then(Commands.argument(valueArgumentName, StringArgumentType.string())
                                .executes(ctx -> addToList(ctx.getSource(), listSupplier, caseInsensitive, literal, StringArgumentType.getString(ctx, valueArgumentName)))))
                .then(Commands.literal("remove")
                        .then(Commands.argument(valueArgumentName, StringArgumentType.string())
                                .suggests((context, builder) -> SharedSuggestionProvider.suggest(new ArrayList<>(listSupplier.get()), builder))
                                .executes(ctx -> removeFromList(ctx.getSource(), listSupplier, caseInsensitive, literal, StringArgumentType.getString(ctx, valueArgumentName)))))
                .then(Commands.literal("clear")
                        .executes(ctx -> clearList(ctx.getSource(), listSupplier, literal)));
    }

    private static LiteralArgumentBuilder<CommandSourceStack> createEntityListNode(String literal, Supplier<List<String>> listSupplier, CommandBuildContext registryAccess) {
        return Commands.literal(literal)
                .then(Commands.literal("add")
                        .then(Commands.argument("entity", ResourceArgument.resource(registryAccess, Registries.ENTITY_TYPE))
                                .executes(ctx -> {
                                    Identifier id = ResourceArgument.getSummonableEntityType(ctx, "entity").key().identifier();
                                    return addToList(ctx.getSource(), listSupplier, false, literal, id.toString());
                                })))
                .then(Commands.literal("remove")
                        .then(Commands.argument("entity", StringArgumentType.string())
                                .suggests((context, builder) -> SharedSuggestionProvider.suggest(new ArrayList<>(listSupplier.get()), builder))
                                .executes(ctx -> removeFromList(ctx.getSource(), listSupplier, false, literal, StringArgumentType.getString(ctx, "entity")))))
                .then(Commands.literal("clear")
                        .executes(ctx -> clearList(ctx.getSource(), listSupplier, literal)));
    }

    private static LiteralArgumentBuilder<CommandSourceStack> createAbilityCooldownCommands(CommandBuildContext registryAccess) {
        return Commands.literal("ability_cooldowns")
                .then(Commands.literal("set")
                        .then(Commands.argument("entity", ResourceArgument.resource(registryAccess, Registries.ENTITY_TYPE))
                                .then(Commands.argument("cooldown", IntegerArgumentType.integer(0))
                                        .executes(ctx -> {
                                            Identifier id = ResourceArgument.getSummonableEntityType(ctx, "entity").key().identifier();
                                            int cooldown = IntegerArgumentType.getInteger(ctx, "cooldown");
                                            return setAbilityCooldown(ctx.getSource(), id.toString(), cooldown);
                                        }))))
                .then(Commands.literal("remove")
                        .then(Commands.argument("entity", StringArgumentType.string())
                                .suggests((context, builder) -> SharedSuggestionProvider.suggest(IdentityConfig.getInstance().getAbilityCooldownMap().keySet(), builder))
                                .executes(ctx -> removeAbilityCooldown(ctx.getSource(), StringArgumentType.getString(ctx, "entity")))))
                .then(Commands.literal("clear")
                        .executes(ctx -> clearAbilityCooldowns(ctx.getSource())));
    }

    private static LiteralArgumentBuilder<CommandSourceStack> createRequiredKillCommands(CommandBuildContext registryAccess) {
        return Commands.literal("required_kills")
                .then(Commands.literal("set")
                        .then(Commands.argument("entity", ResourceArgument.resource(registryAccess, Registries.ENTITY_TYPE))
                                .then(Commands.argument("kills", IntegerArgumentType.integer(0))
                                        .executes(ctx -> {
                                            Identifier id = ResourceArgument.getSummonableEntityType(ctx, "entity").key().identifier();
                                            int kills = IntegerArgumentType.getInteger(ctx, "kills");
                                            return setRequiredKillOverride(ctx.getSource(), id.toString(), kills);
                                        }))))
                .then(Commands.literal("remove")
                        .then(Commands.argument("entity", StringArgumentType.string())
                                .suggests((context, builder) -> SharedSuggestionProvider.suggest(IdentityConfig.getInstance().getRequiredKillsByType().keySet(), builder))
                                .executes(ctx -> removeRequiredKillOverride(ctx.getSource(), StringArgumentType.getString(ctx, "entity")))))
                .then(Commands.literal("clear")
                        .executes(ctx -> clearRequiredKillOverrides(ctx.getSource())));
    }

    private static int addToList(CommandSourceStack source, Supplier<List<String>> supplier, boolean caseInsensitive, String listName, String value) {
        List<String> list = supplier.get();
        boolean exists = caseInsensitive ? list.stream().anyMatch(entry -> entry.equalsIgnoreCase(value)) : list.contains(value);

        if (exists) {
            source.sendFailure(Component.literal(value + " is already present in " + formatKey(listName)));
            return 0;
        }

        list.add(value);
        persistConfig(source, Component.literal("Added " + value + " to " + formatKey(listName)));
        return 1;
    }

    private static int removeFromList(CommandSourceStack source, Supplier<List<String>> supplier, boolean caseInsensitive, String listName, String value) {
        List<String> list = supplier.get();
        boolean removed;

        if (caseInsensitive) {
            removed = list.removeIf(entry -> entry.equalsIgnoreCase(value));
        } else {
            removed = list.remove(value);
        }

        if (!removed) {
            source.sendFailure(Component.literal(value + " is not present in " + formatKey(listName)));
            return 0;
        }

        persistConfig(source, Component.literal("Removed " + value + " from " + formatKey(listName)));
        return 1;
    }

    private static int clearList(CommandSourceStack source, Supplier<List<String>> supplier, String listName) {
        List<String> list = supplier.get();

        if (list.isEmpty()) {
            source.sendSuccess(() -> Component.literal(formatKey(listName) + " is already empty"), false);
            return 0;
        }

        list.clear();
        persistConfig(source, Component.literal("Cleared " + formatKey(listName)));
        return 1;
    }

    private static int setAbilityCooldown(CommandSourceStack source, String entityId, int cooldown) {
        IdentityConfig.getInstance().getAbilityCooldownMap().put(entityId, cooldown);
        persistConfig(source, Component.literal("Set ability cooldown for " + entityId + " to " + cooldown));
        return 1;
    }

    private static int removeAbilityCooldown(CommandSourceStack source, String entityId) {
        Integer removed = IdentityConfig.getInstance().getAbilityCooldownMap().remove(entityId);
        if (removed == null) {
            source.sendFailure(Component.literal("No ability cooldown override exists for " + entityId));
            return 0;
        }

        persistConfig(source, Component.literal("Removed ability cooldown override for " + entityId));
        return 1;
    }

    private static int clearAbilityCooldowns(CommandSourceStack source) {
        Map<String, Integer> map = IdentityConfig.getInstance().getAbilityCooldownMap();
        if (map.isEmpty()) {
            source.sendSuccess(() -> Component.literal("Ability cooldown overrides are already empty"), false);
            return 0;
        }

        map.clear();
        persistConfig(source, Component.literal("Cleared all ability cooldown overrides"));
        return 1;
    }

    private static int setRequiredKillOverride(CommandSourceStack source, String entityId, int kills) {
        IdentityConfig.getInstance().getRequiredKillsByType().put(entityId, kills);
        persistConfig(source, Component.literal("Set required kills for " + entityId + " to " + kills));
        return 1;
    }

    private static int removeRequiredKillOverride(CommandSourceStack source, String entityId) {
        Integer removed = IdentityConfig.getInstance().getRequiredKillsByType().remove(entityId);
        if (removed == null) {
            source.sendFailure(Component.literal("No required kill override exists for " + entityId));
            return 0;
        }

        persistConfig(source, Component.literal("Removed required kill override for " + entityId));
        return 1;
    }

    private static int clearRequiredKillOverrides(CommandSourceStack source) {
        Map<String, Integer> map = IdentityConfig.getInstance().getRequiredKillsByType();
        if (map.isEmpty()) {
            source.sendSuccess(() -> Component.literal("Required kill overrides are already empty"), false);
            return 0;
        }

        map.clear();
        persistConfig(source, Component.literal("Cleared all required kill overrides"));
        return 1;
    }

    private static int setBooleanOption(CommandSourceStack source, String option, boolean value) {
        String key = option.toLowerCase(Locale.ROOT);
        Consumer<Boolean> setter = BOOLEAN_SETTERS.get(key);

        if (setter == null) {
            source.sendFailure(Component.literal("Unknown boolean option: " + option));
            return 0;
        }

        setter.accept(value);
        persistConfig(source, Component.literal("Set " + formatKey(key) + " to " + value));
        return 1;
    }

    private static int setIntegerOption(CommandSourceStack source, String option, int value) {
        String key = option.toLowerCase(Locale.ROOT);
        IntConsumer setter = INT_SETTERS.get(key);

        if (setter == null) {
            source.sendFailure(Component.literal("Unknown integer option: " + option));
            return 0;
        }

        if ("max_health".equals(key) && value < 1) {
            source.sendFailure(Component.literal("max health must be at least 1"));
            return 0;
        }

        setter.accept(value);
        persistConfig(source, Component.literal("Set " + formatKey(key) + " to " + value));
        return 1;
    }

    private static int setFloatOption(CommandSourceStack source, String option, float value) {
        String key = option.toLowerCase(Locale.ROOT);
        Consumer<Float> setter = FLOAT_SETTERS.get(key);

        if (setter == null) {
            source.sendFailure(Component.literal("Unknown float option: " + option));
            return 0;
        }

        if (value <= 0) {
            source.sendFailure(Component.literal("fly speed must be greater than 0"));
            return 0;
        }

        setter.accept(value);
        persistConfig(source, Component.literal("Set " + formatKey(key) + " to " + value));
        return 1;
    }

    private static int setStringOption(CommandSourceStack source, String option, String rawValue) {
        String key = option.toLowerCase(Locale.ROOT);

        if (!STRING_OPTIONS.contains(key)) {
            source.sendFailure(Component.literal("Unknown string option: " + option));
            return 0;
        }

        if ("forced_identity".equals(key)) {
            if (rawValue.equalsIgnoreCase("none") || rawValue.equalsIgnoreCase("null")) {
                IdentityConfig.getInstance().setForcedIdentity(null);
                persistConfig(source, Component.literal("Cleared forced identity"));
                return 1;
            }

            Identifier identifier = Identifier.tryParse(rawValue);
            if (identifier == null || !BuiltInRegistries.ENTITY_TYPE.containsKey(identifier)) {
                source.sendFailure(Component.literal("Unknown entity: " + rawValue));
                return 0;
            }

            IdentityConfig.getInstance().setForcedIdentity(identifier.toString());
            persistConfig(source, Component.literal("Set forced identity to " + identifier));
            return 1;
        }

        return 0;
    }

    private static int reloadConfig(CommandSourceStack source) {
        IdentityConfig.load();
        source.sendSuccess(() -> Component.literal("Reloaded Identity config"), true);
        return 1;
    }

    private static void persistConfig(CommandSourceStack source, Component message) {
        source.sendSuccess(() -> message, true);
        IdentityConfig.save();
    }

    private static String formatKey(String key) {
        return key.replace('_', ' ');
    }

    public static void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            LiteralCommandNode<CommandSourceStack> rootNode = Commands
                    .literal("identity")
                    .requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
                    .build();

            /*
            Used to give the specified Identity to the specified Player.
             */
            LiteralCommandNode<CommandSourceStack> grantNode = Commands
                    .literal("grant")
                    .then(Commands.argument("player", EntityArgument.players())
                            .then(Commands.literal("everything")
                                    .executes(context -> {
                                        ServerPlayer player = EntityArgument.getPlayer(context, "player");
                                        for (IdentityType<?> type : IdentityType.getAllTypes(player.level())) {
                                            if(!PlayerUnlocks.has(player, type)) {
                                                PlayerUnlocks.unlock(player, type);
                                            }
                                        }

                                        return 1;
                                    })
                            )
                            .then(Commands.argument("identity", ResourceArgument.resource(registryAccess, Registries.ENTITY_TYPE))
                                    .executes(context -> {
                                        grant(
                                                context.getSource().getPlayer(),
                                                EntityArgument.getPlayer(context, "player"),
                                                ResourceArgument.getSummonableEntityType(context, "identity").key().identifier(),
                                                null
                                        );
                                        return 1;
                                    })
                                    .then(Commands.argument("nbt", CompoundTagArgument.compoundTag())
                                            .executes(context -> {
                                                CompoundTag nbt = CompoundTagArgument.getCompoundTag(context, "nbt");

                                                grant(
                                                        context.getSource().getPlayer(),
                                                        EntityArgument.getPlayer(context, "player"),
                                                        ResourceArgument.getSummonableEntityType(context, "identity").key().identifier(),
                                                        nbt
                                                );

                                                return 1;
                                            })
                                    )
                            )
                    )
                    .build();

            LiteralCommandNode<CommandSourceStack> revokeNode = Commands
                    .literal("revoke")
                    .then(Commands.argument("player", EntityArgument.players())
                            .then(Commands.literal("everything")
                                    .executes(context -> {
                                        ServerPlayer player = EntityArgument.getPlayer(context, "player");
                                        for (IdentityType<?> type : IdentityType.getAllTypes(player.level())) {
                                            if(PlayerUnlocks.has(player, type)) {
                                                PlayerUnlocks.revoke(player, type);
                                            }
                                        }

                                        return 1;
                                    })
                            )
                            .then(Commands.argument("identity", ResourceArgument.resource(registryAccess, Registries.ENTITY_TYPE))
                                    .executes(context -> {
                                        revoke(
                                                context.getSource().getPlayer(),
                                                EntityArgument.getPlayer(context, "player"),
                                                ResourceArgument.getSummonableEntityType(context, "identity").key().identifier(),
                                                null
                                        );
                                        return 1;
                                    })
                                    .then(Commands.argument("nbt", CompoundTagArgument.compoundTag())
                                            .executes(context -> {
                                                CompoundTag nbt = CompoundTagArgument.getCompoundTag(context, "nbt");

                                                revoke(
                                                        context.getSource().getPlayer(),
                                                        EntityArgument.getPlayer(context, "player"),
                                                        ResourceArgument.getSummonableEntityType(context, "identity").key().identifier(),
                                                        nbt
                                                );

                                                return 1;
                                            })
                                    )
                            )
                    )
                    .build();

            LiteralCommandNode<CommandSourceStack> equip = Commands
                    .literal("equip")
                    .then(Commands.argument("player", EntityArgument.players())
                            .then(Commands.argument("identity", ResourceArgument.resource(registryAccess, Registries.ENTITY_TYPE))
                                    .executes(context -> {
                                        equip(context.getSource().getPlayer(),
                                                EntityArgument.getPlayer(context, "player"),
                                                ResourceArgument.getSummonableEntityType(context, "identity").key().identifier(),
                                                null);

                                        return 1;
                                    })
                                    .then(Commands.argument("nbt", CompoundTagArgument.compoundTag())
                                            .executes(context -> {
                                                CompoundTag nbt = CompoundTagArgument.getCompoundTag(context, "nbt");

                                                equip(context.getSource().getPlayer(),
                                                        EntityArgument.getPlayer(context, "player"),
                                                        ResourceArgument.getSummonableEntityType(context, "identity").key().identifier(),
                                                        nbt);

                                                return 1;
                                            })
                                    )
                            )
                    )
                    .build();

            LiteralCommandNode<CommandSourceStack> unequip = Commands
                    .literal("unequip")
                    .then(Commands.argument("player", EntityArgument.players())
                            .executes(context -> {
                                unequip(
                                        context.getSource().getPlayer(),
                                        EntityArgument.getPlayer(context, "player")
                                );
                                return 1;
                            })
                    )
                    .build();

            LiteralCommandNode<CommandSourceStack> test = Commands
                    .literal("test")
                    .then(Commands.argument("player", EntityArgument.player())
                            .then(Commands.literal("not")
                                    .then(Commands.argument("identity", ResourceArgument.resource(registryAccess, Registries.ENTITY_TYPE))
                                            .executes(context -> {
                                                return testNot(
                                                        context.getSource().getPlayer(),
                                                        EntityArgument.getPlayer(context, "player"),
                                                        ResourceArgument.getSummonableEntityType(context, "identity").key().identifier()
                                                );
                                            })
                                    )
                            )
                            .then(Commands.argument("identity", ResourceArgument.resource(registryAccess, Registries.ENTITY_TYPE))
                                    .executes(context -> {
                                        return test(
                                                context.getSource().getPlayer(),
                                                EntityArgument.getPlayer(context, "player"),
                                                ResourceArgument.getSummonableEntityType(context, "identity").key().identifier()
                                        );
                                    })
                            )
                    )
                    .build();

            LiteralCommandNode<CommandSourceStack> offsetNode =
                    Commands.literal("offset")
                            .then(Commands.argument("value", IntegerArgumentType.integer())
                                    .executes(ctx -> {
                                        int v = IntegerArgumentType.getInteger(ctx, "value");
                                        EntityWidget.VERTICAL_OFFSET = v;
                                        ctx.getSource()
                                                .sendSuccess(
                                                        () -> Component.literal("Entity-grid Y-offset set to §e" + v + "§r"),
                                                        false
                                                );
                                        return 1;
                                    })
                            ).build();

            LiteralCommandNode<CommandSourceStack> whitelistNode =
                    Commands.literal("whitelist")
                            .then(Commands.literal("enable")
                                    .executes(ctx -> {
                                        IdentityConfig.getInstance().setEnableSwaps(false);
                                        if (IdentityConfig.getInstance().logCommands()) {
                                            ctx.getSource().sendSuccess(() -> Component.literal("Enabled identity whitelist"), true);
                                        }
                                        return 1;
                                    }))
                            .then(Commands.literal("disable")
                                    .executes(ctx -> {
                                        IdentityConfig.getInstance().setEnableSwaps(true);
                                        if (IdentityConfig.getInstance().logCommands()) {
                                            ctx.getSource().sendSuccess(() -> Component.literal("Disabled identity whitelist"), true);
                                        }
                                        return 1;
                                    }))
                            .then(Commands.literal("add")
                                    .then(Commands.argument("player", StringArgumentType.string())
                                            .executes(ctx -> {
                                                String name = StringArgumentType.getString(ctx, "player");
                                                IdentityConfig.getInstance().allowedSwappers().add(name);
                                                if (IdentityConfig.getInstance().logCommands()) {
                                                    ctx.getSource().sendSuccess(() -> Component.literal("Added " + name + " to identity whitelist"), true);
                                                }
                                                return 1;
                                            })))
                            .then(Commands.literal("remove")
                                    .then(Commands.argument("player", StringArgumentType.string())
                                            .executes(ctx -> {
                                                String name = StringArgumentType.getString(ctx, "player");
                                                IdentityConfig.getInstance().allowedSwappers().removeIf(n -> n.equalsIgnoreCase(name));
                                                if (IdentityConfig.getInstance().logCommands()) {
                                                    ctx.getSource().sendSuccess(() -> Component.literal("Removed " + name + " from identity whitelist"), true);
                                                }
                                                return 1;
                                            })))
                            .build();

            LiteralArgumentBuilder<CommandSourceStack> configBuilder = Commands.literal("config")
                    .then(Commands.literal("boolean")
                            .then(Commands.argument("option", StringArgumentType.word()).suggests(BOOLEAN_OPTION_SUGGESTIONS)
                                    .then(Commands.argument("value", BoolArgumentType.bool())
                                            .executes(ctx -> setBooleanOption(ctx.getSource(), StringArgumentType.getString(ctx, "option"), BoolArgumentType.getBool(ctx, "value"))))))
                    .then(Commands.literal("integer")
                            .then(Commands.argument("option", StringArgumentType.word()).suggests(INT_OPTION_SUGGESTIONS)
                                    .then(Commands.argument("value", IntegerArgumentType.integer(0))
                                            .executes(ctx -> setIntegerOption(ctx.getSource(), StringArgumentType.getString(ctx, "option"), IntegerArgumentType.getInteger(ctx, "value"))))))
                    .then(Commands.literal("float")
                            .then(Commands.argument("option", StringArgumentType.word()).suggests(FLOAT_OPTION_SUGGESTIONS)
                                    .then(Commands.argument("value", FloatArgumentType.floatArg())
                                            .executes(ctx -> setFloatOption(ctx.getSource(), StringArgumentType.getString(ctx, "option"), FloatArgumentType.getFloat(ctx, "value"))))))
                    .then(Commands.literal("string")
                            .then(Commands.argument("option", StringArgumentType.word()).suggests(STRING_OPTION_SUGGESTIONS)
                                    .then(Commands.argument("value", StringArgumentType.greedyString()).suggests(FORCED_IDENTITY_SUGGESTIONS)
                                            .executes(ctx -> setStringOption(ctx.getSource(), StringArgumentType.getString(ctx, "option"), StringArgumentType.getString(ctx, "value"))))))
                    .then(createListCommand(registryAccess))
                    .then(createMapCommand(registryAccess))
                    .then(Commands.literal("reload")
                            .executes(ctx -> reloadConfig(ctx.getSource())));

            rootNode.addChild(grantNode);
            rootNode.addChild(revokeNode);
            rootNode.addChild(equip);
            rootNode.addChild(unequip);
            rootNode.addChild(test);
            rootNode.addChild(offsetNode);
            rootNode.addChild(whitelistNode);
            rootNode.addChild(configBuilder.build());

            dispatcher.getRoot().addChild(rootNode);
        });
    }

    private static int test(ServerPlayer source, ServerPlayer player, Identifier identity) {
        EntityType<?> type = BuiltInRegistries.ENTITY_TYPE.getValue(identity);

        if(PlayerIdentity.getIdentity(player) != null && PlayerIdentity.getIdentity(player).getType().equals(type)) {
            if(IdentityConfig.getInstance().logCommands()) {
                source.sendSystemMessage(Component.translatable("identity.test_positive", player.getDisplayName(), Component.translatable(type.getDescriptionId())), true);
            }

            return 1;
        }

        if(IdentityConfig.getInstance().logCommands()) {
            source.sendSystemMessage(Component.translatable("identity.test_failed", player.getDisplayName(), Component.translatable(type.getDescriptionId())), true);
        }

        return 0;
    }

    private static int testNot(ServerPlayer source, ServerPlayer player, Identifier identity) {
        EntityType<?> type = BuiltInRegistries.ENTITY_TYPE.getValue(identity);

        if(PlayerIdentity.getIdentity(player) != null && !PlayerIdentity.getIdentity(player).getType().equals(type)) {
            if(IdentityConfig.getInstance().logCommands()) {
                source.sendSystemMessage(Component.translatable("identity.test_failed", player.getDisplayName(), Component.translatable(type.getDescriptionId())), true);
            }

            return 1;
        }

        if(IdentityConfig.getInstance().logCommands()) {
            source.sendSystemMessage(Component.translatable("identity.test_positive", player.getDisplayName(), Component.translatable(type.getDescriptionId())), true);
        }

        return 0;
    }

    private static void grant(ServerPlayer source, ServerPlayer player, Identifier id, @Nullable CompoundTag nbt) {
        IdentityType<LivingEntity> type = new IdentityType(BuiltInRegistries.ENTITY_TYPE.getValue(id));
        Component name = Component.translatable(type.getEntityType().getDescriptionId());

        // If the specified granting NBT is not null, change the IdentityType to reflect potential variants.
        if(nbt != null) {
            CompoundTag copy = nbt.copy();
            copy.putString("id", id.toString());
            ServerLevel serverWorld = source.level();
            Entity loaded = EntityType.loadEntityRecursive(copy, serverWorld, EntitySpawnReason.COMMAND, it -> it);
            if(loaded instanceof LivingEntity living) {
                type = new IdentityType<>(living);
                name = type.createTooltipText(living);
            }
        }

        if(!PlayerUnlocks.has(player, type)) {
            boolean result = PlayerUnlocks.unlock(player, type);

            if(result && IdentityConfig.getInstance().logCommands()) {
                player.sendSystemMessage(Component.translatable("identity.unlock_entity", name), true);
                source.sendSystemMessage(Component.translatable("identity.grant_success", name, player.getDisplayName()), true);
            }
        } else {
            if(IdentityConfig.getInstance().logCommands()) {
                source.sendSystemMessage(Component.translatable("identity.already_has", player.getDisplayName(), name), true);
            }
        }
    }

    private static void revoke(ServerPlayer source, ServerPlayer player, Identifier id, @Nullable CompoundTag nbt) {
        IdentityType<LivingEntity> type = new IdentityType(BuiltInRegistries.ENTITY_TYPE.getValue(id));
        Component name = Component.translatable(type.getEntityType().getDescriptionId());

        // If the specified granting NBT is not null, change the IdentityType to reflect potential variants.
        if(nbt != null) {
            CompoundTag copy = nbt.copy();
            copy.putString("id", id.toString());
            ServerLevel serverWorld = source.level();
            Entity loaded = EntityType.loadEntityRecursive(copy, serverWorld, EntitySpawnReason.COMMAND, it -> it);
            if(loaded instanceof LivingEntity living) {
                type = new IdentityType<>(living);
                name = type.createTooltipText(living);
            }
        }

        if(PlayerUnlocks.has(player, type)) {
            PlayerUnlocks.revoke(player, type);

            if(IdentityConfig.getInstance().logCommands()) {
                player.sendSystemMessage(Component.translatable("identity.revoke_entity", name), true);
                source.sendSystemMessage(Component.translatable("identity.revoke_success", name, player.getDisplayName()), true);
            }
        } else {
            if(IdentityConfig.getInstance().logCommands()) {
                source.sendSystemMessage(Component.translatable("identity.does_not_have", player.getDisplayName(), name), true);
            }
        }
    }

    private static void equip(ServerPlayer source, ServerPlayer player, Identifier identity, @Nullable CompoundTag nbt) {
        Entity created;

        if(nbt != null) {
            CompoundTag copy = nbt.copy();
            copy.putString("id", identity.toString());
            ServerLevel serverWorld = source.level();
            created = EntityType.loadEntityRecursive(copy, serverWorld, EntitySpawnReason.COMMAND, it -> it);
        } else {
            EntityType<?> entity = BuiltInRegistries.ENTITY_TYPE.getValue(identity);
            created = entity.create(player.level(), EntitySpawnReason.COMMAND);
        }

        if(created instanceof LivingEntity living) {
            @Nullable IdentityType<?> defaultType = IdentityType.from(living);

            if(defaultType != null) {
                boolean result = PlayerIdentity.updateIdentity(player, defaultType, (LivingEntity) created);
                if(result && IdentityConfig.getInstance().logCommands()) {
                    source.sendSystemMessage(Component.translatable("identity.equip_success", Component.translatable(created.getType().getDescriptionId()), player.getDisplayName()), true);
                }
            }
        }
    }

    private static void unequip(ServerPlayer source, ServerPlayer player) {
        boolean result = PlayerIdentity.updateIdentity(player, null, null);

        if(result && IdentityConfig.getInstance().logCommands()) {
            source.sendSystemMessage(Component.translatable("identity.unequip_success", player.getDisplayName()));
        }
    }
}
