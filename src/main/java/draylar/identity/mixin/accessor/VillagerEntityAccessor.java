package draylar.identity.mixin.accessor;

import net.minecraft.world.entity.npc.Villager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(Villager.class)
public interface VillagerEntityAccessor {

    @Accessor("villagerXp")
    int getExperience();

    @Accessor("villagerXp")
    void setExperience(int value);

    // TODO descriptor verify - canLevelUp may have been renamed in MC 26.1
    @Invoker("canLevelUp")
    boolean callGetNextLevelExperience();

    // TODO descriptor verify - fillRecipes may have been renamed in MC 26.1
    @Invoker("fillRecipes")
    void callFillRecipes();

    // TODO descriptor verify - levelUp may have been renamed in MC 26.1
    @Invoker("levelUp")
    void callLevelUp();
}

