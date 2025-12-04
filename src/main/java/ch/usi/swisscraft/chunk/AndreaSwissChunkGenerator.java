package ch.usi.swisscraft.chunk;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.NoiseColumn;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.blending.Blender;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class AndreaSwissChunkGenerator extends ChunkGenerator {

    public static final Codec<AndreaSwissChunkGenerator> CODEC = RecordCodecBuilder.create((instance) ->
        instance.group(
            BiomeSource.CODEC.fieldOf("biome_source").forGetter(AndreaSwissChunkGenerator::getBiomeSource)
        ).apply(instance, AndreaSwissChunkGenerator::new));

    public AndreaSwissChunkGenerator(BiomeSource biomeSource) {
        super(biomeSource);
    }

    @Override
    protected Codec<? extends ChunkGenerator> codec() {
        return CODEC;
    }

    @Override
    public @NotNull CompletableFuture<ChunkAccess> fillFromNoise(
        java.util.concurrent.@NotNull Executor executor,
        @NotNull Blender blender,
        @NotNull RandomState randomState,
        @NotNull StructureManager structureManager,
        @NotNull ChunkAccess chunk) {

        return CompletableFuture.supplyAsync(() -> {
            int chunkX = chunk.getPos().x;
            int chunkZ = chunk.getPos().z;

            Heightmap heightmap = chunk.getOrCreateHeightmapUnprimed(Heightmap.Types.OCEAN_FLOOR_WG);
            Heightmap worldGenHeightmap = chunk.getOrCreateHeightmapUnprimed(Heightmap.Types.WORLD_SURFACE_WG);

            BlockPos.MutableBlockPos mutablePos = new BlockPos.MutableBlockPos();

            for (int x = 0; x < 16; x++) {
                for (int z = 0; z < 16; z++) {
                    int realX = chunkX * 16 + x;
                    int realZ = chunkZ * 16 + z;

                    // Generazione sinusoidale semplice
                    int height = (int) (Math.sin(realX / 10.0) * 10 + 70);

                    for (int y = getMinY(); y <= height; y++) {
                        mutablePos.set(x, y, z);
                        BlockState block = Blocks.STONE.defaultBlockState();

                        if (y == height) block = Blocks.GRASS_BLOCK.defaultBlockState();
                        else if (y > height - 4) block = Blocks.DIRT.defaultBlockState();
                        else if (y == getMinY()) block = Blocks.BEDROCK.defaultBlockState();

                        chunk.setBlockState(mutablePos, block, false);
                    }

                    heightmap.update(x, height, z, Blocks.GRASS_BLOCK.defaultBlockState());
                    worldGenHeightmap.update(x, height, z, Blocks.GRASS_BLOCK.defaultBlockState());
                }
            }
            return chunk;
        });
    }

    @Override
    public int getSeaLevel() {
        return 63;
    }

    @Override
    public int getMinY() {
        return -64;
    }

    @Override
    public void buildSurface(WorldGenRegion region, StructureManager structureManager, RandomState randomState, ChunkAccess chunk) {
    }

    @Override
    public void spawnOriginalMobs(WorldGenRegion worldGenRegion) {
    }

    @Override
    public int getGenDepth() {
        return 384;
    }

    @Override
    public void applyCarvers(WorldGenRegion region, long seed, RandomState randomState, BiomeManager biomeManager, StructureManager structureManager, ChunkAccess chunk, GenerationStep.Carving step) {

    }

    private net.minecraft.world.level.levelgen.Aquifer getAquifer(ChunkAccess chunk) {
        return net.minecraft.world.level.levelgen.Aquifer.createDisabled(
            (x, y, z) -> new net.minecraft.world.level.levelgen.Aquifer.FluidStatus(getSeaLevel(), Blocks.WATER.defaultBlockState())
        );
    }

    @Override
    public int getBaseHeight(int x, int z, Heightmap.Types type, LevelHeightAccessor level, RandomState randomState) {
        return 70;
    }

    @Override
    public NoiseColumn getBaseColumn(int x, int z, LevelHeightAccessor level, RandomState randomState) {
        return new NoiseColumn(0, new BlockState[0]);
    }

    @Override
    public void addDebugScreenInfo(List<String> info, RandomState randomState, BlockPos pos) {
        info.add("SwissCraft Generator");
    }
}