package draylar.identity.impl.variant;

import draylar.identity.api.variant.TypeProvider;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.animal.fish.TropicalFish;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;

// TODO: do we want to add this? There will be a boat-load of fish...
public class TropicalFishTypeProvider extends TypeProvider<TropicalFish> {

    @Override
    public int getVariantData(TropicalFish entity) {
        return 0;
    }

    @Override
    public TropicalFish create(EntityType<TropicalFish> type, Level level, int data) {
        return type.create(level, EntitySpawnReason.COMMAND);
    }

    @Override
    public int getFallbackData() {
        return 0;
    }

    @Override
    public int getRange() {
        return 0;
    }

    @Override
    public Component modifyText(TropicalFish entity, MutableComponent text) {
        return text;
    }
}
