package com.devmaster.cult_of_israphel.misc;

import com.devmaster.cult_of_israphel.entity.*;

import com.devmaster.cult_of_israphel.worldgen.ArchbishopStrongholdPiece;
import com.devmaster.cult_of_israphel.worldgen.ArchbishopStrongholdStructure;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.SpawnEggItem;

import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.ForgeSpawnEggItem;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

@Mod.EventBusSubscriber(modid = Cult_of_Israphel.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class RegistryHandler {

    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(Registries.ITEM, Cult_of_Israphel.MOD_ID);
    public static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(Registries.ENTITY_TYPE, Cult_of_Israphel.MOD_ID);
    public static final DeferredRegister<StructureType<?>> STRUCTURE_TYPES = DeferredRegister.create(Registries.STRUCTURE_TYPE, Cult_of_Israphel.MOD_ID);
    public static final DeferredRegister<StructurePieceType> STRUCTURE_PIECE_TYPES = DeferredRegister.create(Registries.STRUCTURE_PIECE, Cult_of_Israphel.MOD_ID);

    public static void register(IEventBus bus) {
        ITEMS.register(bus);
        ENTITIES.register(bus);
        STRUCTURE_TYPES.register(bus);
        STRUCTURE_PIECE_TYPES.register(bus);
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

    public static final RegistryObject<EntityType<Archbishop>> ARCHBISHOP =
            ENTITIES.register("archbishop",
                    () -> EntityType.Builder
                            .of(Archbishop::new, MobCategory.MONSTER)
                            .sized(0.6F, 1.95F)
                            .build("archbishop"));


    //Projectiles
    public static final RegistryObject<EntityType<SoulCastFireball>> ADEPT_FIREBALL =
            ENTITIES.register("adept_fireball",
                    () -> EntityType.Builder.<SoulCastFireball>of(SoulCastFireball::new, MobCategory.MISC)
                            .sized(0.3125F, 0.3125F)
                            .clientTrackingRange(4)
                            .updateInterval(10)
                            .build("adept_fireball"));

    public static final RegistryObject<EntityType<SoulCastFireball>> ARCHBISHOP_FIREBALL =
            ENTITIES.register("archbishop_fireball",
                    () -> EntityType.Builder.<SoulCastFireball>of(SoulCastFireball::new, MobCategory.MISC)
                            .sized(0.3125F, 0.3125F)
                            .clientTrackingRange(4)
                            .updateInterval(10)
                            .build("archbishop_fireball"));

    //Spawn Eggs
    public static final RegistryObject<SpawnEggItem> ISRAPHEL_SPAWN_EGG = ITEMS.register("israphel_spawn_egg", () -> new ForgeSpawnEggItem(RegistryHandler.ISRAPHEL, 0xF9F9F9, 0xFFD700, new Item.Properties()));
    public static final RegistryObject<SpawnEggItem> CULTIST_SPAWN_EGG = ITEMS.register("cultist_spawn_egg", () -> new ForgeSpawnEggItem(RegistryHandler.CULTIST, 0x808080, 0x8B0000, new Item.Properties()));
    public static final RegistryObject<SpawnEggItem> ADEPT_SPAWN_EGG = ITEMS.register("adept_spawn_egg", () -> new ForgeSpawnEggItem(RegistryHandler.ADEPT, 65536, 13762560, new Item.Properties()));
    public static final RegistryObject<SpawnEggItem> ARCHBISHOP_SPAWN_EGG = ITEMS.register("archbishop_spawn_egg", () -> new ForgeSpawnEggItem(RegistryHandler.ARCHBISHOP, 0x8B0000,0xFF0000, new Item.Properties()));

    //Structures
    public static final RegistryObject<StructureType<ArchbishopStrongholdStructure>> ARCHBISHOP_STRONGHOLD = STRUCTURE_TYPES.register("archbishop_stronghold", () -> () -> ArchbishopStrongholdStructure.CODEC);
    public static final RegistryObject<StructurePieceType> ARCHBISHOP_STRONGHOLD_PIECE = STRUCTURE_PIECE_TYPES.register("archbishop_stronghold", () -> ArchbishopStrongholdPiece::new);

    @SubscribeEvent
    public static void registerAttributes(EntityAttributeCreationEvent event) {
        event.put(RegistryHandler.ISRAPHEL.get(), Israphel.createAttributes().build());
        event.put(RegistryHandler.CULTIST.get(), Cultist.createAttributes().build());
        event.put(RegistryHandler.ADEPT.get(), Adept.createAttributes().build());
        event.put(RegistryHandler.ARCHBISHOP.get(), Archbishop.createAttributes().build());

    }
}