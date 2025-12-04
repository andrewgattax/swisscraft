package net.minecraft.network.chat;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.MapDecoder;
import com.mojang.serialization.MapEncoder;
import com.mojang.serialization.MapLike;
import com.mojang.serialization.RecordBuilder;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;
import net.minecraft.network.chat.contents.KeybindContents;
import net.minecraft.network.chat.contents.NbtContents;
import net.minecraft.network.chat.contents.PlainTextContents;
import net.minecraft.network.chat.contents.ScoreContents;
import net.minecraft.network.chat.contents.SelectorContents;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.StringRepresentable;

public class ComponentSerialization {
   public static final Codec<Component> CODEC = ExtraCodecs.recursive("Component", ComponentSerialization::createCodec);
   public static final Codec<Component> FLAT_CODEC = ExtraCodecs.FLAT_JSON.flatXmap((p_309576_) -> {
      return CODEC.parse(JsonOps.INSTANCE, p_309576_);
   }, (p_310173_) -> {
      return CODEC.encodeStart(JsonOps.INSTANCE, p_310173_);
   });

   private static MutableComponent createFromList(List<Component> p_312708_) {
      MutableComponent mutablecomponent = p_312708_.get(0).copy();

      for(int i = 1; i < p_312708_.size(); ++i) {
         mutablecomponent.append(p_312708_.get(i));
      }

      return mutablecomponent;
   }

   public static <T extends StringRepresentable, E> MapCodec<E> createLegacyComponentMatcher(T[] p_312620_, Function<T, MapCodec<? extends E>> p_312447_, Function<E, T> p_309774_, String p_311665_) {
      MapCodec<E> mapcodec = new ComponentSerialization.FuzzyCodec<>(Stream.<T>of(p_312620_).map(p_312447_).toList(), (p_312251_) -> {
         return p_312447_.apply(p_309774_.apply(p_312251_));
      });
      Codec<T> codec = StringRepresentable.fromValues(() -> {
         return p_312620_;
      });
      MapCodec<E> mapcodec1 = codec.dispatchMap(p_311665_, p_309774_, (p_312465_) -> {
         return p_312447_.apply(p_312465_).codec();
      });
      MapCodec<E> mapcodec2 = new ComponentSerialization.StrictEither<>(p_311665_, mapcodec1, mapcodec);
      return ExtraCodecs.orCompressed(mapcodec2, mapcodec1);
   }

   private static Codec<Component> createCodec(Codec<Component> p_310353_) {
      ComponentContents.Type<?>[] type = new ComponentContents.Type[]{PlainTextContents.TYPE, TranslatableContents.TYPE, KeybindContents.TYPE, ScoreContents.TYPE, SelectorContents.TYPE, NbtContents.TYPE};
      MapCodec<ComponentContents> mapcodec = createLegacyComponentMatcher(type, ComponentContents.Type::codec, ComponentContents::type, "type");
      Codec<Component> codec = RecordCodecBuilder.create((p_311001_) -> {
         return p_311001_.group(mapcodec.forGetter(Component::getContents), ExtraCodecs.strictOptionalField(ExtraCodecs.nonEmptyList(p_310353_.listOf()), "extra", List.of()).forGetter(Component::getSiblings), Style.Serializer.MAP_CODEC.forGetter(Component::getStyle)).apply(p_311001_, MutableComponent::new);
      });
      return Codec.either(Codec.either(Codec.STRING, ExtraCodecs.nonEmptyList(p_310353_.listOf())), codec).xmap((p_312362_) -> {
         return p_312362_.map((p_310114_) -> {
            return p_310114_.map(Component::literal, ComponentSerialization::createFromList);
         }, (p_310523_) -> {
            return p_310523_;
         });
      }, (p_312558_) -> {
         String s = p_312558_.tryCollapseToString();
         return s != null ? Either.left(Either.left(s)) : Either.right(p_312558_);
      });
   }

   static class FuzzyCodec<T> extends MapCodec<T> {
      private final List<MapCodec<? extends T>> codecs;
      private final Function<T, MapEncoder<? extends T>> encoderGetter;

      public FuzzyCodec(List<MapCodec<? extends T>> p_313195_, Function<T, MapEncoder<? extends T>> p_313105_) {
         this.codecs = p_313195_;
         this.encoderGetter = p_313105_;
      }

      public <S> DataResult<T> decode(DynamicOps<S> p_311662_, MapLike<S> p_310979_) {
         for(MapDecoder<? extends T> mapdecoder : this.codecs) {
            DataResult<? extends T> dataresult = mapdecoder.decode(p_311662_, p_310979_);
            if (dataresult.result().isPresent()) {
               return (DataResult<T>)dataresult;
            }
         }

         return DataResult.error(() -> {
            return "No matching codec found";
         });
      }

      public <S> RecordBuilder<S> encode(T p_310202_, DynamicOps<S> p_312954_, RecordBuilder<S> p_312771_) {
         MapEncoder<T> mapencoder = (MapEncoder<T>)this.encoderGetter.apply(p_310202_);
         return mapencoder.encode(p_310202_, p_312954_, p_312771_);
      }

      public <S> Stream<S> keys(DynamicOps<S> p_311118_) {
         return this.codecs.stream().flatMap((p_310919_) -> {
            return p_310919_.keys(p_311118_);
         }).distinct();
      }

      public String toString() {
         return "FuzzyCodec[" + this.codecs + "]";
      }
   }

   static class StrictEither<T> extends MapCodec<T> {
      private final String typeFieldName;
      private final MapCodec<T> typed;
      private final MapCodec<T> fuzzy;

      public StrictEither(String p_310206_, MapCodec<T> p_312028_, MapCodec<T> p_312603_) {
         this.typeFieldName = p_310206_;
         this.typed = p_312028_;
         this.fuzzy = p_312603_;
      }

      public <O> DataResult<T> decode(DynamicOps<O> p_310941_, MapLike<O> p_311041_) {
         return p_311041_.get(this.typeFieldName) != null ? this.typed.decode(p_310941_, p_311041_) : this.fuzzy.decode(p_310941_, p_311041_);
      }

      public <O> RecordBuilder<O> encode(T p_310960_, DynamicOps<O> p_310726_, RecordBuilder<O> p_310170_) {
         return this.fuzzy.encode(p_310960_, p_310726_, p_310170_);
      }

      public <T1> Stream<T1> keys(DynamicOps<T1> p_310134_) {
         return Stream.concat(this.typed.keys(p_310134_), this.fuzzy.keys(p_310134_)).distinct();
      }
   }
}