package net.minecraft.core;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DynamicOps;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.JavaOps;

public class Cloner<T> {
   private final Codec<T> directCodec;

   Cloner(Codec<T> p_312738_) {
      this.directCodec = p_312738_;
   }

   public T clone(T p_311468_, HolderLookup.Provider p_309751_, HolderLookup.Provider p_312597_) {
      DynamicOps<Object> dynamicops = RegistryOps.create(JavaOps.INSTANCE, p_309751_);
      DynamicOps<Object> dynamicops1 = RegistryOps.create(JavaOps.INSTANCE, p_312597_);
      Object object = Util.getOrThrow(this.directCodec.encodeStart(dynamicops, p_311468_), (p_311642_) -> {
         return new IllegalStateException("Failed to encode: " + p_311642_);
      });
      return Util.getOrThrow(this.directCodec.parse(dynamicops1, object), (p_311707_) -> {
         return new IllegalStateException("Failed to decode: " + p_311707_);
      });
   }

   public static class Factory {
      private final Map<ResourceKey<? extends Registry<?>>, Cloner<?>> codecs = new HashMap<>();

      public <T> Cloner.Factory addCodec(ResourceKey<? extends Registry<? extends T>> p_310427_, Codec<T> p_312943_) {
         this.codecs.put(p_310427_, new Cloner<>(p_312943_));
         return this;
      }

      @Nullable
      public <T> Cloner<T> cloner(ResourceKey<? extends Registry<? extends T>> p_311946_) {
         return (Cloner<T>)this.codecs.get(p_311946_);
      }
   }
}