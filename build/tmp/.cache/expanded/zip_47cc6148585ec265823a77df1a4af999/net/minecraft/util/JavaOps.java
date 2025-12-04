package net.minecraft.util;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.MapLike;
import com.mojang.serialization.RecordBuilder;
import it.unimi.dsi.fastutil.bytes.ByteArrayList;
import it.unimi.dsi.fastutil.bytes.ByteList;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongList;
import java.nio.ByteBuffer;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import java.util.stream.Stream;
import javax.annotation.Nullable;

public class JavaOps implements DynamicOps<Object> {
   public static final JavaOps INSTANCE = new JavaOps();

   private JavaOps() {
   }

   public Object empty() {
      return null;
   }

   public Object emptyMap() {
      return Map.of();
   }

   public Object emptyList() {
      return List.of();
   }

   public <U> U convertTo(DynamicOps<U> p_312002_, Object p_309625_) {
      if (p_309625_ == null) {
         return p_312002_.empty();
      } else if (p_309625_ instanceof Map) {
         return this.convertMap(p_312002_, p_309625_);
      } else if (p_309625_ instanceof ByteList) {
         ByteList bytelist = (ByteList)p_309625_;
         return p_312002_.createByteList(ByteBuffer.wrap(bytelist.toByteArray()));
      } else if (p_309625_ instanceof IntList) {
         IntList intlist = (IntList)p_309625_;
         return p_312002_.createIntList(intlist.intStream());
      } else if (p_309625_ instanceof LongList) {
         LongList longlist = (LongList)p_309625_;
         return p_312002_.createLongList(longlist.longStream());
      } else if (p_309625_ instanceof List) {
         return this.convertList(p_312002_, p_309625_);
      } else if (p_309625_ instanceof String) {
         String s = (String)p_309625_;
         return p_312002_.createString(s);
      } else if (p_309625_ instanceof Boolean) {
         Boolean obool = (Boolean)p_309625_;
         return p_312002_.createBoolean(obool);
      } else if (p_309625_ instanceof Byte) {
         Byte obyte = (Byte)p_309625_;
         return p_312002_.createByte(obyte);
      } else if (p_309625_ instanceof Short) {
         Short oshort = (Short)p_309625_;
         return p_312002_.createShort(oshort);
      } else if (p_309625_ instanceof Integer) {
         Integer integer = (Integer)p_309625_;
         return p_312002_.createInt(integer);
      } else if (p_309625_ instanceof Long) {
         Long olong = (Long)p_309625_;
         return p_312002_.createLong(olong);
      } else if (p_309625_ instanceof Float) {
         Float f = (Float)p_309625_;
         return p_312002_.createFloat(f);
      } else if (p_309625_ instanceof Double) {
         Double d0 = (Double)p_309625_;
         return p_312002_.createDouble(d0);
      } else if (p_309625_ instanceof Number) {
         Number number = (Number)p_309625_;
         return p_312002_.createNumeric(number);
      } else {
         throw new IllegalStateException("Don't know how to convert " + p_309625_);
      }
   }

   public DataResult<Number> getNumberValue(Object p_310079_) {
      if (p_310079_ instanceof Number number) {
         return DataResult.success(number);
      } else {
         return DataResult.error(() -> {
            return "Not a number: " + p_310079_;
         });
      }
   }

   public Object createNumeric(Number p_310132_) {
      return p_310132_;
   }

   public Object createByte(byte p_310246_) {
      return p_310246_;
   }

   public Object createShort(short p_309486_) {
      return p_309486_;
   }

   public Object createInt(int p_311425_) {
      return p_311425_;
   }

   public Object createLong(long p_311842_) {
      return p_311842_;
   }

   public Object createFloat(float p_312509_) {
      return p_312509_;
   }

   public Object createDouble(double p_310687_) {
      return p_310687_;
   }

   public DataResult<Boolean> getBooleanValue(Object p_311560_) {
      if (p_311560_ instanceof Boolean obool) {
         return DataResult.success(obool);
      } else {
         return DataResult.error(() -> {
            return "Not a boolean: " + p_311560_;
         });
      }
   }

   public Object createBoolean(boolean p_311218_) {
      return p_311218_;
   }

   public DataResult<String> getStringValue(Object p_312499_) {
      if (p_312499_ instanceof String s) {
         return DataResult.success(s);
      } else {
         return DataResult.error(() -> {
            return "Not a string: " + p_312499_;
         });
      }
   }

   public Object createString(String p_311808_) {
      return p_311808_;
   }

   public DataResult<Object> mergeToList(Object p_311409_, Object p_310745_) {
      if (p_311409_ == this.empty()) {
         return DataResult.success(List.of(p_310745_));
      } else if (p_311409_ instanceof List) {
         List<?> list = (List)p_311409_;
         return list.isEmpty() ? DataResult.success(List.of(p_310745_)) : DataResult.success(ImmutableList.builder().addAll(list).add(p_310745_).build());
      } else {
         return DataResult.error(() -> {
            return "Not a list: " + p_311409_;
         });
      }
   }

   public DataResult<Object> mergeToList(Object p_311714_, List<Object> p_310475_) {
      if (p_311714_ == this.empty()) {
         return DataResult.success(p_310475_);
      } else if (p_311714_ instanceof List) {
         List<?> list = (List)p_311714_;
         return list.isEmpty() ? DataResult.success(p_310475_) : DataResult.success(ImmutableList.builder().addAll(list).addAll(p_310475_).build());
      } else {
         return DataResult.error(() -> {
            return "Not a list: " + p_311714_;
         });
      }
   }

   public DataResult<Object> mergeToMap(Object p_311964_, Object p_311197_, Object p_310964_) {
      if (p_311964_ == this.empty()) {
         return DataResult.success(Map.of(p_311197_, p_310964_));
      } else if (p_311964_ instanceof Map) {
         Map<?, ?> map = (Map)p_311964_;
         if (map.isEmpty()) {
            return DataResult.success(Map.of(p_311197_, p_310964_));
         } else {
            ImmutableMap.Builder<Object, Object> builder = ImmutableMap.builderWithExpectedSize(map.size() + 1);
            builder.putAll(map);
            builder.put(p_311197_, p_310964_);
            return DataResult.success(builder.buildKeepingLast());
         }
      } else {
         return DataResult.error(() -> {
            return "Not a map: " + p_311964_;
         });
      }
   }

   public DataResult<Object> mergeToMap(Object p_312957_, Map<Object, Object> p_311321_) {
      if (p_312957_ == this.empty()) {
         return DataResult.success(p_311321_);
      } else if (p_312957_ instanceof Map) {
         Map<?, ?> map = (Map)p_312957_;
         if (map.isEmpty()) {
            return DataResult.success(p_311321_);
         } else {
            ImmutableMap.Builder<Object, Object> builder = ImmutableMap.builderWithExpectedSize(map.size() + p_311321_.size());
            builder.putAll(map);
            builder.putAll(p_311321_);
            return DataResult.success(builder.buildKeepingLast());
         }
      } else {
         return DataResult.error(() -> {
            return "Not a map: " + p_312957_;
         });
      }
   }

   private static Map<Object, Object> mapLikeToMap(MapLike<Object> p_310456_) {
      return p_310456_.entries().collect(ImmutableMap.toImmutableMap(Pair::getFirst, Pair::getSecond));
   }

   public DataResult<Object> mergeToMap(Object p_313174_, MapLike<Object> p_310148_) {
      if (p_313174_ == this.empty()) {
         return DataResult.success(mapLikeToMap(p_310148_));
      } else if (p_313174_ instanceof Map) {
         Map<?, ?> map = (Map)p_313174_;
         if (map.isEmpty()) {
            return DataResult.success(mapLikeToMap(p_310148_));
         } else {
            ImmutableMap.Builder<Object, Object> builder = ImmutableMap.builderWithExpectedSize(map.size());
            builder.putAll(map);
            p_310148_.entries().forEach((p_311291_) -> {
               builder.put(p_311291_.getFirst(), p_311291_.getSecond());
            });
            return DataResult.success(builder.buildKeepingLast());
         }
      } else {
         return DataResult.error(() -> {
            return "Not a map: " + p_313174_;
         });
      }
   }

   static Stream<Pair<Object, Object>> getMapEntries(Map<?, ?> p_311276_) {
      return p_311276_.entrySet().stream().map((p_309487_) -> {
         return Pair.of(p_309487_.getKey(), p_309487_.getValue());
      });
   }

   public DataResult<Stream<Pair<Object, Object>>> getMapValues(Object p_312683_) {
      if (p_312683_ instanceof Map<?, ?> map) {
         return DataResult.success(getMapEntries(map));
      } else {
         return DataResult.error(() -> {
            return "Not a map: " + p_312683_;
         });
      }
   }

   public DataResult<Consumer<BiConsumer<Object, Object>>> getMapEntries(Object p_311678_) {
      if (p_311678_ instanceof Map<?, ?> map) {
         return DataResult.success(map::forEach);
      } else {
         return DataResult.error(() -> {
            return "Not a map: " + p_311678_;
         });
      }
   }

   public Object createMap(Stream<Pair<Object, Object>> p_311915_) {
      return p_311915_.collect(ImmutableMap.toImmutableMap(Pair::getFirst, Pair::getSecond));
   }

   public DataResult<MapLike<Object>> getMap(Object p_309528_) {
      if (p_309528_ instanceof final Map<?, ?> map) {
         return DataResult.success(new MapLike<Object>() {
            @Nullable
            public Object get(Object p_310138_) {
               return map.get(p_310138_);
            }

            @Nullable
            public Object get(String p_309918_) {
               return map.get(p_309918_);
            }

            public Stream<Pair<Object, Object>> entries() {
               return JavaOps.getMapEntries(map);
            }

            public String toString() {
               return "MapLike[" + map + "]";
            }
         });
      } else {
         return DataResult.error(() -> {
            return "Not a map: " + p_309528_;
         });
      }
   }

   public Object createMap(Map<Object, Object> p_310264_) {
      return p_310264_;
   }

   public DataResult<Stream<Object>> getStream(Object p_311244_) {
      if (p_311244_ instanceof List<?> list) {
         return DataResult.success(list.stream().map((p_312057_) -> {
            return p_312057_;
         }));
      } else {
         return DataResult.error(() -> {
            return "Not an list: " + p_311244_;
         });
      }
   }

   public DataResult<Consumer<Consumer<Object>>> getList(Object p_312936_) {
      if (p_312936_ instanceof List<?> list) {
         return DataResult.success(list::forEach);
      } else {
         return DataResult.error(() -> {
            return "Not an list: " + p_312936_;
         });
      }
   }

   public Object createList(Stream<Object> p_310926_) {
      return p_310926_.toList();
   }

   public DataResult<ByteBuffer> getByteBuffer(Object p_309940_) {
      if (p_309940_ instanceof ByteList bytelist) {
         return DataResult.success(ByteBuffer.wrap(bytelist.toByteArray()));
      } else {
         return DataResult.error(() -> {
            return "Not a byte list: " + p_309940_;
         });
      }
   }

   public Object createByteList(ByteBuffer p_311887_) {
      ByteBuffer bytebuffer = p_311887_.duplicate().clear();
      ByteArrayList bytearraylist = new ByteArrayList();
      bytearraylist.size(bytebuffer.capacity());
      bytebuffer.get(0, bytearraylist.elements(), 0, bytearraylist.size());
      return bytearraylist;
   }

   public DataResult<IntStream> getIntStream(Object p_312987_) {
      if (p_312987_ instanceof IntList intlist) {
         return DataResult.success(intlist.intStream());
      } else {
         return DataResult.error(() -> {
            return "Not an int list: " + p_312987_;
         });
      }
   }

   public Object createIntList(IntStream p_309802_) {
      return IntArrayList.toList(p_309802_);
   }

   public DataResult<LongStream> getLongStream(Object p_313231_) {
      if (p_313231_ instanceof LongList longlist) {
         return DataResult.success(longlist.longStream());
      } else {
         return DataResult.error(() -> {
            return "Not a long list: " + p_313231_;
         });
      }
   }

   public Object createLongList(LongStream p_311993_) {
      return LongArrayList.toList(p_311993_);
   }

   public Object remove(Object p_311673_, String p_310356_) {
      if (p_311673_ instanceof Map<?, ?> map) {
         Map<Object, Object> map1 = new LinkedHashMap<>(map);
         map1.remove(p_310356_);
         return DataResult.success(Map.copyOf(map1));
      } else {
         return DataResult.error(() -> {
            return "Not a map: " + p_311673_;
         });
      }
   }

   public RecordBuilder<Object> mapBuilder() {
      return new JavaOps.FixedMapBuilder<>(this);
   }

   public String toString() {
      return "Java";
   }

   static final class FixedMapBuilder<T> extends RecordBuilder.AbstractUniversalBuilder<T, ImmutableMap.Builder<T, T>> {
      public FixedMapBuilder(DynamicOps<T> p_309541_) {
         super(p_309541_);
      }

      protected ImmutableMap.Builder<T, T> initBuilder() {
         return ImmutableMap.builder();
      }

      protected ImmutableMap.Builder<T, T> append(T p_310252_, T p_310560_, ImmutableMap.Builder<T, T> p_309681_) {
         return p_309681_.put(p_310252_, p_310560_);
      }

      protected DataResult<T> build(ImmutableMap.Builder<T, T> p_310181_, T p_312273_) {
         ImmutableMap<T, T> immutablemap;
         try {
            immutablemap = p_310181_.buildOrThrow();
         } catch (IllegalArgumentException illegalargumentexception) {
            return DataResult.error(() -> {
               return "Can't build map: " + illegalargumentexception.getMessage();
            });
         }

         return this.ops().mergeToMap(p_312273_, immutablemap);
      }
   }
}