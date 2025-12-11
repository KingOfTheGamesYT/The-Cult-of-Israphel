package com.devmaster.cult_of_israphel.config;

import net.minecraft.resources.ResourceLocation;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Arrays;
import java.util.List;

public class Config {
    public static ForgeConfigSpec COMMON_CONFIG;
    public static ForgeConfigSpec.ConfigValue<List<? extends String>> ENTITY_BLACKLIST;
    public static ForgeConfigSpec.BooleanValue RANDOMIZE_ALL_SPAWNERS;


    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();

        builder.push("DungeonSpawnerBlacklist");

        ENTITY_BLACKLIST = builder.defineList(
                "blacklistedEntities",
                Arrays.asList(
                        "minecraft:wither",
                        "minecraft:ender_dragon",
                        "minecraft:elder_guardian",
                        "minecraft:slime",
                        "minecraft:zombified_piglin",
                        "minecraft:giant",
                        "draconicevolution:guardian_wither",
                        "draconicevolution:draconic_guardian",
                        "aquamirae:maze_mother",
                        "blue_skies:seclam",
                        "cataclysm:deepling_warlock",
                        "deeperdarker:shriek_worm",
                        "mowziesmobs:grottol",
                        "iceandfire:dread_horse",
                        "alexmobs:bone_serpent_part"

                ),
                obj -> {
                    if (!(obj instanceof String)) return false;
                    ResourceLocation id = ResourceLocation.tryParse((String) obj);
                    return id != null && ForgeRegistries.ENTITY_TYPES.containsKey(id);
                }
        );

        RANDOMIZE_ALL_SPAWNERS = builder.comment(
                "If true, any mob spawner placed in the world (by players) will be randomized"
        ).define("randomizeAllSpawners", true);

        builder.pop();
        COMMON_CONFIG = builder.build();
    }
}