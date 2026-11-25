package com.devmaster.cult_of_israphel.worldgen.spawns;

import com.devmaster.cult_of_israphel.misc.Cult_of_Israphel;
import com.devmaster.cult_of_israphel.misc.RegistryHandler;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Map;

@Mod.EventBusSubscriber(modid = Cult_of_Israphel.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ArchbishopEncounterController {

    public static final int CULTISTS_PER_FLOOR = 5;
    public static final int ADEPTS_PER_FLOOR = 5;
    public static final int GROUND_FLOOR_Y = 0;
    public static final int SECOND_FLOOR_Y = 15;
    public static final int THIRD_FLOOR_Y = 30;
    public static final int ROOF_Y = 48;
    public static final int ARCHBISHOP_RESPAWN_TICKS = 24000;
    public static final float ISRAPHEL_SPAWN_CHANCE = 0.10F;
    public static final int MAX_ISRAPHEL_PER_STRONGHOLD = 1;
    private static final String ENCOUNTER_KEY = "CultEncounter";
    private static final String ENTITY_TYPE_KEY = "CultEncounterEntity";

    public static void processStructure(WorldGenLevel world, BlockPos origin, BoundingBox structureBounds, BoundingBox currentChunkBox)
    {
        ServerLevel level = world.getLevel();
        String encounterKey = createEncounterKey(origin);
        StrongholdEncounterData data = StrongholdEncounterData.get(level);
        StrongholdEncounterData.EncounterRecord record = data.getOrCreate(encounterKey);
        record.setStructureData(origin, structureBounds);
        spawnFloorEntries(world, level, record, encounterKey, currentChunkBox, "ground", GROUND_FLOOR_Y, CULTISTS_PER_FLOOR, 0);
        spawnFloorEntries(world, level, record, encounterKey, currentChunkBox, "second", SECOND_FLOOR_Y, CULTISTS_PER_FLOOR, ADEPTS_PER_FLOOR);
        spawnFloorEntries(world, level, record, encounterKey, currentChunkBox, "third", THIRD_FLOOR_Y, CULTISTS_PER_FLOOR, ADEPTS_PER_FLOOR);
        spawnArchbishop(world, level, record, encounterKey, currentChunkBox);
        record.setInitialized(true);
    }

    private static String createEncounterKey(BlockPos origin) {
        return origin.getX() + "_" + origin.getY() + "_" + origin.getZ();
    }

    private static void spawnFloorEntries(WorldGenLevel world, ServerLevel level, StrongholdEncounterData.EncounterRecord record, String encounterKey, BoundingBox currentChunkBox, String floorName, int floorY, int cultistCount, int adeptCount)
    {
        BoundingBox bounds = record.getBounds();

        for (int i = 0; i < cultistCount; i++) {
            String spawnId = floorName + "_cultist_" + i;

            attemptSpawn(world, level, record, encounterKey, currentChunkBox, bounds, floorY, spawnId, RegistryHandler.CULTIST.get());
        }

        for (int i = 0; i < adeptCount; i++) {
            String spawnId = floorName + "_adept_" + i;

            attemptSpawn(world, level, record, encounterKey, currentChunkBox, bounds, floorY, spawnId, RegistryHandler.ADEPT.get());
        }
    }

    private static void spawnArchbishop(WorldGenLevel world, ServerLevel level, StrongholdEncounterData.EncounterRecord record, String encounterKey, BoundingBox currentChunkBox)
    {
        String spawnId = "initial_archbishop";

        if (record.hasSpawnedEntry(spawnId)) {
            return;
        }

        BlockPos spawnPosition = findArchbishopSpawnPosition(world, record.getBounds(), record.getOrigin(), spawnId);

        if (spawnPosition == null) {
            System.out.println("[Cult Encounter] No valid Archbishop spawn position found.");
            return;
        }

        int spawnChunkX = spawnPosition.getX() >> 4;
        int spawnChunkZ = spawnPosition.getZ() >> 4;
        int currentChunkX = currentChunkBox.minX() >> 4;
        int currentChunkZ = currentChunkBox.minZ() >> 4;

        if (spawnChunkX != currentChunkX || spawnChunkZ != currentChunkZ) {
            return;
        }

        if (spawnMob(level, RegistryHandler.ARCHBISHOP.get(), spawnPosition, encounterKey, "archbishop"))
        {
            record.markEntrySpawned(spawnId);
            record.setArchbishopAlive(true);

            System.out.println("[Cult Encounter] Archbishop spawned at: " + spawnPosition);
        }
    }

    private static void attemptSpawn(WorldGenLevel world, ServerLevel level, StrongholdEncounterData.EncounterRecord record, String encounterKey, BoundingBox currentChunkBox, BoundingBox bounds, int floorY, String spawnId, EntityType<? extends Mob> entityType)
    {
        if (record.hasSpawnedEntry(spawnId)) {
            return;
        }

        BlockPos spawnPosition = getSpawnPosition(bounds, record.getOrigin(), floorY, spawnId);

        if (!currentChunkBox.isInside(spawnPosition)) {
            return;
        }

        if (!hasValidSpawnPosition(world, spawnPosition)) {
            return;
        }

        if (spawnMob(level, entityType, spawnPosition, encounterKey, entityType == RegistryHandler.CULTIST.get() ? "cultist" : "adept"))
        {
            record.markEntrySpawned(spawnId);
        }
    }

    private static BlockPos getSpawnPosition(BoundingBox bounds, BlockPos origin, int floorY, String spawnId)
    {
        long seed = origin.asLong() ^ spawnId.hashCode();
        RandomSource random = RandomSource.create(seed);
        int minX = bounds.minX() + 2;
        int maxX = bounds.maxX() - 2;
        int minZ = bounds.minZ() + 2;
        int maxZ = bounds.maxZ() - 2;
        int x = minX + random.nextInt(Math.max(1, maxX - minX + 1));
        int z = minZ + random.nextInt(Math.max(1, maxZ - minZ + 1));
        int y = origin.getY() + floorY + 1;
        return new BlockPos(x, y, z);
    }

    private static boolean hasValidSpawnPosition(LevelAccessor level, BlockPos position) {
        BlockPos floor = position.below();

        BlockState floorState = level.getBlockState(floor);
        BlockState spawnState = level.getBlockState(position);
        BlockState aboveState = level.getBlockState(position.above());

        boolean solidFloor = floorState.isSolidRender(level, floor);
        boolean spawnAir = spawnState.isAir();
        boolean aboveAir = aboveState.isAir();
        boolean spawnFluid = level.getFluidState(position).isEmpty();
        boolean aboveFluid = level.getFluidState(position.above()).isEmpty();

        System.out.println("[Cult Encounter] Archbishop spawn validation:");
        System.out.println("Floor: " + floorState + " | Solid: " + solidFloor);
        System.out.println("Spawn: " + spawnState + " | Air: " + spawnAir);
        System.out.println("Above: " + aboveState + " | Air: " + aboveAir);
        System.out.println("Spawn fluid empty: " + spawnFluid);
        System.out.println("Above fluid empty: " + aboveFluid);

        return solidFloor && spawnAir && aboveAir && spawnFluid && aboveFluid;
    }

    private static boolean spawnMob(ServerLevel level, EntityType<? extends Mob> entityType, BlockPos position, String encounterKey, String entityTypeName)
    {
        Mob mob = entityType.create(level);

        if (mob == null) {
            return false;
        }

        mob.moveTo(position.getX() + 0.5D, position.getY(), position.getZ() + 0.5D, level.random.nextFloat() * 360.0F, 0.0F);
        mob.getPersistentData().putString(ENCOUNTER_KEY, encounterKey);
        mob.getPersistentData().putString(ENTITY_TYPE_KEY, entityTypeName);
        return level.addFreshEntity(mob);
    }

    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        if (!(event.getEntity().level() instanceof ServerLevel level)) {
            return;
        }

        String encounterKey = event.getEntity()
                .getPersistentData()
                .getString(ENCOUNTER_KEY);

        String entityType = event.getEntity()
                .getPersistentData()
                .getString(ENTITY_TYPE_KEY);

        if (encounterKey.isEmpty()) {
            return;
        }

        StrongholdEncounterData data = StrongholdEncounterData.get(level);
        StrongholdEncounterData.EncounterRecord record = data.get(encounterKey);

        if (record == null) {
            return;
        }

        if (entityType.equals("archbishop")) {
            record.setArchbishopAlive(false);
            record.setNextArchbishopSpawnTime(level.getGameTime() + ARCHBISHOP_RESPAWN_TICKS);

            if (!record.isIsraphelAlive() && level.random.nextFloat() < ISRAPHEL_SPAWN_CHANCE) {

                BlockPos deathPosition = event.getEntity().blockPosition();

                if (spawnMob(level, RegistryHandler.ISRAPHEL.get(), deathPosition, encounterKey, "israphel"))
                {
                    record.setIsraphelAlive(true);
                }
            }
        }

        if (entityType.equals("israphel")) {
            record.setIsraphelAlive(false);
        }
    }

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }

        for (ServerLevel level : event.getServer().getAllLevels()) {
            processRespawns(level);
        }
    }

    private static void processRespawns(ServerLevel level) {
        StrongholdEncounterData data = StrongholdEncounterData.get(level);

        for (Map.Entry<String, StrongholdEncounterData.EncounterRecord> entry : data.getEncounters().entrySet()) {
            String encounterKey = entry.getKey();
            StrongholdEncounterData.EncounterRecord record = entry.getValue();

            if (record.isArchbishopAlive()) {
                continue;
            }

            long respawnTime = record.getNextArchbishopSpawnTime();

            if (respawnTime <= 0 || level.getGameTime() < respawnTime) {
                continue;
            }

            BlockPos spawnPosition = getSpawnPosition(record.getBounds(), record.getOrigin(), ROOF_Y, "initial_archbishop");

            if (!level.hasChunkAt(spawnPosition)) {
                continue;
            }

            if (!hasValidSpawnPosition(level, spawnPosition)) {
                continue;
            }

            if (spawnMob(level, RegistryHandler.ARCHBISHOP.get(), spawnPosition, encounterKey, "archbishop"))
            {
                record.setArchbishopAlive(true);
                record.setNextArchbishopSpawnTime(0);
            }
        }
    }

    private static BlockPos findArchbishopSpawnPosition(LevelAccessor level, BoundingBox bounds, BlockPos origin, String spawnId)
    {
        BlockPos preferredPosition = getSpawnPosition(bounds, origin, ROOF_Y, spawnId);

        int minX = bounds.minX() + 2;
        int maxX = bounds.maxX() - 2;
        int minZ = bounds.minZ() + 2;
        int maxZ = bounds.maxZ() - 2;

        for (int radius = 0; radius <= 12; radius++) {
            for (int dx = -radius; dx <= radius; dx++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    if (Math.abs(dx) != radius && Math.abs(dz) != radius && radius != 0) {
                        continue;
                    }

                    int x = preferredPosition.getX() + dx;
                    int z = preferredPosition.getZ() + dz;

                    if (x < minX || x > maxX || z < minZ || z > maxZ) {
                        continue;
                    }

                    for (int dy = -6; dy <= 6; dy++) {
                        BlockPos candidate = new BlockPos(x, preferredPosition.getY() + dy, z);

                        if (hasValidSpawnPosition(level, candidate)) {
                            return candidate;
                        }
                    }
                }
            }
        }

        return null;
    }
}