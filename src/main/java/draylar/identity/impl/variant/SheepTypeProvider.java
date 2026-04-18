package draylar.identity.impl.variant;

import draylar.identity.api.variant.TypeProvider;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.sheep.Sheep;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.Level;

public class SheepTypeProvider extends TypeProvider<Sheep> {

    @Override
    public int getVariantData(Sheep entity) {
        return entity.getColor().ordinal();
    }

    @Override
    public Sheep create(EntityType<Sheep> type, Level level, int data) {
        Sheep sheep = new Sheep(type, level);
        sheep.setColor(DyeColor.byId(data));
        return sheep;
    }

    @Override
    public int getFallbackData() {
        return DyeColor.WHITE.getId();
    }

    @Override
    public int getRange() {
        return DyeColor.BLACK.getId();
    }

    @Override
    public Component modifyText(Sheep sheep, MutableComponent text) {
        return Component.literal(formatTypePrefix(DyeColor.byId(getVariantData(sheep)).getName()) + " ").append(text);
    }
}
