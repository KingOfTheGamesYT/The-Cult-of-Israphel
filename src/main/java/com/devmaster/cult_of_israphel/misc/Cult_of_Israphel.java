package com.devmaster.cult_of_israphel.misc;

import com.devmaster.cult_of_israphel.config.Config;

import com.devmaster.cult_of_israphel.entity.spawns.AdeptPatrolSpawner;
import com.devmaster.cult_of_israphel.entity.spawns.IsraphelPatrolSpawner;

import net.minecraft.world.item.CreativeModeTabs;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod("cult_of_israphel")
public class Cult_of_Israphel {
    public static final String MOD_ID = "cult_of_israphel";

    public Cult_of_Israphel() {
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::doClientStuff);
        final IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        modEventBus.addListener(this::addCreative);
        RegistryHandler.register(modEventBus);
        modEventBus.addListener(this::setup);
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, Config.COMMON_CONFIG, "cult_of_israphel-config.toml");
        MinecraftForge.EVENT_BUS.register(new IsraphelPatrolSpawner());
        MinecraftForge.EVENT_BUS.register(new AdeptPatrolSpawner());
    }

    private void setup(FMLCommonSetupEvent event) {
    }

    private void doClientStuff(final FMLClientSetupEvent event) {
    }

    private void addCreative(BuildCreativeModeTabContentsEvent event) {
        if(event.getTabKey() == CreativeModeTabs.SPAWN_EGGS) {
            event.accept(RegistryHandler.ISRAPHEL_SPAWN_EGG);
            event.accept(RegistryHandler.CULTIST_SPAWN_EGG);
            event.accept(RegistryHandler.ADEPT_SPAWN_EGG);
            event.accept(RegistryHandler.ARCHBISHOP_SPAWN_EGG);
        }
    }
}