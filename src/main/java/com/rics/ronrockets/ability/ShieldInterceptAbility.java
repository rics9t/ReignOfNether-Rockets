package com.rics.ronrockets.ability;

import com.rics.ronrockets.shield.ShieldStateManager;
import com.solegendary.reignofnether.ability.Ability;
import com.solegendary.reignofnether.building.BuildingPlacement;
import com.solegendary.reignofnether.hud.AbilityButton;
import com.solegendary.reignofnether.keybinds.Keybinding;
import com.solegendary.reignofnether.resources.ResourceName;
import com.solegendary.reignofnether.resources.ResourcesServerEvents;
import com.solegendary.reignofnether.unit.UnitAction;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.level.Level;
import net.minecraft.core.BlockPos;

import java.util.List;

public class ShieldInterceptAbility extends Ability {

    private static final int ACTIVATION_COST = 150;

    public ShieldInterceptAbility() {
        // ✅ 30 seconds (600 ticks) explicitly set in the super constructor for RoN UI to detect
        super(UnitAction.NONE, 600, 0, 0, false);
    }

    @Override
    public AbilityButton getButton(Keybinding hotkey, BuildingPlacement placement) {
        return new AbilityButton(
                "Shield Intercept",
                ResourceLocation.fromNamespaceAndPath("minecraft", "textures/item/nether_star.png"),
                hotkey,
                () -> ShieldStateManager.isActive(placement), // Button glows when active
                () -> !ResourcesServerEvents.canAfford(placement.ownerName, ResourceName.ORE, ACTIVATION_COST),
                () -> placement.getCooldown(this) <= 0,
                () -> this.use(placement.getLevel(), placement, placement.centrePos),
                null,
                List.of(
                        FormattedCharSequence.forward("Activate Shield Intercept", Style.EMPTY.withBold(true)),
                        FormattedCharSequence.forward("Cost: " + ACTIVATION_COST + " Ore", Style.EMPTY),
                        FormattedCharSequence.forward("Duration: 10s", Style.EMPTY),
                        FormattedCharSequence.forward("Cooldown: 30s", Style.EMPTY)
                ),
                this, placement
        );
    }

    @Override
    public void use(Level level, BuildingPlacement buildingUsing, BlockPos bp) {
        if (level.isClientSide()) return;
        if (buildingUsing.getCooldown(this) > 0) return;

        if (!ResourcesServerEvents.canAfford(buildingUsing.ownerName, ResourceName.ORE, ACTIVATION_COST)) return;
        ResourcesServerEvents.addSubtractResources(new com.solegendary.reignofnether.resources.Resources(buildingUsing.ownerName, 0, 0, -ACTIVATION_COST));

        // ✅ Tells RoN's UI engine to start the visual cooldown dial!
        buildingUsing.setCooldown(this, cooldownMax);
        ShieldStateManager.activate(buildingUsing);
    }
}
