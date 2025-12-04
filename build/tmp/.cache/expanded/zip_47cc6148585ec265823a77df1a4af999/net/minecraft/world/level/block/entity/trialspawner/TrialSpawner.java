package net.minecraft.world.level.block.entity.trialspawner;

import com.google.common.annotations.VisibleForTesting;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.dispenser.DefaultDispenseItemBehavior;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.SpawnData;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;

public final class TrialSpawner {
   public static final int DETECT_PLAYER_SPAWN_BUFFER = 40;
   private static final int MAX_MOB_TRACKING_DISTANCE = 47;
   private static final int MAX_MOB_TRACKING_DISTANCE_SQR = Mth.square(47);
   private static final float SPAWNING_AMBIENT_SOUND_CHANCE = 0.02F;
   private final TrialSpawnerConfig config;
   private final TrialSpawnerData data;
   private final TrialSpawner.StateAccessor stateAccessor;
   private PlayerDetector playerDetector;
   private boolean overridePeacefulAndMobSpawnRule;

   public Codec<TrialSpawner> codec() {
      return RecordCodecBuilder.create((p_310329_) -> {
         return p_310329_.group(TrialSpawnerConfig.MAP_CODEC.forGetter(TrialSpawner::getConfig), TrialSpawnerData.MAP_CODEC.forGetter(TrialSpawner::getData)).apply(p_310329_, (p_312762_, p_311305_) -> {
            return new TrialSpawner(p_312762_, p_311305_, this.stateAccessor, this.playerDetector);
         });
      });
   }

   public TrialSpawner(TrialSpawner.StateAccessor p_310539_, PlayerDetector p_312974_) {
      this(TrialSpawnerConfig.DEFAULT, new TrialSpawnerData(), p_310539_, p_312974_);
   }

   public TrialSpawner(TrialSpawnerConfig p_310434_, TrialSpawnerData p_311518_, TrialSpawner.StateAccessor p_310216_, PlayerDetector p_309626_) {
      this.config = p_310434_;
      this.data = p_311518_;
      this.data.setSpawnPotentialsFromConfig(p_310434_);
      this.stateAccessor = p_310216_;
      this.playerDetector = p_309626_;
   }

   public TrialSpawnerConfig getConfig() {
      return this.config;
   }

   public TrialSpawnerData getData() {
      return this.data;
   }

   public TrialSpawnerState getState() {
      return this.stateAccessor.getState();
   }

   public void setState(Level p_310153_, TrialSpawnerState p_312484_) {
      this.stateAccessor.setState(p_310153_, p_312484_);
   }

   public void markUpdated() {
      this.stateAccessor.markUpdated();
   }

   public PlayerDetector getPlayerDetector() {
      return this.playerDetector;
   }

   public boolean canSpawnInLevel(Level p_312209_) {
      if (this.overridePeacefulAndMobSpawnRule) {
         return true;
      } else {
         return p_312209_.getDifficulty() == Difficulty.PEACEFUL ? false : p_312209_.getGameRules().getBoolean(GameRules.RULE_DOMOBSPAWNING);
      }
   }

   public Optional<UUID> spawnMob(ServerLevel p_312690_, BlockPos p_313108_) {
      RandomSource randomsource = p_312690_.getRandom();
      SpawnData spawndata = this.data.getOrCreateNextSpawnData(this, p_312690_.getRandom());
      CompoundTag compoundtag = spawndata.entityToSpawn();
      ListTag listtag = compoundtag.getList("Pos", 6);
      Optional<EntityType<?>> optional = EntityType.by(compoundtag);
      if (optional.isEmpty()) {
         return Optional.empty();
      } else {
         int i = listtag.size();
         double d0 = i >= 1 ? listtag.getDouble(0) : (double)p_313108_.getX() + (randomsource.nextDouble() - randomsource.nextDouble()) * (double)this.config.spawnRange() + 0.5D;
         double d1 = i >= 2 ? listtag.getDouble(1) : (double)(p_313108_.getY() + randomsource.nextInt(3) - 1);
         double d2 = i >= 3 ? listtag.getDouble(2) : (double)p_313108_.getZ() + (randomsource.nextDouble() - randomsource.nextDouble()) * (double)this.config.spawnRange() + 0.5D;
         if (!p_312690_.noCollision(optional.get().getAABB(d0, d1, d2))) {
            return Optional.empty();
         } else {
            Vec3 vec3 = new Vec3(d0, d1, d2);
            if (!inLineOfSight(p_312690_, p_313108_.getCenter(), vec3)) {
               return Optional.empty();
            } else {
               BlockPos blockpos = BlockPos.containing(vec3);
               if (!SpawnPlacements.checkSpawnRules(optional.get(), p_312690_, MobSpawnType.TRIAL_SPAWNER, blockpos, p_312690_.getRandom())) {
                  return Optional.empty();
               } else {
                  Entity entity = EntityType.loadEntityRecursive(compoundtag, p_312690_, (p_312166_) -> {
                     p_312166_.moveTo(d0, d1, d2, randomsource.nextFloat() * 360.0F, 0.0F);
                     return p_312166_;
                  });
                  if (entity == null) {
                     return Optional.empty();
                  } else {
                     if (entity instanceof Mob) {
                        Mob mob = (Mob)entity;
                        if (!mob.checkSpawnObstruction(p_312690_)) {
                           return Optional.empty();
                        }

                        if (spawndata.getEntityToSpawn().size() == 1 && spawndata.getEntityToSpawn().contains("id", 8)) {
                           mob.finalizeSpawn(p_312690_, p_312690_.getCurrentDifficultyAt(mob.blockPosition()), MobSpawnType.TRIAL_SPAWNER, (SpawnGroupData)null, (CompoundTag)null);
                           mob.setPersistenceRequired();
                        }
                     }

                     if (!p_312690_.tryAddFreshEntityWithPassengers(entity)) {
                        return Optional.empty();
                     } else {
                        p_312690_.levelEvent(3011, p_313108_, 0);
                        p_312690_.levelEvent(3012, blockpos, 0);
                        p_312690_.gameEvent(entity, GameEvent.ENTITY_PLACE, blockpos);
                        return Optional.of(entity.getUUID());
                     }
                  }
               }
            }
         }
      }
   }

   public void ejectReward(ServerLevel p_310080_, BlockPos p_311547_, ResourceLocation p_311861_) {
      LootTable loottable = p_310080_.getServer().getLootData().getLootTable(p_311861_);
      LootParams lootparams = (new LootParams.Builder(p_310080_)).create(LootContextParamSets.EMPTY);
      ObjectArrayList<ItemStack> objectarraylist = loottable.getRandomItems(lootparams);
      if (!objectarraylist.isEmpty()) {
         for(ItemStack itemstack : objectarraylist) {
            DefaultDispenseItemBehavior.spawnItem(p_310080_, itemstack, 2, Direction.UP, Vec3.atBottomCenterOf(p_311547_).relative(Direction.UP, 1.2D));
         }

         p_310080_.levelEvent(3014, p_311547_, 0);
      }

   }

   public void tickClient(Level p_309627_, BlockPos p_311485_) {
      if (!this.canSpawnInLevel(p_309627_)) {
         this.data.oSpin = this.data.spin;
      } else {
         TrialSpawnerState trialspawnerstate = this.getState();
         trialspawnerstate.emitParticles(p_309627_, p_311485_);
         if (trialspawnerstate.hasSpinningMob()) {
            double d0 = (double)Math.max(0L, this.data.nextMobSpawnsAt - p_309627_.getGameTime());
            this.data.oSpin = this.data.spin;
            this.data.spin = (this.data.spin + trialspawnerstate.spinningMobSpeed() / (d0 + 200.0D)) % 360.0D;
         }

         if (trialspawnerstate.isCapableOfSpawning()) {
            RandomSource randomsource = p_309627_.getRandom();
            if (randomsource.nextFloat() <= 0.02F) {
               p_309627_.playLocalSound(p_311485_, SoundEvents.TRIAL_SPAWNER_AMBIENT, SoundSource.BLOCKS, randomsource.nextFloat() * 0.25F + 0.75F, randomsource.nextFloat() + 0.5F, false);
            }
         }

      }
   }

   public void tickServer(ServerLevel p_310996_, BlockPos p_312836_) {
      TrialSpawnerState trialspawnerstate = this.getState();
      if (!this.canSpawnInLevel(p_310996_)) {
         if (trialspawnerstate.isCapableOfSpawning()) {
            this.data.reset();
            this.setState(p_310996_, TrialSpawnerState.INACTIVE);
         }

      } else {
         if (this.data.currentMobs.removeIf((p_309715_) -> {
            return shouldMobBeUntracked(p_310996_, p_312836_, p_309715_);
         })) {
            this.data.nextMobSpawnsAt = p_310996_.getGameTime() + (long)this.config.ticksBetweenSpawn();
         }

         TrialSpawnerState trialspawnerstate1 = trialspawnerstate.tickAndGetNext(p_312836_, this, p_310996_);
         if (trialspawnerstate1 != trialspawnerstate) {
            this.setState(p_310996_, trialspawnerstate1);
         }

      }
   }

   private static boolean shouldMobBeUntracked(ServerLevel p_312275_, BlockPos p_310158_, UUID p_312011_) {
      Entity entity = p_312275_.getEntity(p_312011_);
      return entity == null || !entity.isAlive() || !entity.level().dimension().equals(p_312275_.dimension()) || entity.blockPosition().distSqr(p_310158_) > (double)MAX_MOB_TRACKING_DISTANCE_SQR;
   }

   private static boolean inLineOfSight(Level p_311873_, Vec3 p_311845_, Vec3 p_312229_) {
      BlockHitResult blockhitresult = p_311873_.clip(new ClipContext(p_312229_, p_311845_, ClipContext.Block.VISUAL, ClipContext.Fluid.NONE, CollisionContext.empty()));
      return blockhitresult.getBlockPos().equals(BlockPos.containing(p_311845_)) || blockhitresult.getType() == HitResult.Type.MISS;
   }

   public static void addSpawnParticles(Level p_312837_, BlockPos p_311261_, RandomSource p_312356_) {
      for(int i = 0; i < 20; ++i) {
         double d0 = (double)p_311261_.getX() + 0.5D + (p_312356_.nextDouble() - 0.5D) * 2.0D;
         double d1 = (double)p_311261_.getY() + 0.5D + (p_312356_.nextDouble() - 0.5D) * 2.0D;
         double d2 = (double)p_311261_.getZ() + 0.5D + (p_312356_.nextDouble() - 0.5D) * 2.0D;
         p_312837_.addParticle(ParticleTypes.SMOKE, d0, d1, d2, 0.0D, 0.0D, 0.0D);
         p_312837_.addParticle(ParticleTypes.FLAME, d0, d1, d2, 0.0D, 0.0D, 0.0D);
      }

   }

   public static void addDetectPlayerParticles(Level p_309415_, BlockPos p_309941_, RandomSource p_310263_, int p_310988_) {
      for(int i = 0; i < 30 + Math.min(p_310988_, 10) * 5; ++i) {
         double d0 = (double)(2.0F * p_310263_.nextFloat() - 1.0F) * 0.65D;
         double d1 = (double)(2.0F * p_310263_.nextFloat() - 1.0F) * 0.65D;
         double d2 = (double)p_309941_.getX() + 0.5D + d0;
         double d3 = (double)p_309941_.getY() + 0.1D + (double)p_310263_.nextFloat() * 0.8D;
         double d4 = (double)p_309941_.getZ() + 0.5D + d1;
         p_309415_.addParticle(ParticleTypes.TRIAL_SPAWNER_DETECTION, d2, d3, d4, 0.0D, 0.0D, 0.0D);
      }

   }

   public static void addEjectItemParticles(Level p_311170_, BlockPos p_309958_, RandomSource p_309409_) {
      for(int i = 0; i < 20; ++i) {
         double d0 = (double)p_309958_.getX() + 0.4D + p_309409_.nextDouble() * 0.2D;
         double d1 = (double)p_309958_.getY() + 0.4D + p_309409_.nextDouble() * 0.2D;
         double d2 = (double)p_309958_.getZ() + 0.4D + p_309409_.nextDouble() * 0.2D;
         double d3 = p_309409_.nextGaussian() * 0.02D;
         double d4 = p_309409_.nextGaussian() * 0.02D;
         double d5 = p_309409_.nextGaussian() * 0.02D;
         p_311170_.addParticle(ParticleTypes.SMALL_FLAME, d0, d1, d2, d3, d4, d5 * 0.25D);
         p_311170_.addParticle(ParticleTypes.SMOKE, d0, d1, d2, d3, d4, d5);
      }

   }

   /** @deprecated */
   @Deprecated(
      forRemoval = true
   )
   @VisibleForTesting
   public void setPlayerDetector(PlayerDetector p_311472_) {
      this.playerDetector = p_311472_;
   }

   /** @deprecated */
   @Deprecated(
      forRemoval = true
   )
   @VisibleForTesting
   public void overridePeacefulAndMobSpawnRule() {
      this.overridePeacefulAndMobSpawnRule = true;
   }

   public interface StateAccessor {
      void setState(Level p_309383_, TrialSpawnerState p_310563_);

      TrialSpawnerState getState();

      void markUpdated();
   }
}