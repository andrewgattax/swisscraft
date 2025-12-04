package net.minecraft.world.entity.monster.breeze;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Lists;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.Map;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.util.Unit;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.behavior.LongJumpUtil;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class LongJump extends Behavior<Breeze> {
   private static final int REQUIRED_AIR_BLOCKS_ABOVE = 4;
   private static final double MAX_LINE_OF_SIGHT_TEST_RANGE = 50.0D;
   private static final int JUMP_COOLDOWN_TICKS = 10;
   private static final int JUMP_COOLDOWN_WHEN_HURT_TICKS = 2;
   private static final int INHALING_DURATION_TICKS = Math.round(10.0F);
   private static final float MAX_JUMP_VELOCITY = 1.4F;
   private static final ObjectArrayList<Integer> ALLOWED_ANGLES = new ObjectArrayList<>(Lists.newArrayList(40, 55, 60, 75, 80));

   @VisibleForTesting
   public LongJump() {
      super(Map.of(MemoryModuleType.ATTACK_TARGET, MemoryStatus.VALUE_PRESENT, MemoryModuleType.BREEZE_JUMP_COOLDOWN, MemoryStatus.VALUE_ABSENT, MemoryModuleType.BREEZE_JUMP_INHALING, MemoryStatus.REGISTERED, MemoryModuleType.BREEZE_JUMP_TARGET, MemoryStatus.REGISTERED, MemoryModuleType.BREEZE_SHOOT, MemoryStatus.VALUE_ABSENT, MemoryModuleType.WALK_TARGET, MemoryStatus.VALUE_ABSENT), 200);
   }

   protected boolean checkExtraStartConditions(ServerLevel p_312411_, Breeze p_309539_) {
      if (!p_309539_.onGround() && !p_309539_.isInWater()) {
         return false;
      } else if (p_309539_.getBrain().checkMemory(MemoryModuleType.BREEZE_JUMP_TARGET, MemoryStatus.VALUE_PRESENT)) {
         return true;
      } else {
         LivingEntity livingentity = p_309539_.getBrain().getMemory(MemoryModuleType.ATTACK_TARGET).orElse((LivingEntity)null);
         if (livingentity == null) {
            return false;
         } else if (outOfAggroRange(p_309539_, livingentity)) {
            p_309539_.getBrain().eraseMemory(MemoryModuleType.ATTACK_TARGET);
            return false;
         } else if (tooCloseForJump(p_309539_, livingentity)) {
            return false;
         } else if (!canJumpFromCurrentPosition(p_312411_, p_309539_)) {
            return false;
         } else {
            BlockPos blockpos = snapToSurface(p_309539_, randomPointBehindTarget(livingentity, p_309539_.getRandom()));
            if (blockpos == null) {
               return false;
            } else if (!hasLineOfSight(p_309539_, blockpos.getCenter()) && !hasLineOfSight(p_309539_, blockpos.above(4).getCenter())) {
               return false;
            } else {
               p_309539_.getBrain().setMemory(MemoryModuleType.BREEZE_JUMP_TARGET, blockpos);
               return true;
            }
         }
      }
   }

   protected boolean canStillUse(ServerLevel p_310673_, Breeze p_311330_, long p_310051_) {
      return p_311330_.getPose() != Pose.STANDING && !p_311330_.getBrain().hasMemoryValue(MemoryModuleType.BREEZE_JUMP_COOLDOWN);
   }

   protected void start(ServerLevel p_310741_, Breeze p_312948_, long p_311377_) {
      if (p_312948_.getBrain().checkMemory(MemoryModuleType.BREEZE_JUMP_INHALING, MemoryStatus.VALUE_ABSENT)) {
         p_312948_.getBrain().setMemoryWithExpiry(MemoryModuleType.BREEZE_JUMP_INHALING, Unit.INSTANCE, (long)INHALING_DURATION_TICKS);
      }

      p_312948_.setPose(Pose.INHALING);
      p_312948_.getBrain().getMemory(MemoryModuleType.BREEZE_JUMP_TARGET).ifPresent((p_311106_) -> {
         p_312948_.lookAt(EntityAnchorArgument.Anchor.EYES, p_311106_.getCenter());
      });
   }

   protected void tick(ServerLevel p_312629_, Breeze p_310204_, long p_313176_) {
      if (finishedInhaling(p_310204_)) {
         Vec3 vec3 = p_310204_.getBrain().getMemory(MemoryModuleType.BREEZE_JUMP_TARGET).flatMap((p_311994_) -> {
            return calculateOptimalJumpVector(p_310204_, p_310204_.getRandom(), Vec3.atBottomCenterOf(p_311994_));
         }).orElse((Vec3)null);
         if (vec3 == null) {
            p_310204_.setPose(Pose.STANDING);
            return;
         }

         p_310204_.playSound(SoundEvents.BREEZE_JUMP, 1.0F, 1.0F);
         p_310204_.setPose(Pose.LONG_JUMPING);
         p_310204_.setYRot(p_310204_.yBodyRot);
         p_310204_.setDiscardFriction(true);
         p_310204_.setDeltaMovement(vec3);
      } else if (finishedJumping(p_310204_)) {
         p_310204_.playSound(SoundEvents.BREEZE_LAND, 1.0F, 1.0F);
         p_310204_.setPose(Pose.STANDING);
         p_310204_.setDiscardFriction(false);
         boolean flag = p_310204_.getBrain().hasMemoryValue(MemoryModuleType.HURT_BY);
         p_310204_.getBrain().setMemoryWithExpiry(MemoryModuleType.BREEZE_JUMP_COOLDOWN, Unit.INSTANCE, flag ? 2L : 10L);
         p_310204_.getBrain().setMemoryWithExpiry(MemoryModuleType.BREEZE_SHOOT, Unit.INSTANCE, 100L);
      }

   }

   protected void stop(ServerLevel p_309511_, Breeze p_311681_, long p_312980_) {
      if (p_311681_.getPose() == Pose.LONG_JUMPING || p_311681_.getPose() == Pose.INHALING) {
         p_311681_.setPose(Pose.STANDING);
      }

      p_311681_.getBrain().eraseMemory(MemoryModuleType.BREEZE_JUMP_TARGET);
      p_311681_.getBrain().eraseMemory(MemoryModuleType.BREEZE_JUMP_INHALING);
   }

   private static boolean finishedInhaling(Breeze p_310510_) {
      return p_310510_.getBrain().getMemory(MemoryModuleType.BREEZE_JUMP_INHALING).isEmpty() && p_310510_.getPose() == Pose.INHALING;
   }

   private static boolean finishedJumping(Breeze p_309558_) {
      return p_309558_.getPose() == Pose.LONG_JUMPING && p_309558_.onGround();
   }

   private static Vec3 randomPointBehindTarget(LivingEntity p_310934_, RandomSource p_310813_) {
      int i = 90;
      float f = p_310934_.yHeadRot + 180.0F + (float)p_310813_.nextGaussian() * 90.0F / 2.0F;
      float f1 = Mth.lerp(p_310813_.nextFloat(), 4.0F, 8.0F);
      Vec3 vec3 = Vec3.directionFromRotation(0.0F, f).scale((double)f1);
      return p_310934_.position().add(vec3);
   }

   @Nullable
   private static BlockPos snapToSurface(LivingEntity p_312785_, Vec3 p_311613_) {
      ClipContext clipcontext = new ClipContext(p_311613_, p_311613_.relative(Direction.DOWN, 10.0D), ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, p_312785_);
      HitResult hitresult = p_312785_.level().clip(clipcontext);
      if (hitresult.getType() == HitResult.Type.BLOCK) {
         return BlockPos.containing(hitresult.getLocation()).above();
      } else {
         ClipContext clipcontext1 = new ClipContext(p_311613_, p_311613_.relative(Direction.UP, 10.0D), ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, p_312785_);
         HitResult hitresult1 = p_312785_.level().clip(clipcontext1);
         return hitresult1.getType() == HitResult.Type.BLOCK ? BlockPos.containing(hitresult.getLocation()).above() : null;
      }
   }

   @VisibleForTesting
   public static boolean hasLineOfSight(Breeze p_311529_, Vec3 p_310012_) {
      Vec3 vec3 = new Vec3(p_311529_.getX(), p_311529_.getY(), p_311529_.getZ());
      if (p_310012_.distanceTo(vec3) > 50.0D) {
         return false;
      } else {
         return p_311529_.level().clip(new ClipContext(vec3, p_310012_, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, p_311529_)).getType() == HitResult.Type.MISS;
      }
   }

   private static boolean outOfAggroRange(Breeze p_310244_, LivingEntity p_309508_) {
      return !p_309508_.closerThan(p_310244_, 24.0D);
   }

   private static boolean tooCloseForJump(Breeze p_310091_, LivingEntity p_311303_) {
      return p_311303_.distanceTo(p_310091_) - 4.0F <= 0.0F;
   }

   private static boolean canJumpFromCurrentPosition(ServerLevel p_312023_, Breeze p_313218_) {
      BlockPos blockpos = p_313218_.blockPosition();

      for(int i = 1; i <= 4; ++i) {
         BlockPos blockpos1 = blockpos.relative(Direction.UP, i);
         if (!p_312023_.getBlockState(blockpos1).isAir() && !p_312023_.getFluidState(blockpos1).is(FluidTags.WATER)) {
            return false;
         }
      }

      return true;
   }

   private static Optional<Vec3> calculateOptimalJumpVector(Breeze p_310143_, RandomSource p_313023_, Vec3 p_309973_) {
      for(int i : Util.shuffledCopy(ALLOWED_ANGLES, p_313023_)) {
         Optional<Vec3> optional = LongJumpUtil.calculateJumpVectorForAngle(p_310143_, p_309973_, 1.4F, i, false);
         if (optional.isPresent()) {
            return optional;
         }
      }

      return Optional.empty();
   }
}