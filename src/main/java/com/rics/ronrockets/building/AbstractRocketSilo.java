package com.rics.ronrockets.building;

import com.rics.ronrockets.RonRocketsConfig;
import com.rics.ronrockets.ability.LaunchRocketAbility;
import com.rics.ronrockets.ability.ProduceRocketAbility;
import com.rics.ronrockets.rocket.RocketProduction;
import com.solegendary.reignofnether.building.BuildingClientEvents;
import com.solegendary.reignofnether.building.BuildingPlacement;
import com.solegendary.reignofnether.building.BuildingServerEvents;
import com.solegendary.reignofnether.sandbox.SandboxClientEvents;
import com.solegendary.reignofnether.sandbox.SandboxServer;
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

        this.productions.add(RocketProduction.ROCKET_PROD, Keybindings.keyQ);
        this.abilities.add(new LaunchRocketAbility(), Keybindings.keyW);
    }

    /** Count how many silos a player currently owns. */
    public static int countSiloOwned(String ownerName, boolean clientSide) {
        Iterable<BuildingPlacement> buildings = clientSide
            ? BuildingClientEvents.getBuildings()
            : BuildingServerEvents.getBuildings();
        int count = 0;
        for (BuildingPlacement b : buildings) {
            if (b.getBuilding() instanceof AbstractRocketSilo && b.ownerName.equals(ownerName)) {
                count++;
            }
        }
        return count;
    }

    /**
     * Server-side guard + placement factory.
     * Returns null (cancels placement) when the player has reached the silo limit.
     */
    protected BuildingPlacement checkOnePerPlayerAndCreate(Level level, BlockPos pos,
                                                           Rotation rotation, String ownerName) {
        boolean isClient = level.isClientSide();
        LOG.info("checkOnePerPlayerAndCreate called: clientSide={}, owner={}, pos={}", isClient, ownerName, pos);

        if (!isClient) {
            // Sandbox players bypass the silo limit entirely
            if (!SandboxServer.isSandboxPlayer(ownerName)) {
                int limit = RonRocketsConfig.getSiloLimit();
                int current = countSiloOwned(ownerName, false);
                if (current >= limit) {
                    PlayerServerEvents.sendMessageToPlayer(
                        ownerName, "building.ronrockets.rocket_silo.already_owned", true
                    );
                    LOG.info("Rejected: player owns {} silos, limit is {}", current, limit);
                    return null;
                }
            }
        }

        try {
            BuildingPlacement placement = new ProductionPlacement(
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

    /** Client-side isEnabled supplier for the build button. */
    protected static boolean clientCanPlaceSilo() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return true;
        // Use client-side sandbox check (works for non-host players in multiplayer)
        if (SandboxClientEvents.isSandboxPlayer(mc.player.getName().getString())) return true;
        int limit = RonRocketsConfig.getSiloLimit();
        int current = countSiloOwned(mc.player.getName().getString(), true);
        return current < limit;
    }

    @Override
    public float getMeleeDamageMult() {
        return 1.5f;
    }
}
