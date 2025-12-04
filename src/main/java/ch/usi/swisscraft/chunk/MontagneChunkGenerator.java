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

public class MontagneChunkGenerator extends ChunkGenerator {

    // Necessario per la serializzazione (salvataggio/caricamento configurazione mondo)
    public static final Codec<MontagneChunkGenerator> CODEC = RecordCodecBuilder.create((instance) ->
            instance.group(
                    BiomeSource.CODEC.fieldOf("biome_source").forGetter(MontagneChunkGenerator::getBiomeSource)
            ).apply(instance, MontagneChunkGenerator::new));

    public MontagneChunkGenerator(BiomeSource biomeSource) {
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
            // Genera le montagne procedurali
            generateMountains(chunk);
            return chunk;
        });
    }

    @Override
    public int getSeaLevel() {
        return 0;
    }

    @Override
    public int getMinY() {
        return -64;
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
        return 384; // Da -64 a 320 = 384 blocchi (limiti vanilla)
    }

    @Override
    public void applyCarvers(WorldGenRegion region, long seed, RandomState randomState, BiomeManager biomeManager, StructureManager structureManager, ChunkAccess chunk, GenerationStep.Carving step) {
        // Lascia vuoto per impedire la generazione di caverne standard
    }
    
    // Metodi obbligatori di utilità per il calcolo delle altezze (usati dai mob spawn, strutture, ecc)
    @Override
    public int getBaseHeight(int x, int z, Heightmap.Types type, LevelHeightAccessor level, RandomState randomState) {
         // Calcola l'altezza usando montagne LARGHE E MASSIVE (massimizzando vanilla)
         double noise1 = Math.sin(x / 400.0) * Math.cos(z / 400.0);
         double noise2 = Math.sin(x / 200.0 + 100) * Math.sin(z / 200.0);
         double noise3 = Math.cos(x / 600.0) * Math.sin(z / 600.0);
         double noise4 = Math.sin(x / 1000.0) * Math.cos(z / 1000.0);
         double noise5 = Math.cos(x / 150.0) * Math.cos(z / 150.0);
         int height = 150 + (int) (noise1 * 80 + noise2 * 60 + noise3 * 90 + noise4 * 70 + noise5 * 40);
         return Math.max(80, Math.min(height, 310));
    }

    @Override
    public NoiseColumn getBaseColumn(int x, int z, LevelHeightAccessor level, RandomState randomState) {
        return new NoiseColumn(0, new BlockState[0]); // Versione semplificata
    }

    @Override
    public void addDebugScreenInfo(List<String> info, RandomState randomState, BlockPos pos) {
        info.add("SwissCraft Generator");
    }

    /**
     * Genera montagne procedurali con una varietà di blocchi
     * @param chunk Il chunk da riempire
     */
    private void generateMountains(ChunkAccess chunk) {
        int chunkX = chunk.getPos().x;
        int chunkZ = chunk.getPos().z;

        Heightmap heightmap = chunk.getOrCreateHeightmapUnprimed(Heightmap.Types.OCEAN_FLOOR_WG);
        BlockPos.MutableBlockPos mutablePos = new BlockPos.MutableBlockPos();

        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                int realX = chunkX * 16 + x;
                int realZ = chunkZ * 16 + z;

                // Calcola l'altezza usando una combinazione di funzioni trigonometriche
                // per creare MONTAGNE MASSIVE e LARGHE (massimizzando i limiti vanilla)
                // Usiamo scale MOLTO più grandi per montagne più larghe e imponenti
                double noise1 = Math.sin(realX / 400.0) * Math.cos(realZ / 400.0);       // Forma base MASSIVA
                double noise2 = Math.sin(realX / 200.0 + 100) * Math.sin(realZ / 200.0); // Variazione media
                double noise3 = Math.cos(realX / 600.0) * Math.sin(realZ / 600.0);       // Catene enormi
                double noise4 = Math.sin(realX / 1000.0) * Math.cos(realZ / 1000.0);     // Catene continentali
                double noise5 = Math.cos(realX / 150.0) * Math.cos(realZ / 150.0);       // Dettagli

                // Combina i rumori per creare montagne LARGHE E ALTE che sfruttano il limite di 320
                // Le scale più grandi creano montagne più "estese" orizzontalmente
                int height = 150 + (int) (
                    noise1 * 80 +    // Variazione base ampia
                    noise2 * 60 +    // Picchi secondari
                    noise3 * 90 +    // Catene larghe
                    noise4 * 70 +    // Variazioni continentali
                    noise5 * 40      // Dettagli superficie
                );

                // Limita l'altezza ai limiti vanilla (ma montagne MOLTO LARGHE)
                height = Math.max(80, Math.min(height, 310));

                // Genera la colonna di blocchi
                for (int y = -64; y <= height; y++) {
                    mutablePos.set(x, y, z);
                    BlockState block = selectBlockForPosition(y, height, realX, realZ);
                    chunk.setBlockState(mutablePos, block, false);
                }

                // Aggiorna l'heightmap
                heightmap.update(x, height, z, chunk.getBlockState(mutablePos.set(x, height, z)));
            }
        }
    }

    /**
     * Seleziona intelligentemente il tipo di blocco in base alla posizione e all'altezza
     * Ottimizzato per altezze vanilla (fino a 320)
     */
    private BlockState selectBlockForPosition(int y, int surfaceHeight, int worldX, int worldZ) {
        // Bedrock in fondo
        if (y <= -60) {
            return Blocks.BEDROCK.defaultBlockState();
        }

        // Calcola la profondità dalla superficie
        int depth = surfaceHeight - y;

        // Ghiaccio compatto sulle vette più alte (sopra 280)
        if (y >= 280 && depth <= 0) {
            return Blocks.PACKED_ICE.defaultBlockState();
        }

        // Neve sulle cime alte (sopra 240)
        if (y >= 240 && depth <= 0) {
            return Blocks.SNOW_BLOCK.defaultBlockState();
        }

        // Superficie: varia in base all'altezza
        if (depth == 0) {
            if (y > 280) {
                // Ghiaccio compatto alle vette più alte
                return Blocks.PACKED_ICE.defaultBlockState();
            } else if (y > 240) {
                // Neve permanente
                return Blocks.SNOW_BLOCK.defaultBlockState();
            } else if (y > 200) {
                // Neve e pietra miste
                return Math.sin(worldX * 0.05) > 0.3 ? Blocks.SNOW_BLOCK.defaultBlockState() : Blocks.STONE.defaultBlockState();
            } else if (y > 160) {
                // Pietra nuda alle alte altitudini
                return Blocks.STONE.defaultBlockState();
            } else if (y > 120) {
                // Mix di pietra e terra rocciosa
                return Math.sin(worldX * 0.1) > 0 ? Blocks.STONE.defaultBlockState() : Blocks.COARSE_DIRT.defaultBlockState();
            } else {
                // Erba normale
                return Blocks.GRASS_BLOCK.defaultBlockState();
            }
        }

        // Strati di ghiaccio sotto la superficie sopra i 280
        if (y >= 280 && depth <= 5) {
            return Blocks.PACKED_ICE.defaultBlockState();
        }

        // Strati di neve sotto la superficie sopra i 240
        if (y >= 240 && depth <= 5) {
            return Blocks.SNOW_BLOCK.defaultBlockState();
        }

        // Strato di terra sotto l'erba (solo alle basse altitudini)
        if (depth <= 3 && y < 160) {
            return Blocks.DIRT.defaultBlockState();
        }

        // Usa il rumore per variare i tipi di pietra
        double rockNoise = Math.sin(worldX * 0.1 + worldZ * 0.1) + Math.cos(worldX * 0.05);

        // Rocce montane alte (sopra 200)
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

        // Mix di pietre alle medie altitudini (120-200)
        if (y > 120) {
            if (rockNoise > 0.5) {
                return Blocks.ANDESITE.defaultBlockState();
            } else if (rockNoise > 0) {
                return Blocks.STONE.defaultBlockState();
            } else {
                return Blocks.DEEPSLATE.defaultBlockState();
            }
        }

        // Mix di pietre a media altezza (40-120)
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

        // Deepslate e pietra in profondità
        if (y > 0) {
            return rockNoise > 0 ? Blocks.STONE.defaultBlockState() : Blocks.DEEPSLATE.defaultBlockState();
        }

        // Tutto deepslate sotto y=0
        return Blocks.DEEPSLATE.defaultBlockState();
    }
}
