package com.rics.ronrockets.building;

import com.rics.ronrockets.RonRocketsMod;
import com.solegendary.reignofnether.api.ReignOfNetherRegistries;
import com.solegendary.reignofnether.faction.Faction;
import com.solegendary.reignofnether.faction.FactionRegistries;
import com.solegendary.reignofnether.keybinds.Keybindings;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;

public class RocketBuildings {

    public static VillagerRocketSilo VILLAGER_SILO;
    public static ShieldArrayBuilding SHIELD_ARRAY;

    public static void register() {

        VILLAGER_SILO = Registry.register(
                ReignOfNetherRegistries.BUILDING,
                ResourceLocation.fromNamespaceAndPath(
                        RonRocketsMod.MODID,
                        "villager_rocket_silo"
                ),
                new VillagerRocketSilo()
        );

        SHIELD_ARRAY = Registry.register(
                ReignOfNetherRegistries.BUILDING,
                ResourceLocation.fromNamespaceAndPath(
                        RonRocketsMod.MODID,
                        "shield_array"
                ),
                new ShieldArrayBuilding()
        );

        // ✅ Add to faction menus
        FactionRegistries.register(
                Faction.VILLAGERS,
                VILLAGER_SILO,
                Keybindings.keyZ
        );

        FactionRegistries.register(
                Faction.VILLAGERS,
                SHIELD_ARRAY,
                Keybindings.keyX
        );
    }
}
