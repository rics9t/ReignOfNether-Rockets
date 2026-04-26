package com.rics.ronrockets.ability;

import com.rics.ronrockets.RonRocketsMod;
import com.rics.ronrockets.shield.ShieldEnergyManager;
import com.solegendary.reignofnether.ability.Ability;
import com.solegendary.reignofnether.building.BuildingPlacement;
import com.solegendary.reignofnether.hud.AbilityButton;
import com.solegendary.reignofnether.keybinds.Keybinding;
import com.solegendary.reignofnether.unit.UnitAction;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;

import java.util.List;

public class ShieldInterceptAbility extends Ability {

    public ShieldInterceptAbility() {
        super(UnitAction.NONE, 0, 0, 0, false);
    }

    @Override
    public AbilityButton getButton(Keybinding hotkey, BuildingPlacement placement) {

        int energy = ShieldEnergyManager.getEnergy(placement);

        return new AbilityButton(
                I18n.get("abilities.ronrockets.shield_intercept"),
                new ResourceLocation(RonRocketsMod.MODID, "textures/icons/shield_intercept.png"),
                hotkey,
                () -> false,
                () -> false,
                () -> true,
                null,
                null,
                List.of(
                        FormattedCharSequence.forward(
                                I18n.get("abilities.ronrockets.shield_intercept"),
                                Style.EMPTY.withBold(true)
                        ),
                        FormattedCharSequence.forward(
                                "Energy: " + energy + " / 500",
                                Style.EMPTY
                        ),
                        FormattedCharSequence.forward(
                                "Intercept Cost: 250",
                                Style.EMPTY
                        ),
                        FormattedCharSequence.forward(
                                "Refill: 10 energy per 2 ore",
                                Style.EMPTY
                        )
                ),
                this,
                placement
        );
    }
}
