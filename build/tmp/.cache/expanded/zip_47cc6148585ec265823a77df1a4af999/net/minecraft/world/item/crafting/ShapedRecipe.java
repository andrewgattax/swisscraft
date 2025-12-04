package net.minecraft.world.item.crafting;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class ShapedRecipe implements CraftingRecipe, net.minecraftforge.common.crafting.IShapedRecipe<CraftingContainer> {
   static int MAX_WIDTH = 3;
   static int MAX_HEIGHT = 3;
   /**
    * Expand the max width and height allowed in the deserializer.
    * This should be called by modders who add custom crafting tables that are larger than the vanilla 3x3.
    * @param width your max recipe width
    * @param height your max recipe height
    */
   public static void setCraftingSize(int width, int height) {
      if (MAX_WIDTH < width) MAX_WIDTH = width;
      if (MAX_HEIGHT < height) MAX_HEIGHT = height;
   }

   final ShapedRecipePattern pattern;
   final ItemStack result;
   final String group;
   final CraftingBookCategory category;
   final boolean showNotification;

   public ShapedRecipe(String p_250221_, CraftingBookCategory p_250716_, ShapedRecipePattern p_312200_, ItemStack p_248581_, boolean p_310619_) {
      this.group = p_250221_;
      this.category = p_250716_;
      this.pattern = p_312200_;
      this.result = p_248581_;
      this.showNotification = p_310619_;
   }

   public ShapedRecipe(String p_272759_, CraftingBookCategory p_273506_, ShapedRecipePattern p_310709_, ItemStack p_272852_) {
      this(p_272759_, p_273506_, p_310709_, p_272852_, true);
   }

   public RecipeSerializer<?> getSerializer() {
      return RecipeSerializer.SHAPED_RECIPE;
   }

   public String getGroup() {
      return this.group;
   }

   @Override
   public int getRecipeWidth() {
      return getWidth();
   }

   public CraftingBookCategory category() {
      return this.category;
   }

   @Override
   public int getRecipeHeight() {
      return getHeight();
   }

   public ItemStack getResultItem(RegistryAccess p_266881_) {
      return this.result;
   }

   public NonNullList<Ingredient> getIngredients() {
      return this.pattern.ingredients();
   }

   public boolean showNotification() {
      return this.showNotification;
   }

   public boolean canCraftInDimensions(int p_44161_, int p_44162_) {
      return p_44161_ >= this.pattern.width() && p_44162_ >= this.pattern.height();
   }

   public boolean matches(CraftingContainer p_44176_, Level p_44177_) {
      return this.pattern.matches(p_44176_);
   }

   public ItemStack assemble(CraftingContainer p_266686_, RegistryAccess p_266725_) {
      return this.getResultItem(p_266725_).copy();
   }

   public int getWidth() {
      return this.pattern.width();
   }

   public int getHeight() {
      return this.pattern.height();
   }

   public boolean isIncomplete() {
      NonNullList<Ingredient> nonnulllist = this.getIngredients();
      return nonnulllist.isEmpty() || nonnulllist.stream().filter((p_151277_) -> {
         return !p_151277_.isEmpty();
      }).anyMatch((p_151273_) -> {
         return net.minecraftforge.common.ForgeHooks.hasNoElements(p_151273_);
      });
   }

   public static class Serializer implements RecipeSerializer<ShapedRecipe> {
      public static final Codec<ShapedRecipe> CODEC = RecordCodecBuilder.create((p_309256_) -> {
         return p_309256_.group(ExtraCodecs.strictOptionalField(Codec.STRING, "group", "").forGetter((p_309251_) -> {
            return p_309251_.group;
         }), CraftingBookCategory.CODEC.fieldOf("category").orElse(CraftingBookCategory.MISC).forGetter((p_309253_) -> {
            return p_309253_.category;
         }), ShapedRecipePattern.MAP_CODEC.forGetter((p_309254_) -> {
            return p_309254_.pattern;
         }), ItemStack.ITEM_WITH_COUNT_CODEC.fieldOf("result").forGetter((p_309252_) -> {
            return p_309252_.result;
         }), ExtraCodecs.strictOptionalField(Codec.BOOL, "show_notification", true).forGetter((p_309255_) -> {
            return p_309255_.showNotification;
         })).apply(p_309256_, ShapedRecipe::new);
      });

      public Codec<ShapedRecipe> codec() {
         return CODEC;
      }

      public ShapedRecipe fromNetwork(FriendlyByteBuf p_44234_) {
         String s = p_44234_.readUtf();
         CraftingBookCategory craftingbookcategory = p_44234_.readEnum(CraftingBookCategory.class);
         ShapedRecipePattern shapedrecipepattern = ShapedRecipePattern.fromNetwork(p_44234_);
         ItemStack itemstack = p_44234_.readItem();
         boolean flag = p_44234_.readBoolean();
         return new ShapedRecipe(s, craftingbookcategory, shapedrecipepattern, itemstack, flag);
      }

      public void toNetwork(FriendlyByteBuf p_44227_, ShapedRecipe p_44228_) {
         p_44227_.writeUtf(p_44228_.group);
         p_44227_.writeEnum(p_44228_.category);
         p_44228_.pattern.toNetwork(p_44227_);
         p_44227_.writeItem(p_44228_.result);
         p_44227_.writeBoolean(p_44228_.showNotification);
      }
   }
}
