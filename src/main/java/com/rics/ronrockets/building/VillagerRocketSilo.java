package com.rics.ronrockets.building;

import com.solegendary.reignofnether.faction.Faction;

public class VillagerRocketSilo extends AbstractRocketSilo {

    public VillagerRocketSilo() {
        super("villager_rocket_silo");
        this.portraitBlock = net.minecraft.world.level.block.Blocks.IRON_BLOCK;
    }

    @Override
    public Faction getFaction() {
        return Faction.VILLAGERS;
    }
}