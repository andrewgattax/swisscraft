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

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class SwissChunkGenerator extends ChunkGenerator {

    // Necessario per la serializzazione (salvataggio/caricamento configurazione mondo)
    public static final Codec<SwissChunkGenerator> CODEC = RecordCodecBuilder.create((instance) ->
            instance.group(
                    BiomeSource.CODEC.fieldOf("biome_source").forGetter(SwissChunkGenerator::getBiomeSource)
            ).apply(instance, SwissChunkGenerator::new));

    public SwissChunkGenerator(BiomeSource biomeSource) {
        super(biomeSource);
    }

    @Override
    protected Codec<? extends ChunkGenerator> codec() {
        return CODEC;
    }

    // Questo metodo determina la forma base del terreno (pietra, acqua, aria)
    @Override
    public CompletableFuture<ChunkAccess> fillFromNoise(
            java.util.concurrent.Executor executor, 
            Blender blender, 
            RandomState randomState, 
            StructureManager structureManager, 
            ChunkAccess chunk) {
        
        return CompletableFuture.supplyAsync(() -> {
            // Qui inserisci la tua logica PROCEDURALE
            int chunkX = chunk.getPos().x;
            int chunkZ = chunk.getPos().z;

            Heightmap heightmap = chunk.getOrCreateHeightmapUnprimed(Heightmap.Types.OCEAN_FLOOR_WG);
            
            BlockPos.MutableBlockPos mutablePos = new BlockPos.MutableBlockPos();

            // Iteriamo su ogni blocco del chunk (16x16)
            for (int x = 0; x < 16; x++) {
                for (int z = 0; z < 16; z++) {
                    int realX = chunkX * 16 + x;
                    int realZ = chunkZ * 16 + z;

                    // ESEMPIO: Generazione piatta personalizzata o basata su una tua formula matematica
                    // Qui potresti chiamare una API esterna o usare il tuo algoritmo Noise
                    int height = (int) (Math.sin(realX / 10.0) * 10 + 64); 

                    for (int y = -64; y < height; y++) {
                        mutablePos.set(x, y, z);
                        BlockState block = Blocks.STONE.defaultBlockState();
                        
                        if (y == height - 1) block = Blocks.GRASS_BLOCK.defaultBlockState();
                        else if (y < 0) block = Blocks.BEDROCK.defaultBlockState();
                        
                        chunk.setBlockState(mutablePos, block, false);
                    }
                    
                    // Aggiorna l'heightmap (importante per spawn e luce)
                    heightmap.update(x, height, z, Blocks.GRASS_BLOCK.defaultBlockState());
                }
            }
            return chunk;
        });
    }

    @Override
    public int getSeaLevel() {
        return 0;
    }

    @Override
    public int getMinY() {
        return 0;
    }

    @Override
    public void buildSurface(WorldGenRegion region, StructureManager structureManager, RandomState randomState, ChunkAccess chunk) {
        // Solitamente usato per mettere terra/erba sopra la pietra.
        // Se fai tutto in fillFromNoise, puoi lasciare questo vuoto o chiamare super se vuoi comportamenti vanilla parziali.
    }

    @Override
    public void spawnOriginalMobs(WorldGenRegion worldGenRegion) {

    }

    @Override
    public int getGenDepth() {
        return 0;
    }

    @Override
    public void applyCarvers(WorldGenRegion region, long seed, RandomState randomState, BiomeManager biomeManager, StructureManager structureManager, ChunkAccess chunk, GenerationStep.Carving step) {
        // Lascia vuoto per impedire la generazione di caverne standard
    }
    
    // Metodi obbligatori di utilità per il calcolo delle altezze (usati dai mob spawn, strutture, ecc)
    @Override
    public int getBaseHeight(int x, int z, Heightmap.Types type, LevelHeightAccessor level, RandomState randomState) {
         return 64; // Ritorna l'altezza calcolata dalla tua formula per quella X,Z
    }

    @Override
    public NoiseColumn getBaseColumn(int x, int z, LevelHeightAccessor level, RandomState randomState) {
        return new NoiseColumn(0, new BlockState[0]); // Versione semplificata
    }

    @Override
    public void addDebugScreenInfo(List<String> info, RandomState randomState, BlockPos pos) {
        info.add("SwissCraft Generator");
    }
}
