package net.minecraft.data.tags;

import java.util.concurrent.CompletableFuture;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.level.biome.Biome;

public class UpdateOneTwentyOneBiomeTagsProvider extends TagsProvider<Biome> {
   public UpdateOneTwentyOneBiomeTagsProvider(PackOutput p_310637_, CompletableFuture<HolderLookup.Provider> p_311863_, CompletableFuture<TagsProvider.TagLookup<Biome>> p_310269_) {
      super(p_310637_, Registries.BIOME, p_311863_, p_310269_);
   }

   protected void addTags(HolderLookup.Provider p_312031_) {
      this.tag(BiomeTags.HAS_TRIAL_CHAMBERS).addTag(BiomeTags.IS_OVERWORLD);
   }
}