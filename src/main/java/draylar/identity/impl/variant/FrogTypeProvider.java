package draylar.identity.impl.variant;

import draylar.identity.api.variant.TypeProvider;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.animal.frog.Frog;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;

public class FrogTypeProvider extends TypeProvider<Frog> {

    @Override
    public int getVariantData(Frog entity) {
        // Frog variants are now registry-based (Holder<FrogVariant>), return 0 as default
        return 0;
    }

    @Override
    public Frog create(EntityType<Frog> type, Level level, int data) {
        return type.create(level, EntitySpawnReason.COMMAND);
    }

    @Override
    public int getFallbackData() {
        return 0;
    }

    @Override
    public int getRange() {
        return 0; // Simplified - frog variants are now registry-based
    }

    @Override
    public Component modifyText(Frog frog, MutableComponent text) {
        return text;
    }
}
