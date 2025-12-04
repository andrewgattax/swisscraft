package net.minecraft.data.advancements.packs;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.advancements.AdvancementProvider;

public class UpdateOneTwentyOneAdvancementProvider {
   public static AdvancementProvider create(PackOutput p_309585_, CompletableFuture<HolderLookup.Provider> p_309812_) {
      return new AdvancementProvider(p_309585_, p_309812_, List.of(new UpdateOneTwentyOneAdventureAdvancements()));
   }
}