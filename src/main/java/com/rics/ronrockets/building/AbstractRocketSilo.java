package com.rics.ronrockets.building;

import com.rics.ronrockets.ability.LaunchRocketAbility;
import com.rics.ronrockets.ability.ProduceRocketAbility;
import com.rics.ronrockets.rocket.RocketProduction;
import com.solegendary.reignofnether.building.BuildingClientEvents;
import com.solegendary.reignofnether.building.BuildingPlacement;
import com.solegendary.reignofnether.building.BuildingServerEvents;
import com.solegendary.reignofnether.building.buildings.placements.ProductionPlacement;
import com.solegendary.reignofnether.building.production.ProductionBuilding;
import com.solegendary.reignofnether.keybinds.Keybindings;
import com.solegendary.reignofnether.player.PlayerServerEvents;
import com.solegendary.reignofnether.resources.ResourceCost;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Rotation;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import static com.solegendary.reignofnether.building.BuildingUtils.getAbsoluteBlockData;

public abstract class AbstractRocketSilo extends ProductionBuilding {

    private static final Logger LOG = LogManager.getLogger("RonRockets");

    public AbstractRocketSilo(String structureName) {
        super(structureName, ResourceCost.Building(0, 300, 250, 0), false);
        this.name = "Rocket Silo";

        // Q = production
        this.productions.add(RocketProduction.ROCKET_PROD, Keybindings.keyQ);

        // W = launch
        this.abilities.add(new LaunchRocketAbility(), Keybindings.keyW);
    }

    /**
     * Checks whether a player already owns a Rocket Silo (built OR under construction).
     * Shared by all faction silos and the RocketPlacementHandler.
     */
    public static boolean playerOwnsSilo(String ownerName, boolean clientSide) {
        Iterable<BuildingPlacement> buildings = clientSide
            ? BuildingClientEvents.getBuildings()
            : BuildingServerEvents.getBuildings();
        for (BuildingPlacement b : buildings) {
            if (b.getBuilding() instanceof AbstractRocketSilo && b.ownerName.equals(ownerName)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Server-side guard + placement factory.
     * Returns null (cancels placement) when the player already owns a silo.
     * Subclasses call this from createBuildingPlacement.
     */
    protected BuildingPlacement checkOnePerPlayerAndCreate(Level level, BlockPos pos,
            Rotation rotation, String ownerName) {
        boolean isClient = level.isClientSide();
        LOG.info("checkOnePerPlayerAndCreate called: clientSide={}, owner={}, pos={}", isClient, ownerName, pos);

        if (!isClient && playerOwnsSilo(ownerName, false)) {
            PlayerServerEvents.sendMessageToPlayer(
                ownerName, "You already have a Rocket Silo!", true, ownerName
            );
            LOG.info("Rejected: player already owns a silo (server-side)");
            return null;
        }

        try {
            BuildingPlacement placement = new com.solegendary.reignofnether.building.buildings.placements.ProductionPlacement(
                this, level, pos, rotation, ownerName,
                getAbsoluteBlockData(getRelativeBlockData(level), level, pos, rotation),
                false
            );
            placement.setCharges(ProduceRocketAbility.INSTANCE, 0);
            LOG.info("Successfully created ProductionPlacement on {} side", isClient ? "client" : "server");
            return placement;
        } catch (Exception e) {
            LOG.error("FAILED to create ProductionPlacement on {} side!", isClient ? "client" : "server", e);
            return null;
        }
    }

    /**
     * Client-side isEnabled supplier for the build button.
     * Disables the button when the local player already owns a silo.
     */
    protected static boolean clientCanPlaceSilo() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return true;
        return !playerOwnsSilo(mc.player.getName().getString(), true);
    }

    @Override
    public float getMeleeDamageMult() {
        return 1.5f; // very fragile — rockets are glass cannons
    }
}
