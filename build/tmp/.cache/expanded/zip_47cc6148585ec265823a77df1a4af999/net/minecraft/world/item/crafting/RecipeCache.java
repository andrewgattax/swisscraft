package net.minecraft.world.item.crafting;

import java.lang.ref.WeakReference;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.core.NonNullList;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class RecipeCache {
   private final RecipeCache.Entry[] entries;
   private WeakReference<RecipeManager> cachedRecipeManager = new WeakReference<>((RecipeManager)null);

   public RecipeCache(int p_309405_) {
      this.entries = new RecipeCache.Entry[p_309405_];
   }

   public Optional<CraftingRecipe> get(Level p_311354_, CraftingContainer p_310846_) {
      if (p_310846_.isEmpty()) {
         return Optional.empty();
      } else {
         this.validateRecipeManager(p_311354_);

         for(int i = 0; i < this.entries.length; ++i) {
            RecipeCache.Entry recipecache$entry = this.entries[i];
            if (recipecache$entry != null && recipecache$entry.matches(p_310846_.getItems())) {
               this.moveEntryToFront(i);
               return Optional.ofNullable(recipecache$entry.value());
            }
         }

         return this.compute(p_310846_, p_311354_);
      }
   }

   private void validateRecipeManager(Level p_310788_) {
      RecipeManager recipemanager = p_310788_.getRecipeManager();
      if (recipemanager != this.cachedRecipeManager.get()) {
         this.cachedRecipeManager = new WeakReference<>(recipemanager);
         Arrays.fill(this.entries, (Object)null);
      }

   }

   private Optional<CraftingRecipe> compute(CraftingContainer p_309716_, Level p_309968_) {
      Optional<RecipeHolder<CraftingRecipe>> optional = p_309968_.getRecipeManager().getRecipeFor(RecipeType.CRAFTING, p_309716_, p_309968_);
      this.insert(p_309716_.getItems(), optional.map(RecipeHolder::value).orElse((CraftingRecipe)null));
      return optional.map(RecipeHolder::value);
   }

   private void moveEntryToFront(int p_309395_) {
      if (p_309395_ > 0) {
         RecipeCache.Entry recipecache$entry = this.entries[p_309395_];
         System.arraycopy(this.entries, 0, this.entries, 1, p_309395_);
         this.entries[0] = recipecache$entry;
      }

   }

   private void insert(List<ItemStack> p_313121_, @Nullable CraftingRecipe p_311497_) {
      NonNullList<ItemStack> nonnulllist = NonNullList.withSize(p_313121_.size(), ItemStack.EMPTY);

      for(int i = 0; i < p_313121_.size(); ++i) {
         nonnulllist.set(i, p_313121_.get(i).copyWithCount(1));
      }

      System.arraycopy(this.entries, 0, this.entries, 1, this.entries.length - 1);
      this.entries[0] = new RecipeCache.Entry(nonnulllist, p_311497_);
   }

   static record Entry(NonNullList<ItemStack> key, @Nullable CraftingRecipe value) {
      public boolean matches(List<ItemStack> p_311947_) {
         if (this.key.size() != p_311947_.size()) {
            return false;
         } else {
            for(int i = 0; i < this.key.size(); ++i) {
               if (!ItemStack.isSameItemSameTags(this.key.get(i), p_311947_.get(i))) {
                  return false;
               }
            }

            return true;
         }
      }
   }
}