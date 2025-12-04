package net.minecraft.data.tags;

import java.util.concurrent.CompletableFuture;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.damagesource.DamageTypes;

public class UpdateOneTwentyOneDamageTypeTagsProvider extends TagsProvider<DamageType> {
   public UpdateOneTwentyOneDamageTypeTagsProvider(PackOutput p_309466_, CompletableFuture<HolderLookup.Provider> p_309611_) {
      super(p_309466_, Registries.DAMAGE_TYPE, p_309611_);
   }

   protected void addTags(HolderLookup.Provider p_312660_) {
      this.tag(DamageTypeTags.BREEZE_IMMUNE_TO).add(DamageTypes.ARROW, DamageTypes.TRIDENT);
   }
}