package draylar.identity.api.model;

import net.minecraft.client.model.geom.ModelPart;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.*;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import com.mojang.datafixers.util.Pair;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

@Environment(EnvType.CLIENT)
public class EntityArms {

    private static final Map<EntityType<? extends LivingEntity>, Pair<EntityArmProvider<? extends LivingEntity>, ArmRenderingManipulator<?>>> DIRECT_PROVIDERS = new LinkedHashMap<>();
    private static final Map<Class<?>, Pair<ClassArmProvider<?>, ArmRenderingManipulator<?>>> CLASS_PROVIDERS = new LinkedHashMap<>();

    @SuppressWarnings("unchecked")
    public static <T extends LivingEntity> void register(EntityType<T> type, EntityArmProvider<T> provider, ArmRenderingManipulator<?> manipulator) {
        DIRECT_PROVIDERS.put(type, new Pair<>(provider, manipulator));
    }

    public static <T> void register(Class<T> modelClass, ClassArmProvider<T> provider, ArmRenderingManipulator<T> manipulator) {
        CLASS_PROVIDERS.put(modelClass, new Pair<>(provider, manipulator));
    }

    @Nullable
    @SuppressWarnings("unchecked")
    public static <T extends LivingEntity> Pair<ModelPart, ArmRenderingManipulator<?>> get(T entity, EntityModel model) {
        Pair<EntityArmProvider<? extends LivingEntity>, ArmRenderingManipulator<?>> before = DIRECT_PROVIDERS.get(entity.getType());

        if(before != null) {
            Pair<EntityArmProvider<T>, ArmRenderingManipulator<?>> provider = new Pair<>((EntityArmProvider<T>) before.getFirst(), before.getSecond());
            return new Pair<>(provider.getFirst().getArm(entity, model), provider.getSecond());
        } else {
            Optional<Pair<ClassArmProvider<?>, ArmRenderingManipulator<?>>> beforeClassProvider = CLASS_PROVIDERS.entrySet().stream().filter(pair -> {
                return pair.getKey().isInstance(model);
            }).findFirst().map(entry -> new Pair<>(entry.getValue().getFirst(), entry.getValue().getSecond()));

            if(beforeClassProvider.isPresent()) {
                Pair<ClassArmProvider<EntityModel>, ArmRenderingManipulator<EntityModel>> classProvider = new Pair<>((ClassArmProvider<EntityModel>) beforeClassProvider.get().getFirst(), (ArmRenderingManipulator<EntityModel>) beforeClassProvider.get().getSecond());
                return new Pair<>(classProvider.getFirst().getArm(entity, model), classProvider.getSecond());
            } else {
                return null;
            }
        }
    }

    @Nullable
    @SuppressWarnings("unchecked")
    public static <T extends LivingEntity> EntityArmProvider<T> get(EntityType<LivingEntity> type) {
        return (EntityArmProvider<T>) DIRECT_PROVIDERS.get(type);
    }

    @Nullable
    @SuppressWarnings("unchecked")
    public static <T extends LivingEntity> EntityArmProvider<T> get(Class<EntityModel> modelClass) {
        return (EntityArmProvider<T>) CLASS_PROVIDERS.get(modelClass);
    }

    public static void init() {
        // TODO: Phase 6 - Rewrite model arm providers for MC 26.1 (EntityModel generics removed,
        // model classes moved to sub-packages, model accessor targets changed)
        // Original registrations:
        // - LlamaModel, PandaModel, BlazeModel, OcelotModel, SpiderModel, IronGolemModel
        // - PigModel, PolarBearModel, RavagerModel, SquidModel, QuadrupedModel
        // - EntityType.PILLAGER with IllagerModel
    }

    private EntityArms() {
        // NO-OP
    }
}
