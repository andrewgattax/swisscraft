package net.minecraft.util.datafix;

import com.google.gson.JsonObject;
import com.mojang.datafixers.DataFixUtils;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import net.minecraft.util.GsonHelper;

public class ComponentDataFixUtils {
   private static final String EMPTY_CONTENTS = createTextComponentJson("");

   public static <T> Dynamic<T> createPlainTextComponent(DynamicOps<T> p_312596_, String p_312893_) {
      String s = createTextComponentJson(p_312893_);
      return new Dynamic<>(p_312596_, p_312596_.createString(s));
   }

   public static <T> Dynamic<T> createEmptyComponent(DynamicOps<T> p_310010_) {
      return new Dynamic<>(p_310010_, p_310010_.createString(EMPTY_CONTENTS));
   }

   private static String createTextComponentJson(String p_309616_) {
      JsonObject jsonobject = new JsonObject();
      jsonobject.addProperty("text", p_309616_);
      return GsonHelper.toStableString(jsonobject);
   }

   public static <T> Dynamic<T> createTranslatableComponent(DynamicOps<T> p_310384_, String p_313033_) {
      JsonObject jsonobject = new JsonObject();
      jsonobject.addProperty("translate", p_313033_);
      return new Dynamic<>(p_310384_, p_310384_.createString(GsonHelper.toStableString(jsonobject)));
   }

   public static <T> Dynamic<T> wrapLiteralStringAsComponent(Dynamic<T> p_309728_) {
      return DataFixUtils.orElse(p_309728_.asString().map((p_312090_) -> {
         return createPlainTextComponent(p_309728_.getOps(), p_312090_);
      }).result(), p_309728_);
   }
}