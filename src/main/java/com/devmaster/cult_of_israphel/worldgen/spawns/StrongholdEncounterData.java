package com.devmaster.cult_of_israphel.worldgen.spawns;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class StrongholdEncounterData extends SavedData {
    private static final String DATA_NAME = "archbishop_stronghold_encounters";
    private final Map<String, EncounterRecord> encounters = new HashMap<>();

    public static StrongholdEncounterData get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(StrongholdEncounterData::load, StrongholdEncounterData::new, DATA_NAME);
    }

    public EncounterRecord getOrCreate(String encounterKey) {
        EncounterRecord record = encounters.computeIfAbsent(encounterKey, key -> new EncounterRecord(this));
        setDirty();
        return record;
    }

    public EncounterRecord get(String encounterKey) {
        return encounters.get(encounterKey);
    }

    public Map<String, EncounterRecord> getEncounters() {
        return encounters;
    }

    public static StrongholdEncounterData load(CompoundTag tag) {
        StrongholdEncounterData data = new StrongholdEncounterData();
        ListTag encounterList = tag.getList("Encounters", CompoundTag.TAG_COMPOUND);

        for (int i = 0; i < encounterList.size(); i++) {
            CompoundTag encounterTag = encounterList.getCompound(i);
            String key = encounterTag.getString("Key");
            EncounterRecord record = new EncounterRecord(data);
            record.initialized = encounterTag.getBoolean("Initialized");
            record.archbishopAlive = encounterTag.getBoolean("ArchbishopAlive");
            record.israphelAlive = encounterTag.getBoolean("IsraphelAlive");
            record.nextArchbishopSpawnTime = encounterTag.getLong("NextArchbishopSpawnTime");
            record.origin = BlockPos.of(encounterTag.getLong("Origin"));
            record.minX = encounterTag.getInt("MinX");
            record.minY = encounterTag.getInt("MinY");
            record.minZ = encounterTag.getInt("MinZ");
            record.maxX = encounterTag.getInt("MaxX");
            record.maxY = encounterTag.getInt("MaxY");
            record.maxZ = encounterTag.getInt("MaxZ");
            ListTag spawnedList = encounterTag.getList("SpawnedEntries", CompoundTag.TAG_STRING);

            for (int j = 0; j < spawnedList.size(); j++) {
                record.spawnedEntries.add(spawnedList.getString(j));
            }

            data.encounters.put(key, record);
        }

        return data;
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        ListTag encounterList = new ListTag();

        for (Map.Entry<String, EncounterRecord> entry : encounters.entrySet()) {
            CompoundTag encounterTag = new CompoundTag();
            EncounterRecord record = entry.getValue();
            encounterTag.putString("Key", entry.getKey());
            encounterTag.putBoolean("Initialized", record.initialized);
            encounterTag.putBoolean("ArchbishopAlive", record.archbishopAlive);
            encounterTag.putBoolean("IsraphelAlive", record.israphelAlive);
            encounterTag.putLong("NextArchbishopSpawnTime", record.nextArchbishopSpawnTime);
            encounterTag.putLong("Origin", record.origin.asLong());
            encounterTag.putInt("MinX", record.minX);
            encounterTag.putInt("MinY", record.minY);
            encounterTag.putInt("MinZ", record.minZ);
            encounterTag.putInt("MaxX", record.maxX);
            encounterTag.putInt("MaxY", record.maxY);
            encounterTag.putInt("MaxZ", record.maxZ);
            ListTag spawnedList = new ListTag();

            for (String spawnId : record.spawnedEntries) {
                spawnedList.add(StringTag.valueOf(spawnId));
            }

            encounterTag.put("SpawnedEntries", spawnedList);
            encounterList.add(encounterTag);
        }

        tag.put("Encounters", encounterList);
        return tag;
    }

    public static class EncounterRecord {
        private final StrongholdEncounterData parent;
        private boolean initialized;
        private boolean archbishopAlive;
        private boolean israphelAlive;
        private long nextArchbishopSpawnTime;
        private final Set<String> spawnedEntries = new HashSet<>();
        private BlockPos origin = BlockPos.ZERO;
        private int minX;
        private int minY;
        private int minZ;
        private int maxX;
        private int maxY;
        private int maxZ;

        public EncounterRecord(StrongholdEncounterData parent) {
            this.parent = parent;
        }

        public void setStructureData(BlockPos origin, BoundingBox bounds) {
            this.origin = origin;
            this.minX = bounds.minX();
            this.minY = bounds.minY();
            this.minZ = bounds.minZ();
            this.maxX = bounds.maxX();
            this.maxY = bounds.maxY();
            this.maxZ = bounds.maxZ();
            parent.setDirty();
        }

        public BlockPos getOrigin() {
            return origin;
        }

        public BoundingBox getBounds() {
            return new BoundingBox(minX, minY, minZ, maxX, maxY, maxZ);
        }

        public boolean isInitialized() {
            return initialized;
        }

        public void setInitialized(boolean initialized) {
            this.initialized = initialized;
            parent.setDirty();
        }

        public boolean isArchbishopAlive() {
            return archbishopAlive;
        }

        public void setArchbishopAlive(boolean archbishopAlive) {
            this.archbishopAlive = archbishopAlive;
            parent.setDirty();
        }

        public boolean isIsraphelAlive() {
            return israphelAlive;
        }

        public void setIsraphelAlive(boolean israphelAlive) {
            this.israphelAlive = israphelAlive;
            parent.setDirty();
        }

        public long getNextArchbishopSpawnTime() {
            return nextArchbishopSpawnTime;
        }

        public void setNextArchbishopSpawnTime(long nextArchbishopSpawnTime) {
            this.nextArchbishopSpawnTime = nextArchbishopSpawnTime;
            parent.setDirty();
        }

        public boolean hasSpawnedEntry(String spawnId) {
            return spawnedEntries.contains(spawnId);
        }

        public boolean markEntrySpawned(String spawnId) {
            boolean added = spawnedEntries.add(spawnId);

            if (added) {
                parent.setDirty();
            }

            return added;
        }
    }
}
