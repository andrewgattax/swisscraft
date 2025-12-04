package net.minecraft.world.entity.monster.breeze;

import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.util.Unit;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.entity.ai.util.LandRandomPos;
import net.minecraft.world.phys.Vec3;

public class Slide extends Behavior<Breeze> {
   public Slide() {
      super(Map.of(MemoryModuleType.ATTACK_TARGET, MemoryStatus.VALUE_PRESENT, MemoryModuleType.WALK_TARGET, MemoryStatus.VALUE_ABSENT, MemoryModuleType.BREEZE_JUMP_COOLDOWN, MemoryStatus.VALUE_ABSENT, MemoryModuleType.BREEZE_SHOOT, MemoryStatus.VALUE_ABSENT));
   }

   protected boolean checkExtraStartConditions(ServerLevel p_312721_, Breeze p_311782_) {
      return p_311782_.onGround() && !p_311782_.isInWater() && p_311782_.getPose() == Pose.STANDING;
   }

   protected void start(ServerLevel p_312079_, Breeze p_310251_, long p_310596_) {
      LivingEntity livingentity = p_310251_.getBrain().getMemory(MemoryModuleType.ATTACK_TARGET).orElse((LivingEntity)null);
      if (livingentity != null) {
         boolean flag = p_310251_.withinOuterCircleRange(livingentity.position());
         boolean flag1 = p_310251_.withinMiddleCircleRange(livingentity.position());
         boolean flag2 = p_310251_.withinInnerCircleRange(livingentity.position());
         Vec3 vec3 = null;
         if (flag) {
            vec3 = randomPointInMiddleCircle(p_310251_, livingentity);
         } else if (flag2) {
            Vec3 vec31 = DefaultRandomPos.getPosAway(p_310251_, 5, 5, livingentity.position());
            if (vec31 != null && livingentity.distanceToSqr(vec31.x, vec31.y, vec31.z) > livingentity.distanceToSqr(p_310251_)) {
               vec3 = vec31;
            }
         } else if (flag1) {
            vec3 = LandRandomPos.getPos(p_310251_, 5, 3);
         }

         if (vec3 != null) {
            p_310251_.getBrain().setMemory(MemoryModuleType.WALK_TARGET, new WalkTarget(BlockPos.containing(vec3), 0.6F, 1));
         }

      }
   }

   protected void stop(ServerLevel p_309742_, Breeze p_310528_, long p_312496_) {
      p_310528_.getBrain().setMemoryWithExpiry(MemoryModuleType.BREEZE_JUMP_COOLDOWN, Unit.INSTANCE, 20L);
   }

   private static Vec3 randomPointInMiddleCircle(Breeze p_310635_, LivingEntity p_312574_) {
      Vec3 vec3 = p_312574_.position().subtract(p_310635_.position());
      double d0 = vec3.length() - Mth.lerp(p_310635_.getRandom().nextDouble(), 8.0D, 4.0D);
      Vec3 vec31 = vec3.normalize().multiply(d0, d0, d0);
      return p_310635_.position().add(vec31);
   }
}