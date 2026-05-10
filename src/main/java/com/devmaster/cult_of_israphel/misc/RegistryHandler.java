package com.devmaster.cult_of_israphel.misc;

import com.devmaster.cult_of_israphel.entity.Israphel;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.item.Item;

import net.minecraft.world.item.SpawnEggItem;
import net.minecraftforge.common.ForgeSpawnEggItem;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

@Mod.EventBusSubscriber(modid = Cult_of_Israphel.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class RegistryHandler {

    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(Registries.ITEM, Cult_of_Israphel.MOD_ID);

    public static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(Registries.ENTITY_TYPE, Cult_of_Israphel.MOD_ID);

    public static void register(IEventBus bus) {
        ITEMS.register(bus);
        ENTITIES.register(bus);
    }

    public static final RegistryObject<EntityType<Israphel>> ISRAPHEL =
            ENTITIES.register("israphel",
                    () -> EntityType.Builder
                            .of(Israphel::new, MobCategory.MONSTER)
                            .sized(0.6F, 1.95F)
                            .build("israphel"));


                           // 0x2B2B2B, 0x8B0000,


    //public static final RegistryObject<Item> ISRAPHEL_SPAWN_EGG = ITEMS.register("israphel_spawn_egg",() -> new ForgeSpawnEggItem(RegistryHandler.ISRAPHEL, 0x800002, 0xff0f45, new Item.Properties()));
    public static final RegistryObject<SpawnEggItem> ISRAPHEL_SPAWN_EGG = ITEMS.register("israphel_spawn_egg", () -> new ForgeSpawnEggItem(RegistryHandler.ISRAPHEL, 0xF9F9F9, 0xFFD700, new Item.Properties()));


    @SubscribeEvent
    public static void registerAttributes(EntityAttributeCreationEvent event) {
        event.put(RegistryHandler.ISRAPHEL.get(), Israphel.createAttributes().build());
    }
}
