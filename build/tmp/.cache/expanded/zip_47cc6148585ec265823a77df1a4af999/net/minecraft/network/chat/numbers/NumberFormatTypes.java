package net.minecraft.network.chat.numbers;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;

public class NumberFormatTypes {
   public static final MapCodec<NumberFormat> MAP_CODEC = BuiltInRegistries.NUMBER_FORMAT_TYPE.byNameCodec().dispatchMap(NumberFormat::type, (p_310744_) -> {
      return p_310744_.mapCodec().codec();
   });
   public static final Codec<NumberFormat> CODEC = MAP_CODEC.codec();

   public static NumberFormatType<?> bootstrap(Registry<NumberFormatType<?>> p_310229_) {
      NumberFormatType<?> numberformattype = Registry.register(p_310229_, "blank", BlankFormat.TYPE);
      Registry.register(p_310229_, "styled", StyledFormat.TYPE);
      Registry.register(p_310229_, "fixed", FixedFormat.TYPE);
      return numberformattype;
   }

   public static <T extends NumberFormat> void writeToStream(FriendlyByteBuf p_313171_, T p_312224_) {
      NumberFormatType<T> numberformattype = (NumberFormatType<T>)p_312224_.type();
      p_313171_.writeId(BuiltInRegistries.NUMBER_FORMAT_TYPE, numberformattype);
      numberformattype.writeToStream(p_313171_, p_312224_);
   }

   public static NumberFormat readFromStream(FriendlyByteBuf p_312075_) {
      NumberFormatType<?> numberformattype = p_312075_.readById(BuiltInRegistries.NUMBER_FORMAT_TYPE);
      return numberformattype.readFromStream(p_312075_);
   }
}