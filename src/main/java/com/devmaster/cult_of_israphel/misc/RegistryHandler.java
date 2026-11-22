package com.devmaster.cult_of_israphel.misc;

import com.devmaster.cult_of_israphel.entity.Adept;
import com.devmaster.cult_of_israphel.entity.AdeptFireball;
import com.devmaster.cult_of_israphel.entity.Cultist;
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
                            .of(Israphel::new, MobCategory.CREATURE)
                            .sized(0.6F, 1.95F)
                            .build("israphel"));

    public static final RegistryObject<EntityType<Cultist>> CULTIST =
            ENTITIES.register("cultist",
                    () -> EntityType.Builder
                            .of(Cultist::new, MobCategory.MONSTER)
                            .sized(0.6F, 1.95F)
                            .build("cultist"));

    public static final RegistryObject<EntityType<Adept>> ADEPT =
            ENTITIES.register("adept",
                    () -> EntityType.Builder
                            .of(Adept::new, MobCategory.MONSTER)
                            .sized(0.6F, 1.95F)
                            .build("adept"));

    //Projectiles
    public static final RegistryObject<EntityType<AdeptFireball>> ADEPT_FIREBALL =
            ENTITIES.register("adept_fireball",
                    () -> EntityType.Builder.<AdeptFireball>of(AdeptFireball::new, MobCategory.MISC)
                            .sized(0.3125F, 0.3125F)
                            .clientTrackingRange(4)
                            .updateInterval(10)
                            .build("adept_fireball"));

    public static final RegistryObject<SpawnEggItem> ISRAPHEL_SPAWN_EGG = ITEMS.register("israphel_spawn_egg", () -> new ForgeSpawnEggItem(RegistryHandler.ISRAPHEL, 0xF9F9F9, 0xFFD700, new Item.Properties()));
    public static final RegistryObject<SpawnEggItem> CULTIST_SPAWN_EGG = ITEMS.register("cultist_spawn_egg", () -> new ForgeSpawnEggItem(RegistryHandler.CULTIST, 0x808080, 0x8B0000, new Item.Properties()));
    public static final RegistryObject<SpawnEggItem> ADEPT_SPAWN_EGG = ITEMS.register("adept_spawn_egg", () -> new ForgeSpawnEggItem(RegistryHandler.ADEPT, 65536, 13762560, new Item.Properties()));
    public static final RegistryObject<SpawnEggItem> ARCHBISHOP_SPAWN_EGG = ITEMS.register("archbishop_spawn_egg", () -> new ForgeSpawnEggItem(RegistryHandler.ADEPT, 0x8B0000,0xFF0000, new Item.Properties()));

    @SubscribeEvent
    public static void registerAttributes(EntityAttributeCreationEvent event) {
        event.put(RegistryHandler.ISRAPHEL.get(), Israphel.createAttributes().build());
        event.put(RegistryHandler.CULTIST.get(), Cultist.createAttributes().build());
        event.put(RegistryHandler.ADEPT.get(), Adept.createAttributes().build());

    }
}