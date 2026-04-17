package draylar.identity.api.model;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.MultiBufferSource;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;

import java.util.HashMap;
import java.util.Map;

/**
 * Registry class for {@link EntityUpdater} instances.
 *
 * <p>{@link EntityUpdater}s are used to apply changes to identity entity instances on the client using information from the player.
 * As an example, an {@link EntityUpdater} can be used to tell a identity bat to "stop roosting," which triggers the flight animation.
 * {@link EntityUpdater}s are called once every render tick {@link net.minecraft.client.renderer.entity.EntityRenderer#render(Entity, float, float, PoseStack, MultiBufferSource, int)}.
 */
@Environment(EnvType.CLIENT)
public class EntityUpdaters {

    private static final Map<EntityType<? extends LivingEntity>, EntityUpdater<? extends LivingEntity>> map = new HashMap<>();

    /**
     * Returns a {@link EntityUpdater} if one has been registered for the given {@link EntityType}, or null.
     *
     * @param entityType entity type key to retrieve a value registered in {@link EntityUpdaters#register(EntityType, EntityUpdater)}
     * @param <T>        passed in {@link EntityType} generic
     * @return registered {@link EntityUpdater} instance for the given {@link EntityType}, or null if one does not exist
     */
    public static <T extends LivingEntity> EntityUpdater<T> getUpdater(EntityType<T> entityType) {
        return (EntityUpdater<T>) map.getOrDefault(entityType, null);
    }

    /**
     * Registers an {@link EntityUpdater} for the given {@link EntityType}.
     *
     * <p>Note that a given {@link EntityType} can only have 1 {@link EntityUpdater} associated with it.
     *
     * @param type          entity type key associated with the given {@link EntityUpdater}
     * @param entityUpdater {@link EntityUpdater} associated with the given {@link EntityType}
     * @param <T>           passed in {@link EntityType} generic
     */
    public static <T extends LivingEntity> void register(EntityType<T> type, EntityUpdater<T> entityUpdater) {
        map.put(type, entityUpdater);
    }

    private EntityUpdaters() {
        // NO-OP
    }

    public static void init() {
        // TODO: Phase 5 - Rewrite entity updater registrations for MC 26.1
        // Many field names and methods changed (Yarn -> Mojang):
        // - setRoosting, prevFlapProgress, flapProgress, wingPosition, segmentCircularBuffer, etc.
        // - getMainHandItem -> getMainHandItem, getDefaultState -> defaultBlockState
        // Original registrations: BAT, PARROT, ENDER_DRAGON, ENDERMAN, CREEPER
    }
}
