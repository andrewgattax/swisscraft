package net.minecraft.data.recipes;

import javax.annotation.Nullable;
import net.minecraft.advancements.Criterion;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.level.ItemLike;

public interface RecipeBuilder {
   ResourceLocation ROOT_RECIPE_ADVANCEMENT = new ResourceLocation("recipes/root");

   RecipeBuilder unlockedBy(String p_176496_, Criterion<?> p_297505_);

   RecipeBuilder group(@Nullable String p_176495_);

   Item getResult();

   void save(RecipeOutput p_298791_, ResourceLocation p_176504_);

   default void save(RecipeOutput p_298540_) {
      this.save(p_298540_, getDefaultRecipeId(this.getResult()));
   }

   default void save(RecipeOutput p_300884_, String p_176502_) {
      ResourceLocation resourcelocation = getDefaultRecipeId(this.getResult());
      ResourceLocation resourcelocation1 = new ResourceLocation(p_176502_);
      if (resourcelocation1.equals(resourcelocation)) {
         throw new IllegalStateException("Recipe " + p_176502_ + " should remove its 'save' argument as it is equal to default one");
      } else {
         this.save(p_300884_, resourcelocation1);
      }
   }

   static ResourceLocation getDefaultRecipeId(ItemLike p_176494_) {
      return BuiltInRegistries.ITEM.getKey(p_176494_.asItem());
   }

   static CraftingBookCategory determineBookCategory(RecipeCategory p_313042_) {
      CraftingBookCategory craftingbookcategory;
      switch (p_313042_) {
         case BUILDING_BLOCKS:
            craftingbookcategory = CraftingBookCategory.BUILDING;
            break;
         case TOOLS:
         case COMBAT:
            craftingbookcategory = CraftingBookCategory.EQUIPMENT;
            break;
         case REDSTONE:
            craftingbookcategory = CraftingBookCategory.REDSTONE;
            break;
         default:
            craftingbookcategory = CraftingBookCategory.MISC;
      }

      return craftingbookcategory;
   }
}