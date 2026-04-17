package draylar.identity.ability.impl;

import draylar.identity.ability.IdentityAbility;
import draylar.identity.impl.SonicBoomUser;
import net.minecraft.world.entity.monster.warden.Warden;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

public class WardenAbility extends IdentityAbility<Warden> {

    @Override
    public void onUse(Player player, Warden identity, Level level) {
        ((SonicBoomUser) player).identity$ability_startSonicBoom();
    }

    @Override
    public Item getIcon() {
        return Items.ECHO_SHARD;
    }

    @Override
    public int getCooldown(Warden entity) {
        return 20 * 10;
    }
}
