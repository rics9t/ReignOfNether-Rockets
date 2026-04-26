package com.rics.ronrockets.ability;

import com.rics.ronrockets.RonRocketsMod;
import com.rics.ronrockets.shield.ShieldStateManager;
import com.solegendary.reignofnether.ability.Ability;
import com.solegendary.reignofnether.building.BuildingPlacement;
import com.solegendary.reignofnether.hud.AbilityButton;
import com.solegendary.reignofnether.keybinds.Keybinding;
import com.solegendary.reignofnether.resources.ResourceName;
import com.solegendary.reignofnether.resources.Resources;
import com.solegendary.reignofnether.resources.ResourcesServerEvents;
import com.solegendary.reignofnether.unit.UnitAction;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.level.Level;

import java.util.List;

public class ShieldInterceptAbility extends Ability {

    private static final int ACTIVATION_COST = 150;

    public ShieldInterceptAbility() {
        super(UnitAction.NONE, 0, 0, 0, false);
    }

    @Override
    public AbilityButton getButton(Keybinding hotkey, BuildingPlacement placement) {

        return new AbilityButton(
                I18n.get("abilities.ronrockets.shield_intercept"),
                new ResourceLocation(RonRocketsMod.MODID, "textures/icons/shield_intercept.png"),
                hotkey,
                () -> false,
                () -> false,
                () -> ShieldStateManager.canActivate(placement),
                () -> this.use(placement.getLevel(), placement, placement.centrePos),
                null,
                List.of(
                        FormattedCharSequence.forward(
                                I18n.get("abilities.ronrockets.shield_intercept"),
                                Style.EMPTY.withBold(true)
                        ),
                        FormattedCharSequence.forward(
                                "Radius: 64 blocks",
                                Style.EMPTY
                        ),
                        FormattedCharSequence.forward(
                                "Active Duration: 10s",
                                Style.EMPTY
                        ),
                        FormattedCharSequence.forward(
                                "Cooldown: 30s",
                                Style.EMPTY
                        ),
                        FormattedCharSequence.forward(
                                "Activation Cost: 150 Ore",
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
        if (!ShieldStateManager.canActivate(buildingUsing)) return;

        if (!ResourcesServerEvents.canAfford(
                buildingUsing.ownerName,
                ResourceName.ORE,
                ACTIVATION_COST)) return;

        ResourcesServerEvents.addSubtractResources(
                new Resources(buildingUsing.ownerName, 0, 0, -ACTIVATION_COST)
        );

        ShieldStateManager.activate(buildingUsing);
    }
}
