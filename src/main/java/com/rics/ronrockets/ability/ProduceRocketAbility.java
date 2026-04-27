package com.rics.ronrockets.ability;

import com.solegendary.reignofnether.ability.Ability;
import com.solegendary.reignofnether.building.BuildingPlacement;
import com.solegendary.reignofnether.hud.AbilityButton;
import com.solegendary.reignofnether.keybinds.Keybinding;
import com.solegendary.reignofnether.unit.UnitAction;

import net.minecraft.resources.ResourceLocation;

import java.util.List;

public class ProduceRocketAbility extends Ability {

    public static final int MAX_ROCKETS = 2;

    public ProduceRocketAbility() {
        super(UnitAction.NONE, 0, 0, 0, false);
        this.maxCharges = MAX_ROCKETS;
    }

    // ✅ CRITICAL FIX:
    // Return a hidden dummy button so Abilities system does not insert null
    @Override
    public AbilityButton getButton(Keybinding hotkey, BuildingPlacement placement) {
        return new AbilityButton(
                "",
                ResourceLocation.fromNamespaceAndPath("minecraft", "textures/item/barrier.png"),
                hotkey,
                () -> false,
                () -> true,
                () -> false,
                () -> {},
                null,
                List.of(),
                this,
                placement
        );
    }
}
