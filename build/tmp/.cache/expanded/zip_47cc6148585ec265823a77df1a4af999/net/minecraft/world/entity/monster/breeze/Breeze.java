package net.minecraft.world.entity.monster.breeze;

import com.mojang.serialization.Dynamic;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.protocol.game.DebugPackets;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AnimationState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import net.minecraft.world.phys.Vec3;

public class Breeze extends Monster {
   private static final int SLIDE_PARTICLES_AMOUNT = 20;
   private static final int IDLE_PARTICLES_AMOUNT = 1;
   private static final int JUMP_DUST_PARTICLES_AMOUNT = 20;
   private static final int JUMP_TRAIL_PARTICLES_AMOUNT = 3;
   private static final int JUMP_TRAIL_DURATION_TICKS = 5;
   private static final int JUMP_CIRCLE_DISTANCE_Y = 10;
   private static final float FALL_DISTANCE_SOUND_TRIGGER_THRESHOLD = 3.0F;
   public AnimationState idle = new AnimationState();
   public AnimationState slide = new AnimationState();
   public AnimationState longJump = new AnimationState();
   public AnimationState shoot = new AnimationState();
   public AnimationState inhale = new AnimationState();
   private int jumpTrailStartedTick = 0;

   public static AttributeSupplier.Builder createAttributes() {
      return Mob.createMobAttributes().add(Attributes.MOVEMENT_SPEED, (double)0.6F).add(Attributes.MAX_HEALTH, 30.0D).add(Attributes.FOLLOW_RANGE, 24.0D).add(Attributes.ATTACK_DAMAGE, 2.0D);
   }

   public Breeze(EntityType<? extends Monster> p_310338_, Level p_309512_) {
      super(p_310338_, p_309512_);
      this.setPathfindingMalus(BlockPathTypes.DANGER_TRAPDOOR, -1.0F);
      this.setPathfindingMalus(BlockPathTypes.DAMAGE_FIRE, -1.0F);
   }

   protected Brain<?> makeBrain(Dynamic<?> p_311857_) {
      return BreezeAi.makeBrain(this.brainProvider().makeBrain(p_311857_));
   }

   public Brain<Breeze> getBrain() {
      return (Brain<Breeze>)super.getBrain();
   }

   protected Brain.Provider<Breeze> brainProvider() {
      return Brain.provider(BreezeAi.MEMORY_TYPES, BreezeAi.SENSOR_TYPES);
   }

   public boolean canAttack(LivingEntity p_309632_) {
      return p_309632_.getType() != EntityType.BREEZE && super.canAttack(p_309632_);
   }

   public void onSyncedDataUpdated(EntityDataAccessor<?> p_309800_) {
      if (this.level().isClientSide() && DATA_POSE.equals(p_309800_)) {
         this.resetAnimations();
         Pose pose = this.getPose();
         switch (pose) {
            case SHOOTING:
               this.shoot.startIfStopped(this.tickCount);
               break;
            case INHALING:
               this.longJump.startIfStopped(this.tickCount);
               break;
            case SLIDING:
               this.slide.startIfStopped(this.tickCount);
         }
      }

      super.onSyncedDataUpdated(p_309800_);
   }

   private void resetAnimations() {
      this.shoot.stop();
      this.idle.stop();
      this.inhale.stop();
      this.longJump.stop();
      this.slide.stop();
   }

   public void tick() {
      switch (this.getPose()) {
         case SHOOTING:
         case INHALING:
         case STANDING:
            this.resetJumpTrail().emitGroundParticles(1 + this.getRandom().nextInt(1));
            break;
         case SLIDING:
            this.emitGroundParticles(20);
            break;
         case LONG_JUMPING:
            this.emitJumpTrailParticles();
      }

      super.tick();
   }

   public Breeze resetJumpTrail() {
      this.jumpTrailStartedTick = 0;
      return this;
   }

   public Breeze emitJumpDustParticles() {
      Vec3 vec3 = this.position().add(0.0D, (double)0.1F, 0.0D);

      for(int i = 0; i < 20; ++i) {
         this.level().addParticle(ParticleTypes.GUST_DUST, vec3.x, vec3.y, vec3.z, 0.0D, 0.0D, 0.0D);
      }

      return this;
   }

   public void emitJumpTrailParticles() {
      if (++this.jumpTrailStartedTick <= 5) {
         BlockState blockstate = this.level().getBlockState(this.blockPosition().below());
         Vec3 vec3 = this.getDeltaMovement();
         Vec3 vec31 = this.position().add(vec3).add(0.0D, (double)0.1F, 0.0D);

         for(int i = 0; i < 3; ++i) {
            this.level().addParticle(new BlockParticleOption(ParticleTypes.BLOCK, blockstate), vec31.x, vec31.y, vec31.z, 0.0D, 0.0D, 0.0D);
         }

      }
   }

   public void emitGroundParticles(int p_310885_) {
      Vec3 vec3 = this.getBoundingBox().getCenter();
      Vec3 vec31 = new Vec3(vec3.x, this.position().y, vec3.z);
      BlockState blockstate = this.level().getBlockState(this.blockPosition().below());
      if (blockstate.getRenderShape() != RenderShape.INVISIBLE) {
         for(int i = 0; i < p_310885_; ++i) {
            this.level().addParticle(new BlockParticleOption(ParticleTypes.BLOCK, blockstate), vec31.x, vec31.y, vec31.z, 0.0D, 0.0D, 0.0D);
         }

      }
   }

   public void playAmbientSound() {
      this.level().playLocalSound(this, this.getAmbientSound(), this.getSoundSource(), 1.0F, 1.0F);
   }

   public SoundSource getSoundSource() {
      return SoundSource.HOSTILE;
   }

   protected SoundEvent getDeathSound() {
      return SoundEvents.BREEZE_DEATH;
   }

   protected SoundEvent getHurtSound(DamageSource p_311322_) {
      return SoundEvents.BREEZE_HURT;
   }

   protected SoundEvent getAmbientSound() {
      return this.onGround() ? SoundEvents.BREEZE_IDLE_GROUND : SoundEvents.BREEZE_IDLE_AIR;
   }

   public boolean withinOuterCircleRange(Vec3 p_311928_) {
      Vec3 vec3 = this.blockPosition().getCenter();
      return p_311928_.closerThan(vec3, 20.0D, 10.0D) && !p_311928_.closerThan(vec3, 8.0D, 10.0D);
   }

   public boolean withinMiddleCircleRange(Vec3 p_311815_) {
      Vec3 vec3 = this.blockPosition().getCenter();
      return p_311815_.closerThan(vec3, 8.0D, 10.0D) && !p_311815_.closerThan(vec3, 4.0D, 10.0D);
   }

   public boolean withinInnerCircleRange(Vec3 p_311473_) {
      Vec3 vec3 = this.blockPosition().getCenter();
      return p_311473_.closerThan(vec3, 4.0D, 10.0D);
   }

   protected void customServerAiStep() {
      this.level().getProfiler().push("breezeBrain");
      this.getBrain().tick((ServerLevel)this.level(), this);
      this.level().getProfiler().popPush("breezeActivityUpdate");
      this.level().getProfiler().pop();
      super.customServerAiStep();
   }

   protected void sendDebugPackets() {
      super.sendDebugPackets();
      DebugPackets.sendEntityBrain(this);
      DebugPackets.sendBreezeInfo(this);
   }

   public boolean canAttackType(EntityType<?> p_310232_) {
      return p_310232_ == EntityType.PLAYER;
   }

   public int getMaxHeadYRot() {
      return 30;
   }

   public int getHeadRotSpeed() {
      return 25;
   }

   public double getSnoutYPosition() {
      return this.getEyeY() - 0.4D;
   }

   public boolean isInvulnerableTo(DamageSource p_309859_) {
      return p_309859_.is(DamageTypeTags.BREEZE_IMMUNE_TO) || p_309859_.getEntity() instanceof Breeze || super.isInvulnerableTo(p_309859_);
   }

   public double getFluidJumpThreshold() {
      return (double)this.getEyeHeight();
   }

   public boolean causeFallDamage(float p_310250_, float p_310041_, DamageSource p_311921_) {
      if (p_310250_ > 3.0F) {
         this.playSound(SoundEvents.BREEZE_LAND, 1.0F, 1.0F);
      }

      return super.causeFallDamage(p_310250_, p_310041_, p_311921_);
   }

   protected Entity.MovementEmission getMovementEmission() {
      return Entity.MovementEmission.EVENTS;
   }
}