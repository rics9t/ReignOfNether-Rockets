package com.rics.ronrockets.ability;

import com.rics.ronrockets.RonRocketsMod;
import com.rics.ronrockets.building.AbstractRocketSilo;
import com.rics.ronrockets.entity.RocketEntities;
import com.rics.ronrockets.entity.RocketEntity;
import com.rics.ronrockets.network.RocketWarningClientboundPacket;
import com.solegendary.reignofnether.ability.Ability;
import com.solegendary.reignofnether.building.BuildingPlacement;
import com.solegendary.reignofnether.cursor.CursorClientEvents;
import com.solegendary.reignofnether.hud.AbilityButton;
import com.solegendary.reignofnether.keybinds.Keybinding;
import com.solegendary.reignofnether.unit.UnitAction;

import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;

import java.util.List;

import static com.solegendary.reignofnether.util.MiscUtil.fcs;

public class LaunchRocketAbility extends Ability {

    // 30-second cooldown between launches (comparable to CallLightning at 60s,
    // but rocket must be produced first so 30s is fair)
    private static final int COOLDOWN_TICKS = 600;

    public LaunchRocketAbility() {
        super(UnitAction.ATTACK_GROUND, COOLDOWN_TICKS, 9999, 0, false);
    }

    @Override
    public AbilityButton getButton(Keybinding hotkey, BuildingPlacement placement) {
        int storedRockets = placement.getCharges(ProduceRocketAbility.INSTANCE);
        int maxRockets;
        try {
            maxRockets = ProduceRocketAbility.getMaxRockets();
        } catch (Exception e) {
            maxRockets = 2;
        }
        String title = I18n.get("abilities.ronrockets.launch_rocket");

        return new AbilityButton(
            title,
            ResourceLocation.fromNamespaceAndPath(RonRocketsMod.MODID, "textures/icons/launch_rocket.png"),
            hotkey,
            () -> CursorClientEvents.getLeftClickAction() == UnitAction.ATTACK_GROUND,
            () -> false,
            () -> storedRockets > 0,
            () -> CursorClientEvents.setLeftClickAction(UnitAction.ATTACK_GROUND),
            null,
            List.of(
                fcs(title, true),
                fcs(I18n.get("abilities.ronrockets.launch_rocket.tooltip1", storedRockets, maxRockets)),
                fcs(I18n.get("abilities.ronrockets.launch_rocket.tooltip2")),
                fcs(I18n.get("abilities.ronrockets.launch_rocket.tooltip3")),
                fcs(I18n.get("abilities.ronrockets.launch_rocket.tooltip4", COOLDOWN_TICKS / 20))
            ),
            this,
            placement
        );
    }

    @Override
    public void use(Level level, BuildingPlacement buildingUsing, BlockPos targetBp) {
        if (!(buildingUsing.getBuilding() instanceof AbstractRocketSilo)) return;

        int rockets = buildingUsing.getCharges(ProduceRocketAbility.INSTANCE);
        if (rockets <= 0) return;

        // Server-only: spawn entity, broadcast warning, consume rocket
        if (!level.isClientSide()) {
            ServerLevel serverLevel = (ServerLevel) level;

            RocketEntity rocket = new RocketEntity(RocketEntities.ROCKET.get(), serverLevel);
            rocket.setPos(
                buildingUsing.centrePos.getX() + 0.5,
                buildingUsing.centrePos.getY() + 5,
                buildingUsing.centrePos.getZ() + 0.5
            );
            rocket.setTarget(targetBp);
            rocket.setAttacker(buildingUsing.ownerName);

            serverLevel.addFreshEntity(rocket);

            // Broadcast pre-impact warning to all clients
            RocketWarningClientboundPacket.send(targetBp, buildingUsing.ownerName);
        }

        // BOTH sides: update UI immediately
        buildingUsing.setCharges(ProduceRocketAbility.INSTANCE, rockets - 1);
        this.setToMaxCooldown(buildingUsing);
        buildingUsing.updateButtons();
    }
}
