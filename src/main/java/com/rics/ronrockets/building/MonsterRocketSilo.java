package com.rics.ronrockets.building;

import com.solegendary.reignofnether.faction.Faction;
import net.minecraft.world.level.block.Blocks;

public class MonsterRocketSilo extends AbstractRocketSilo {

    public MonsterRocketSilo() {
        super("monster_rocket_silo");
        this.portraitBlock = Blocks.OBSIDIAN;
    }

    @Override
    public Faction getFaction() {
        return Faction.MONSTERS;
    }
}