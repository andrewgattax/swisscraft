package net.minecraft.world.item.crafting;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;

public abstract class SingleItemRecipe implements Recipe<Container> {
   protected final Ingredient ingredient;
   protected final ItemStack result;
   private final RecipeType<?> type;
   private final RecipeSerializer<?> serializer;
   protected final String group;

   public SingleItemRecipe(RecipeType<?> p_44416_, RecipeSerializer<?> p_44417_, String p_44419_, Ingredient p_44420_, ItemStack p_44421_) {
      this.type = p_44416_;
      this.serializer = p_44417_;
      this.group = p_44419_;
      this.ingredient = p_44420_;
      this.result = p_44421_;
   }

   public RecipeType<?> getType() {
      return this.type;
   }

   public RecipeSerializer<?> getSerializer() {
      return this.serializer;
   }

   public String getGroup() {
      return this.group;
   }

   public ItemStack getResultItem(RegistryAccess p_266964_) {
      return this.result;
   }

   public NonNullList<Ingredient> getIngredients() {
      NonNullList<Ingredient> nonnulllist = NonNullList.create();
      nonnulllist.add(this.ingredient);
      return nonnulllist;
   }

   public boolean canCraftInDimensions(int p_44424_, int p_44425_) {
      return true;
   }

   public ItemStack assemble(Container p_44427_, RegistryAccess p_266999_) {
      return this.result.copy();
   }

   public interface Factory<T extends SingleItemRecipe> {
      T create(String p_310227_, Ingredient p_313029_, ItemStack p_312409_);
   }

   public static class Serializer<T extends SingleItemRecipe> implements RecipeSerializer<T> {
      final SingleItemRecipe.Factory<T> factory;
      private final Codec<T> codec;

      protected Serializer(SingleItemRecipe.Factory<T> p_311205_) {
         this.factory = p_311205_;
         this.codec = RecordCodecBuilder.create((p_309261_) -> {
            return p_309261_.group(ExtraCodecs.strictOptionalField(Codec.STRING, "group", "").forGetter((p_298324_) -> {
               return p_298324_.group;
            }), Ingredient.CODEC_NONEMPTY.fieldOf("ingredient").forGetter((p_299566_) -> {
               return p_299566_.ingredient;
            }), ItemStack.RESULT_CODEC.forGetter((p_301692_) -> {
               return p_301692_.result;
            })).apply(p_309261_, p_311205_::create);
         });
      }

      public Codec<T> codec() {
         return this.codec;
      }

      public T fromNetwork(FriendlyByteBuf p_44447_) {
         String s = p_44447_.readUtf();
         Ingredient ingredient = Ingredient.fromNetwork(p_44447_);
         ItemStack itemstack = p_44447_.readItem();
         return this.factory.create(s, ingredient, itemstack);
      }

      public void toNetwork(FriendlyByteBuf p_44440_, T p_44441_) {
         p_44440_.writeUtf(p_44441_.group);
         p_44441_.ingredient.toNetwork(p_44440_);
         p_44440_.writeItem(p_44441_.result);
      }
   }
}