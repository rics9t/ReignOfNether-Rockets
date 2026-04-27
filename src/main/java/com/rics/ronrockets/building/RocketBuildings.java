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
    public static MonsterRocketSilo MONSTER_SILO;
    public static PiglinRocketSilo PIGLIN_SILO;
    public static ShieldArrayBuilding SHIELD_ARRAY;

    public static void register() {

        VILLAGER_SILO = Registry.register(
                ReignOfNetherRegistries.BUILDING,
                ResourceLocation.fromNamespaceAndPath(RonRocketsMod.MODID, "villager_rocket_silo"),
                new VillagerRocketSilo()
        );

        MONSTER_SILO = Registry.register(
                ReignOfNetherRegistries.BUILDING,
                ResourceLocation.fromNamespaceAndPath(RonRocketsMod.MODID, "monster_rocket_silo"),
                new MonsterRocketSilo()
        );

        PIGLIN_SILO = Registry.register(
                ReignOfNetherRegistries.BUILDING,
                ResourceLocation.fromNamespaceAndPath(RonRocketsMod.MODID, "piglin_rocket_silo"),
                new PiglinRocketSilo()
        );

        SHIELD_ARRAY = Registry.register(
                ReignOfNetherRegistries.BUILDING,
                ResourceLocation.fromNamespaceAndPath(RonRocketsMod.MODID, "shield_array"),
                new ShieldArrayBuilding()
        );

        // ✅ Villagers
        FactionRegistries.register(Faction.VILLAGERS, VILLAGER_SILO, Keybindings.keyZ);
        FactionRegistries.register(Faction.VILLAGERS, SHIELD_ARRAY, Keybindings.keyX);

        // ✅ Monsters
        FactionRegistries.register(Faction.MONSTERS, MONSTER_SILO, Keybindings.keyZ);
        FactionRegistries.register(Faction.MONSTERS, SHIELD_ARRAY, Keybindings.keyX);

        // ✅ Piglins
        FactionRegistries.register(Faction.PIGLINS, PIGLIN_SILO, Keybindings.keyZ);
        FactionRegistries.register(Faction.PIGLINS, SHIELD_ARRAY, Keybindings.keyX);
    }
}
