package net.minecraft.advancements.critereon;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.advancements.Criterion;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.level.storage.loot.LootContext;

public class BredAnimalsTrigger extends SimpleCriterionTrigger<BredAnimalsTrigger.TriggerInstance> {
   public Codec<BredAnimalsTrigger.TriggerInstance> codec() {
      return BredAnimalsTrigger.TriggerInstance.CODEC;
   }

   public void trigger(ServerPlayer p_147279_, Animal p_147280_, Animal p_147281_, @Nullable AgeableMob p_147282_) {
      LootContext lootcontext = EntityPredicate.createContext(p_147279_, p_147280_);
      LootContext lootcontext1 = EntityPredicate.createContext(p_147279_, p_147281_);
      LootContext lootcontext2 = p_147282_ != null ? EntityPredicate.createContext(p_147279_, p_147282_) : null;
      this.trigger(p_147279_, (p_18653_) -> {
         return p_18653_.matches(lootcontext, lootcontext1, lootcontext2);
      });
   }

   public static record TriggerInstance(Optional<ContextAwarePredicate> player, Optional<ContextAwarePredicate> parent, Optional<ContextAwarePredicate> partner, Optional<ContextAwarePredicate> child) implements SimpleCriterionTrigger.SimpleInstance {
      public static final Codec<BredAnimalsTrigger.TriggerInstance> CODEC = RecordCodecBuilder.create((p_308113_) -> {
         return p_308113_.group(ExtraCodecs.strictOptionalField(EntityPredicate.ADVANCEMENT_CODEC, "player").forGetter(BredAnimalsTrigger.TriggerInstance::player), ExtraCodecs.strictOptionalField(EntityPredicate.ADVANCEMENT_CODEC, "parent").forGetter(BredAnimalsTrigger.TriggerInstance::parent), ExtraCodecs.strictOptionalField(EntityPredicate.ADVANCEMENT_CODEC, "partner").forGetter(BredAnimalsTrigger.TriggerInstance::partner), ExtraCodecs.strictOptionalField(EntityPredicate.ADVANCEMENT_CODEC, "child").forGetter(BredAnimalsTrigger.TriggerInstance::child)).apply(p_308113_, BredAnimalsTrigger.TriggerInstance::new);
      });

      public static Criterion<BredAnimalsTrigger.TriggerInstance> bredAnimals() {
         return CriteriaTriggers.BRED_ANIMALS.createCriterion(new BredAnimalsTrigger.TriggerInstance(Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty()));
      }

      public static Criterion<BredAnimalsTrigger.TriggerInstance> bredAnimals(EntityPredicate.Builder p_18668_) {
         return CriteriaTriggers.BRED_ANIMALS.createCriterion(new BredAnimalsTrigger.TriggerInstance(Optional.empty(), Optional.empty(), Optional.empty(), Optional.of(EntityPredicate.wrap(p_18668_))));
      }

      public static Criterion<BredAnimalsTrigger.TriggerInstance> bredAnimals(Optional<EntityPredicate> p_298213_, Optional<EntityPredicate> p_299258_, Optional<EntityPredicate> p_297439_) {
         return CriteriaTriggers.BRED_ANIMALS.createCriterion(new BredAnimalsTrigger.TriggerInstance(Optional.empty(), EntityPredicate.wrap(p_298213_), EntityPredicate.wrap(p_299258_), EntityPredicate.wrap(p_297439_)));
      }

      public boolean matches(LootContext p_18676_, LootContext p_18677_, @Nullable LootContext p_18678_) {
         if (!this.child.isPresent() || p_18678_ != null && this.child.get().matches(p_18678_)) {
            return matches(this.parent, p_18676_) && matches(this.partner, p_18677_) || matches(this.parent, p_18677_) && matches(this.partner, p_18676_);
         } else {
            return false;
         }
      }

      private static boolean matches(Optional<ContextAwarePredicate> p_300266_, LootContext p_300903_) {
         return p_300266_.isEmpty() || p_300266_.get().matches(p_300903_);
      }

      public void validate(CriterionValidator p_312724_) {
         SimpleCriterionTrigger.SimpleInstance.super.validate(p_312724_);
         p_312724_.validateEntity(this.parent, ".parent");
         p_312724_.validateEntity(this.partner, ".partner");
         p_312724_.validateEntity(this.child, ".child");
      }

      public Optional<ContextAwarePredicate> player() {
         return this.player;
      }
   }
}