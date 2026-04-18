package draylar.identity.api;

import draylar.identity.registry.IdentityEntityTags;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;

import java.util.HashSet;
import java.util.Set;

public class SafeTagManager {

    private static final Set<Identifier> CUSTOM_FLYING_ENTITIES = new HashSet<>();
    private static final Set<Identifier> CUSTOM_BREATHE_UNDERWATER_ENTITIES = new HashSet<>();
    private static final Set<Identifier> CUSTOM_FIRE_IMMUNE_ENTITIES = new HashSet<>();
    private static final Set<Identifier> CUSTOM_SLOW_FALLING = new HashSet<>();
    private static final Set<Identifier> CUSTOM_BURNS_IN_DAYLIGHT = new HashSet<>();
    private static final Set<Identifier> CUSTOM_CANT_SWIM = new HashSet<>();
    private static final Set<Identifier> CUSTOM_HURT_BY_HEAT = new HashSet<>();
    private static final Set<Identifier> CUSTOM_LAVA_WALKING = new HashSet<>();
    private static final Set<Identifier> CUSTOM_PIGLIN_FRIENDLY = new HashSet<>();
    private static final Set<Identifier> CUSTOM_RAVAGER_RIDING = new HashSet<>();
    private static final Set<Identifier> CUSTOM_UNDROWNABLE = new HashSet<>();
    private static final Set<Identifier> CUSTOM_WOLF_PREY = new HashSet<>();
    private static final Set<Identifier> CUSTOM_FOX_PREY = new HashSet<>();


    public static void loadAll() {
        // Load all custom sets
        loadTagSafely(IdentityEntityTags.CUSTOM_FLYING, CUSTOM_FLYING_ENTITIES, "custom_flying");
        loadTagSafely(IdentityEntityTags.CUSTOM_BREATHE_UNDERWATER, CUSTOM_BREATHE_UNDERWATER_ENTITIES, "custom_breathe_underwater");
        loadTagSafely(IdentityEntityTags.CUSTOM_FIRE_IMMUNE, CUSTOM_FIRE_IMMUNE_ENTITIES, "custom_fire_immune");
        loadTagSafely(IdentityEntityTags.CUSTOM_SLOW_FALLING, CUSTOM_SLOW_FALLING, "custom_slow_falling");
        loadTagSafely(IdentityEntityTags.CUSTOM_BURNS_IN_DAYLIGHT, CUSTOM_BURNS_IN_DAYLIGHT, "custom_burns_in_daylight");
        loadTagSafely(IdentityEntityTags.CUSTOM_CANT_SWIM, CUSTOM_CANT_SWIM, "custom_cant_swim");
        loadTagSafely(IdentityEntityTags.CUSTOM_HURT_BY_HEAT, CUSTOM_HURT_BY_HEAT, "custom_hurt_by_high_temperature");
        loadTagSafely(IdentityEntityTags.CUSTOM_LAVA_WALKING, CUSTOM_LAVA_WALKING, "custom_lava_walking");
        loadTagSafely(IdentityEntityTags.CUSTOM_PIGLIN_FRIENDLY, CUSTOM_PIGLIN_FRIENDLY, "custom_piglin_friendly");
        loadTagSafely(IdentityEntityTags.CUSTOM_RAVAGER_RIDING, CUSTOM_RAVAGER_RIDING, "custom_ravager_riding");
        loadTagSafely(IdentityEntityTags.CUSTOM_UNDROWNABLE, CUSTOM_UNDROWNABLE, "custom_undrownable");
        loadTagSafely(IdentityEntityTags.CUSTOM_WOLF_PREY, CUSTOM_WOLF_PREY, "custom_wolf_prey");
        loadTagSafely(IdentityEntityTags.CUSTOM_FOX_PREY, CUSTOM_FOX_PREY, "custom_fox_prey");
    }

    private static void loadTagSafely(TagKey<EntityType<?>> tagKey, Set<Identifier> targetSet, String tagName) {
        targetSet.clear();

        Iterable<Holder<EntityType<?>>> tagIterable = BuiltInRegistries.ENTITY_TYPE.getTagOrEmpty(tagKey);
        boolean found = false;
        for (Holder<EntityType<?>> holder : tagIterable) {
            found = true;
            Identifier id = BuiltInRegistries.ENTITY_TYPE.getKey(holder.value());
            if (id != null) {
                targetSet.add(id);
            } else {
                System.out.println("[Identity] Skipping missing entity in " + tagName);
            }
        }
        if (found) {
            System.out.println("[Identity] Loaded " + targetSet.size() + " entries into " + tagName);
        } else {
            System.out.println("[Identity] Warning: Tag not found or empty: " + tagName);
        }
    }

    // --- API for checking if an entity matches ---

    public static boolean isCustomFlying(EntityType<?> type) {
        return CUSTOM_FLYING_ENTITIES.contains(BuiltInRegistries.ENTITY_TYPE.getKey(type));
    }

    public static boolean isCustomBreatheUnderwater(EntityType<?> type) {
        return CUSTOM_BREATHE_UNDERWATER_ENTITIES.contains(BuiltInRegistries.ENTITY_TYPE.getKey(type));
    }

    public static boolean isCustomFireImmune(EntityType<?> type) {
        return CUSTOM_FIRE_IMMUNE_ENTITIES.contains(BuiltInRegistries.ENTITY_TYPE.getKey(type));
    }

    public static boolean isCustomSlowFalling(EntityType<?> type) {
        return CUSTOM_SLOW_FALLING.contains(BuiltInRegistries.ENTITY_TYPE.getKey(type));
    }

    public static boolean isCustomBurnsInDaylight(EntityType<?> type) {
        return CUSTOM_BURNS_IN_DAYLIGHT.contains(BuiltInRegistries.ENTITY_TYPE.getKey(type));
    }

    public static boolean isCustomCantSwim(EntityType<?> type) {
        return CUSTOM_CANT_SWIM.contains(BuiltInRegistries.ENTITY_TYPE.getKey(type));
    }

    public static boolean isCustomHurtByHeat(EntityType<?> type) {
        return CUSTOM_HURT_BY_HEAT.contains(BuiltInRegistries.ENTITY_TYPE.getKey(type));
    }

    public static boolean isCustomLavaWalking(EntityType<?> type) {
        return CUSTOM_LAVA_WALKING.contains(BuiltInRegistries.ENTITY_TYPE.getKey(type));
    }

    public static boolean isCustomPiglinFriendly(EntityType<?> type) {
        return CUSTOM_PIGLIN_FRIENDLY.contains(BuiltInRegistries.ENTITY_TYPE.getKey(type));
    }

    public static boolean isCustomRavagerRiding(EntityType<?> type) {
        return CUSTOM_RAVAGER_RIDING.contains(BuiltInRegistries.ENTITY_TYPE.getKey(type));
    }

    public static boolean isCustomUndrownable(EntityType<?> type) {
        return CUSTOM_UNDROWNABLE.contains(BuiltInRegistries.ENTITY_TYPE.getKey(type));
    }

    public static boolean isCustomWolfPrey(EntityType<?> type) {
        return CUSTOM_WOLF_PREY.contains(BuiltInRegistries.ENTITY_TYPE.getKey(type));
    }

    public static boolean isCustomFoxPrey(EntityType<?> type) {
        return CUSTOM_FOX_PREY.contains(BuiltInRegistries.ENTITY_TYPE.getKey(type));
    }
}
