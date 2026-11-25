package com.devmaster.cult_of_israphel.worldgen.spawns;

import com.devmaster.cult_of_israphel.entity.Israphel;
import com.devmaster.cult_of_israphel.misc.RegistryHandler;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.List;

public class IsraphelPatrolSpawner {

    private static final int SPAWN_TICKS = 1200;
    private static final float DAILY_SPAWN_CHANCE = 0.02F;
    private long lastCheckedDay = Long.MIN_VALUE;

    @SubscribeEvent
    public void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }

        ServerLevel level = event.getServer().overworld();
        long currentDay = Math.floorDiv(level.getDayTime(), SPAWN_TICKS);

        if (lastCheckedDay == Long.MIN_VALUE) {
            lastCheckedDay = currentDay;
            return;
        }

        if (currentDay == lastCheckedDay) {
            return;
        }

        lastCheckedDay = currentDay;
        RandomSource random = level.getRandom();

        if (random.nextFloat() >= DAILY_SPAWN_CHANCE) {
            return;
        }
        attemptSpawn(level);
    }

    private void attemptSpawn(ServerLevel level) {
        for (Entity entity : level.getAllEntities()) {
            if (entity instanceof Israphel && entity.isAlive()) {
                return;
            }
        }

        List<ServerPlayer> players = level.players().stream()
                .filter(player -> !player.isSpectator())
                .filter(player -> !player.isCreative())
                .toList();

        if (players.isEmpty()) {
            return;
        }

        RandomSource random = level.getRandom();
        ServerPlayer player = players.get(random.nextInt(players.size()));
        double angle = random.nextDouble() * Math.PI * 2.0;
        int distance = 32 + random.nextInt(17);
        int x = player.getBlockX() + (int) Math.round(Math.cos(angle) * distance);
        int z = player.getBlockZ() + (int) Math.round(Math.sin(angle) * distance);
        BlockPos spawnPos = BlockPos.containing(x, level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z), z);

        if (!level.hasChunkAt(spawnPos)) {
            return;
        }

        Israphel israphel = RegistryHandler.ISRAPHEL.get().create(level);

        if (israphel == null) {
            return;
        }

        israphel.moveTo(x + 0.5, spawnPos.getY(), z + 0.5, random.nextFloat() * 360.0F, 0.0F);

        if (!israphel.checkSpawnObstruction(level)) {
            return;
        }
        level.addFreshEntity(israphel);
    }
}