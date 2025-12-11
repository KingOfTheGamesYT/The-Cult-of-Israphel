package com.devmaster.cult_of_israphel.misc;

import com.devmaster.cult_of_israphel.config.Config;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


@Mod("cult_of_israphel")
public class Cult_of_Israphel {
    public static final Logger LOGGER = LogManager.getLogger("Cult of Israphel");
    public static final String MOD_ID = "cult_of_israphel";

    public Cult_of_Israphel() {
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::doClientStuff);
        final IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        modEventBus.addListener(this::setup);
        MinecraftForge.EVENT_BUS.register(this);
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, Config.COMMON_CONFIG, "cult_of_israphel-config.toml");
    }

    private void setup(FMLCommonSetupEvent event) {
    }

    private void doClientStuff(final FMLClientSetupEvent event) {
    }
}