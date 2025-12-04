package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Dynamic;
import java.util.Objects;
import net.minecraft.Util;

public class EntityHorseSplitFix extends EntityRenameFix {
   public EntityHorseSplitFix(Schema p_15447_, boolean p_15448_) {
      super("EntityHorseSplitFix", p_15447_, p_15448_);
   }

   protected Pair<String, Typed<?>> fix(String p_15451_, Typed<?> p_15452_) {
      Dynamic<?> dynamic = p_15452_.get(DSL.remainderFinder());
      if (Objects.equals("EntityHorse", p_15451_)) {
         int i = dynamic.get("Type").asInt(0);
         String s1;
         switch (i) {
            case 1:
               s1 = "Donkey";
               break;
            case 2:
               s1 = "Mule";
               break;
            case 3:
               s1 = "ZombieHorse";
               break;
            case 4:
               s1 = "SkeletonHorse";
               break;
            default:
               s1 = "Horse";
         }

         String s = s1;
         dynamic.remove("Type");
         Type<?> type = this.getOutputSchema().findChoiceType(References.ENTITY).types().get(s);
         return Pair.of(s, Util.writeAndReadTypedOrThrow(p_15452_, type, (p_308980_) -> {
            return p_308980_;
         }));
      } else {
         return Pair.of(p_15451_, p_15452_);
      }
   }
}