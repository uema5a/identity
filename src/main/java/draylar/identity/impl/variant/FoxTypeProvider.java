package draylar.identity.impl.variant;

import draylar.identity.api.variant.TypeProvider;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.animal.fox.Fox;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;

public class FoxTypeProvider extends TypeProvider<Fox> {

    @Override
    public int getVariantData(Fox entity) {
        return entity.getVariant().getId();
    }

    @Override
    public Fox create(EntityType<Fox> type, Level level, int data) {
        // Fox.setVariant is private in 26.1 - variant must be set via NBT or accessor
        Fox fox = type.create(level, EntitySpawnReason.COMMAND);
        return fox;
    }

    @Override
    public int getFallbackData() {
        return Fox.Variant.RED.getId();
    }

    @Override
    public int getRange() {
        return Fox.Variant.values().length - 1;
    }

    @Override
    public Component modifyText(Fox entity, MutableComponent text) {
        return Component.literal(formatTypePrefix(Fox.Variant.byId(getVariantData(entity)).getSerializedName()) + " ").append(text);
    }
}
