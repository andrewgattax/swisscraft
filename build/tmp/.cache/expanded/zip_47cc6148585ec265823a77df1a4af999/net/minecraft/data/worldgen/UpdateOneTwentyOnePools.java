package net.minecraft.data.worldgen;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;

public class UpdateOneTwentyOnePools {
   public static final ResourceKey<StructureTemplatePool> EMPTY = createKey("empty");

   public static ResourceKey<StructureTemplatePool> createKey(String p_312895_) {
      return ResourceKey.create(Registries.TEMPLATE_POOL, new ResourceLocation(p_312895_));
   }

   public static void register(BootstapContext<StructureTemplatePool> p_309824_, String p_312325_, StructureTemplatePool p_309820_) {
      Pools.register(p_309824_, p_312325_, p_309820_);
   }

   public static void bootstrap(BootstapContext<StructureTemplatePool> p_310920_) {
      TrialChambersStructurePools.bootstrap(p_310920_);
   }
}