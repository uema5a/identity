package draylar.identity.impl;

import draylar.identity.api.variant.IdentityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.nbt.CompoundTag;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Set;

public interface PlayerDataProvider {

    Set<IdentityType<?>> getUnlocked();
    void setUnlocked(Set<IdentityType<?>> unlocked);

    Set<IdentityType<?>> getFavorites();
    void setFavorites(Set<IdentityType<?>> favorites);

    int getRemainingHostilityTime();
    void setRemainingHostilityTime(int max);

    int getAbilityCooldown();
    void setAbilityCooldown(int cooldown);

    LivingEntity getIdentity();
    void setIdentity(@Nullable LivingEntity identity);
    boolean updateIdentity(@Nullable IdentityType<?> type, @Nullable LivingEntity identity);

    IdentityType<?> getIdentityType();
    void setIdentityType(@Nullable IdentityType<?> type);

    Map<String, CompoundTag> getVillagerIdentities();
    void setVillagerIdentity(String key, CompoundTag identity);
    void removeVillagerIdentity(String key);

    @Nullable String getActiveVillagerKey();
    void setActiveVillagerKey(@Nullable String key);
}
