package com.rics.ronrockets.ability;

import com.rics.ronrockets.RonRocketsMod;
import com.solegendary.reignofnether.ability.Ability;
import com.solegendary.reignofnether.building.BuildingPlacement;
import com.solegendary.reignofnether.hud.AbilityButton;
import com.solegendary.reignofnether.keybinds.Keybinding;
import com.solegendary.reignofnether.unit.UnitAction;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.level.Level;

import java.util.List;

public class ProduceRocketAbility extends Ability {

    private static final int PRODUCTION_TIME = 2400; // 120 seconds

    public ProduceRocketAbility() {
        super(UnitAction.NONE, PRODUCTION_TIME, 0, 0, false);
        this.maxCharges = 2;
    }

    @Override
    public AbilityButton getButton(Keybinding hotkey, BuildingPlacement placement) {

        int charges = placement.getCharges(this);

        return new AbilityButton(
                "Produce Rocket",
                new ResourceLocation(RonRocketsMod.MODID, "textures/icons/produce_rocket.png"),
                hotkey,
                () -> false,
                () -> charges >= maxCharges,
                () -> charges < maxCharges,
                () -> this.use(placement.getLevel(), placement, placement.centrePos),
                null,
                List.of(
                        FormattedCharSequence.forward(
                                I18n.get("abilities.ronrockets.produce_rocket"),
                                Style.EMPTY.withBold(true)
                        ),
                        FormattedCharSequence.forward(
                                "Production Time: 120s",
                                Style.EMPTY
                        ),
                        FormattedCharSequence.forward(
                                "Stored Rockets: " + charges + "/2",
                                Style.EMPTY
                        )
                ),
                this,
                placement
        );
    }

    @Override
    public void use(Level level, BuildingPlacement buildingUsing, net.minecraft.core.BlockPos bp) {

        if (level.isClientSide()) return;

        if (buildingUsing.getCharges(this) >= maxCharges) return;

        buildingUsing.setCooldown(this, cooldownMax);
    }
}
