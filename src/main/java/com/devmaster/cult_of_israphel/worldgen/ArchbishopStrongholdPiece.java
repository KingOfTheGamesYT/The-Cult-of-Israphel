package com.devmaster.cult_of_israphel.worldgen;

import com.devmaster.cult_of_israphel.misc.RegistryHandler;
import com.devmaster.cult_of_israphel.worldgen.spawns.ArchbishopEncounterController;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;


public class ArchbishopStrongholdPiece extends StructurePiece {

    private static final ResourceLocation TEMPLATE = new ResourceLocation("cult_of_israphel", "archbishop_stronghold");
    private final BlockPos position;

    public ArchbishopStrongholdPiece(BlockPos position, BoundingBox bounds) {
        super(RegistryHandler.ARCHBISHOP_STRONGHOLD_PIECE.get(), 0, bounds);
        this.position = position;
    }

    public ArchbishopStrongholdPiece(StructurePieceSerializationContext context, CompoundTag tag)
    {
        super(RegistryHandler.ARCHBISHOP_STRONGHOLD_PIECE.get(), tag);
        this.position = BlockPos.of(tag.getLong("Position"));
    }

    @Override
    protected void addAdditionalSaveData(StructurePieceSerializationContext context, CompoundTag tag)
    {
        tag.putLong("Position", position.asLong());
    }

    @Override
    public void postProcess(WorldGenLevel level, StructureManager structureManager, ChunkGenerator generator, RandomSource random, BoundingBox box, ChunkPos chunkPos, BlockPos reference)
    {
        StructureTemplate template = level.getLevel()
                .getServer()
                .getStructureManager()
                .get(TEMPLATE)
                .orElse(null);

        if (template == null) {
            return;
        }

        StructurePlaceSettings settings = new StructurePlaceSettings().setBoundingBox(box);
        template.placeInWorld(level, position, position, settings, random, 2);
        ArchbishopEncounterController.processStructure(level, position, getBoundingBox(), box);
    }
}


