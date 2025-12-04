package net.minecraft.commands;

import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.internal.Streams;
import com.google.gson.stream.JsonReader;
import com.mojang.brigadier.StringReader;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import java.lang.reflect.Field;
import net.minecraft.Util;

public class ParserUtils {
   private static final Field JSON_READER_POS = Util.make(() -> {
      try {
         Field field = JsonReader.class.getDeclaredField("pos");
         field.setAccessible(true);
         return field;
      } catch (NoSuchFieldException nosuchfieldexception) {
         throw new IllegalStateException("Couldn't get field 'pos' for JsonReader", nosuchfieldexception);
      }
   });
   private static final Field JSON_READER_LINESTART = Util.make(() -> {
      try {
         Field field = JsonReader.class.getDeclaredField("lineStart");
         field.setAccessible(true);
         return field;
      } catch (NoSuchFieldException nosuchfieldexception) {
         throw new IllegalStateException("Couldn't get field 'lineStart' for JsonReader", nosuchfieldexception);
      }
   });

   private static int getPos(JsonReader p_311647_) {
      try {
         return JSON_READER_POS.getInt(p_311647_) - JSON_READER_LINESTART.getInt(p_311647_) + 1;
      } catch (IllegalAccessException illegalaccessexception) {
         throw new IllegalStateException("Couldn't read position of JsonReader", illegalaccessexception);
      }
   }

   public static <T> T parseJson(StringReader p_311860_, Codec<T> p_311403_) {
      JsonReader jsonreader = new JsonReader(new java.io.StringReader(p_311860_.getRemaining()));
      jsonreader.setLenient(false);

      Object object;
      try {
         JsonElement jsonelement = Streams.parse(jsonreader);
         object = Util.<T, JsonParseException>getOrThrow(p_311403_.parse(JsonOps.INSTANCE, jsonelement), JsonParseException::new);
      } catch (StackOverflowError stackoverflowerror) {
         throw new JsonParseException(stackoverflowerror);
      } finally {
         p_311860_.setCursor(p_311860_.getCursor() + getPos(jsonreader));
      }

      return (T)object;
   }
}