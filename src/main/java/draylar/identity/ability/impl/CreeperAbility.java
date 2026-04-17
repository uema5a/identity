package draylar.identity.ability.impl;

import draylar.identity.ability.IdentityAbility;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

public class CreeperAbility extends IdentityAbility<Creeper> {

    @Override
    public void onUse(Player player, Creeper identity, Level level) {
        float power = identity.isPowered() ? 6.0f : 3.0f;
        level.explode(player, player.getX(), player.getY(), player.getZ(), power, Level.ExplosionInteraction.NONE);
    }

    @Override
    public Item getIcon() {
        return Items.TNT;
    }
}
