package com.rics.ronrockets.building;

import com.solegendary.reignofnether.faction.Faction;
import net.minecraft.world.level.block.Blocks;

public class PiglinRocketSilo extends AbstractRocketSilo {

    public PiglinRocketSilo() {
        super("piglin_rocket_silo");
        this.portraitBlock = Blocks.GILDED_BLACKSTONE;
    }

    @Override
    public Faction getFaction() {
        return Faction.PIGLINS;
    }
}