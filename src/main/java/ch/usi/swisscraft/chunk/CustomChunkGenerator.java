package ch.usi.swisscraft.chunk;

import ch.usi.swisscraft.client.ChunkDataRetriever;
import ch.usi.swisscraft.client.LineByLineRetriever;
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

public class CustomChunkGenerator extends ChunkGenerator {

    public static final Codec<CustomChunkGenerator> CODEC = RecordCodecBuilder.create((instance) ->
            instance.group(
                    BiomeSource.CODEC.fieldOf("biome_source").forGetter(CustomChunkGenerator::getBiomeSource)
            ).apply(instance, CustomChunkGenerator::new));

    public CustomChunkGenerator(BiomeSource biomeSource) {
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

            int originXlv95 = 2717922;
            int originYlv95 = 1096000;

            Heightmap heightmap = chunk.getOrCreateHeightmapUnprimed(Heightmap.Types.OCEAN_FLOOR_WG);
            Heightmap worldGenHeightmap = chunk.getOrCreateHeightmapUnprimed(Heightmap.Types.WORLD_SURFACE_WG);

            BlockPos.MutableBlockPos mutablePos = new BlockPos.MutableBlockPos();

            ChunkDataRetriever chunkDataRetriever = LineByLineRetriever.getInstance();
            List<Integer> heights = chunkDataRetriever.retrieveHeightMap(originXlv95+(chunkX * 16), originYlv95+(chunkZ * 16), 16);

            for (int x = 0; x < 16; x++) {
                for (int z = 0; z < 16; z++) {
                    int realX = chunkX * 16 + x;
                    int realZ = chunkZ * 16 + z;

                    int height = heights.get((z * 16)+ x) - 250;

                    for (int y = getMinY(); y <= height; y++) {
                        mutablePos.set(x, y, z);
                        BlockState block = selectBlockForPosition(y, height, realX, realZ);

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
    public int getBaseHeight(int p_223032_, int p_223033_, Heightmap.Types p_223034_, LevelHeightAccessor p_223035_, RandomState p_223036_) {
        return 70;
    }

    @Override
    public void buildSurface(WorldGenRegion region, StructureManager structureManager, RandomState randomState, ChunkAccess chunk) {
    }

    @Override
    public void spawnOriginalMobs(WorldGenRegion worldGenRegion) {

    }

    @Override
    public int getGenDepth() {
        return 384; // Da -64 a 320 = 384 blocks (vanilla limitations)
    }

    @Override
    public void applyCarvers(WorldGenRegion region, long seed, RandomState randomState, BiomeManager biomeManager, StructureManager structureManager, ChunkAccess chunk, GenerationStep.Carving step) {
        // Lascia vuoto per impedire la generazione di caverne standard
    }

    @Override
    public NoiseColumn getBaseColumn(int x, int z, LevelHeightAccessor level, RandomState randomState) {
        return new NoiseColumn(0, new BlockState[0]); // Versione semplificata
    }

    @Override
    public void addDebugScreenInfo(List<String> info, RandomState randomState, BlockPos pos) {
        info.add("SwissCraft Generator");
    }

    private BlockState selectBlockForPosition(int y, int surfaceHeight, int worldX, int worldZ) {
        // Bedrock in fondo
        if (y <= -60) {
            return Blocks.BEDROCK.defaultBlockState();
        }

        int depth = surfaceHeight - y;

        if (y >= 280 && depth <= 0) {
            return Blocks.PACKED_ICE.defaultBlockState();
        }

        if (y >= 240 && depth <= 0) {
            return Blocks.SNOW_BLOCK.defaultBlockState();
        }

        if (depth == 0) {
            if (y > 280) {
                return Blocks.PACKED_ICE.defaultBlockState();
            } else if (y > 240) {
                return Blocks.SNOW_BLOCK.defaultBlockState();
            } else if (y > 200) {
                return Math.sin(worldX * 0.05) > 0.3 ? Blocks.SNOW_BLOCK.defaultBlockState() : Blocks.STONE.defaultBlockState();
            } else if (y > 160) {
                return Blocks.STONE.defaultBlockState();
            } else if (y > 120) {
                return Math.sin(worldX * 0.1) > 0 ? Blocks.STONE.defaultBlockState() : Blocks.COARSE_DIRT.defaultBlockState();
            } else {
                return Blocks.GRASS_BLOCK.defaultBlockState();
            }
        }

        if (y >= 280 && depth <= 5) {
            return Blocks.PACKED_ICE.defaultBlockState();
        }

        if (y >= 240 && depth <= 5) {
            return Blocks.SNOW_BLOCK.defaultBlockState();
        }

        if (depth <= 3 && y < 160) {
            return Blocks.DIRT.defaultBlockState();
        }

        double rockNoise = Math.sin(worldX * 0.1 + worldZ * 0.1) + Math.cos(worldX * 0.05);

        if (y > 200) {
            if (rockNoise > 0.6) {
                return Blocks.STONE.defaultBlockState();
            } else if (rockNoise > 0.2) {
                return Blocks.ANDESITE.defaultBlockState();
            } else if (rockNoise > -0.2) {
                return Blocks.CALCITE.defaultBlockState();
            } else {
                return Blocks.DIORITE.defaultBlockState();
            }
        }

        if (y > 120) {
            if (rockNoise > 0.5) {
                return Blocks.ANDESITE.defaultBlockState();
            } else if (rockNoise > 0) {
                return Blocks.STONE.defaultBlockState();
            } else {
                return Blocks.DEEPSLATE.defaultBlockState();
            }
        }

        if (y > 40) {
            if (rockNoise > 0.7) {
                return Blocks.DIORITE.defaultBlockState();
            } else if (rockNoise > 0.3) {
                return Blocks.STONE.defaultBlockState();
            } else if (rockNoise > 0) {
                return Blocks.ANDESITE.defaultBlockState();
            } else {
                return Blocks.GRANITE.defaultBlockState();
            }
        }

        if (y > 0) {
            return rockNoise > 0 ? Blocks.STONE.defaultBlockState() : Blocks.DEEPSLATE.defaultBlockState();
        }

        return Blocks.DEEPSLATE.defaultBlockState();
    }
}
