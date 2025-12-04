package net.minecraft.advancements.critereon;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.advancements.Criterion;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.ExtraCodecs;

public class LootTableTrigger extends SimpleCriterionTrigger<LootTableTrigger.TriggerInstance> {
   public Codec<LootTableTrigger.TriggerInstance> codec() {
      return LootTableTrigger.TriggerInstance.CODEC;
   }

   public void trigger(ServerPlayer p_54598_, ResourceLocation p_54599_) {
      this.trigger(p_54598_, (p_54606_) -> {
         return p_54606_.matches(p_54599_);
      });
   }

   public static record TriggerInstance(Optional<ContextAwarePredicate> player, ResourceLocation lootTable) implements SimpleCriterionTrigger.SimpleInstance {
      public static final Codec<LootTableTrigger.TriggerInstance> CODEC = RecordCodecBuilder.create((p_311978_) -> {
         return p_311978_.group(ExtraCodecs.strictOptionalField(EntityPredicate.ADVANCEMENT_CODEC, "player").forGetter(LootTableTrigger.TriggerInstance::player), ResourceLocation.CODEC.fieldOf("loot_table").forGetter(LootTableTrigger.TriggerInstance::lootTable)).apply(p_311978_, LootTableTrigger.TriggerInstance::new);
      });

      public static Criterion<LootTableTrigger.TriggerInstance> lootTableUsed(ResourceLocation p_54619_) {
         return CriteriaTriggers.GENERATE_LOOT.createCriterion(new LootTableTrigger.TriggerInstance(Optional.empty(), p_54619_));
      }

      public boolean matches(ResourceLocation p_54621_) {
         return this.lootTable.equals(p_54621_);
      }

      public Optional<ContextAwarePredicate> player() {
         return this.player;
      }
   }
}