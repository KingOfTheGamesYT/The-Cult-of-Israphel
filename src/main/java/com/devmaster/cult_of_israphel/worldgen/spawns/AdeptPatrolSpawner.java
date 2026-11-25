package com.devmaster.cult_of_israphel.worldgen.spawns;

import com.devmaster.cult_of_israphel.entity.Adept;
import com.devmaster.cult_of_israphel.entity.Cultist;
import com.devmaster.cult_of_israphel.misc.RegistryHandler;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.List;
import java.util.UUID;

public class AdeptPatrolSpawner {

    private static final int SPAWN_INTERVAL = 1200;
    private static final float SPAWN_CHANCE = 0.02F;
    private static final int MIN_DISTANCE = 32;
    private static final int MAX_DISTANCE = 48;
    private static final int PATROL_CHECK_RADIUS = 128;
    private static long lastCheckedTick = Long.MIN_VALUE;

    @SubscribeEvent
    public void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }

        ServerLevel level = event.getServer().overworld();

        if (level == null) {
            return;
        }

        long currentTick = level.getGameTime();

        if (lastCheckedTick == Long.MIN_VALUE) {
            lastCheckedTick = currentTick;
            return;
        }

        if (currentTick - lastCheckedTick < SPAWN_INTERVAL) {
            return;
        }

        lastCheckedTick = currentTick;
        RandomSource random = level.getRandom();

        if (random.nextFloat() >= SPAWN_CHANCE) {
            return;
        }

        attemptSpawn(level);
    }

    private static void attemptSpawn(ServerLevel level) {
        List<ServerPlayer> players = level.players().stream()
                .filter(player -> !player.isCreative())
                .filter(player -> !player.isSpectator())
                .toList();

        if (players.isEmpty()) {
            return;
        }

        RandomSource random = level.getRandom();
        ServerPlayer player = players.get(random.nextInt(players.size()));
        AABB patrolArea = player.getBoundingBox().inflate(PATROL_CHECK_RADIUS);
        List<Adept> existingPatrols = level.getEntitiesOfClass(Adept.class, patrolArea, Entity::isAlive);

        if (!existingPatrols.isEmpty()) {
            return;
        }

        double angle = random.nextDouble() * Math.PI * 2.0;
        int distance = MIN_DISTANCE + random.nextInt(MAX_DISTANCE - MIN_DISTANCE + 1);
        int x = player.getBlockX() + (int) Math.round(Math.cos(angle) * distance);
        int z = player.getBlockZ() + (int) Math.round(Math.sin(angle) * distance);
        BlockPos spawnPos = BlockPos.containing(x, level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z), z);

        if (!level.hasChunkAt(spawnPos)) {
            return;
        }

        Adept adept = RegistryHandler.ADEPT.get().create(level);

        if (adept == null) {
            return;
        }

        UUID patrolId = UUID.randomUUID();
        adept.moveTo(x + 0.5, spawnPos.getY(), z + 0.5, random.nextFloat() * 360.0F, 0.0F);
        adept.getPersistentData().putUUID("AdeptPatrolID", patrolId);
        adept.getPersistentData().putBoolean("AdeptPatrolLeader", true);

        if (!adept.checkSpawnObstruction(level)) {
            return;
        }

        if (!level.addFreshEntity(adept)) {
            return;
        }

        int cultistCount = 3 + random.nextInt(3);

        for (int i = 0; i < cultistCount; i++) {
            spawnCultist(level, adept, patrolId, random, i);
        }
    }

    private static void spawnCultist(ServerLevel level, Adept adept, UUID patrolId, RandomSource random, int index)
    {
        Cultist cultist = RegistryHandler.CULTIST.get().create(level);

        if (cultist == null) {
            return;
        }

        double angle = random.nextDouble() * Math.PI * 2.0;
        double distance = 2.0 + random.nextDouble() * 2.0;
        double x = adept.getX() + Math.cos(angle) * distance;
        double z = adept.getZ() + Math.sin(angle) * distance;
        BlockPos spawnPos = BlockPos.containing(x, level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, (int) x, (int) z), z);

        if (!level.hasChunkAt(spawnPos)) {
            return;
        }

        cultist.moveTo(x, spawnPos.getY(), z, random.nextFloat() * 360.0F, 0.0F);
        cultist.getPersistentData().putUUID("AdeptPatrolID", patrolId);
        cultist.getPersistentData().putUUID("AdeptPatrolLeaderUUID", adept.getUUID());

        if (!cultist.checkSpawnObstruction(level)) {
            return;
        }

        level.addFreshEntity(cultist);
    }
}