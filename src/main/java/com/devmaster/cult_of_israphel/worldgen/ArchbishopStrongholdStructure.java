package com.devmaster.cult_of_israphel.worldgen;

import com.devmaster.cult_of_israphel.misc.RegistryHandler;

import com.mojang.serialization.Codec;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

import java.util.Optional;

public class ArchbishopStrongholdStructure extends Structure {

    public static final Codec<ArchbishopStrongholdStructure> CODEC = simpleCodec(ArchbishopStrongholdStructure::new);

    public ArchbishopStrongholdStructure(Structure.StructureSettings settings) {
        super(settings);
    }

    @Override
    public Optional<Structure.GenerationStub> findGenerationPoint(GenerationContext context) {
        ChunkPos chunkPos = context.chunkPos();
        int x = chunkPos.getMinBlockX();
        int z = chunkPos.getMinBlockZ();
        int y = context.chunkGenerator().getFirstOccupiedHeight(x, z, Heightmap.Types.WORLD_SURFACE_WG, context.heightAccessor(), context.randomState());
        BlockPos position = new BlockPos(x, y, z);
        ResourceLocation templateLocation = new ResourceLocation("cult_of_israphel", "archbishop_stronghold");
        StructureTemplate template = context.structureTemplateManager().getOrCreate(templateLocation);
        StructurePlaceSettings settings = new StructurePlaceSettings();
        BoundingBox bounds = template.getBoundingBox(settings, position);
        return Optional.of(new Structure.GenerationStub(position, builder -> builder.addPiece(new ArchbishopStrongholdPiece(position, bounds))));
    }

    @Override
    public StructureType<?> type() {
        return RegistryHandler.ARCHBISHOP_STRONGHOLD.get();
    }
}
