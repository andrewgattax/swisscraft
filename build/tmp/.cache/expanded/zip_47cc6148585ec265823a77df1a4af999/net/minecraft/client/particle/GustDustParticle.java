package net.minecraft.client.particle;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Vector3f;
import org.joml.Vector3fc;

@OnlyIn(Dist.CLIENT)
public class GustDustParticle extends TextureSheetParticle {
   private final Vector3f fromColor = new Vector3f(0.5F, 0.5F, 0.5F);
   private final Vector3f toColor = new Vector3f(1.0F, 1.0F, 1.0F);

   GustDustParticle(ClientLevel p_311214_, double p_313053_, double p_310306_, double p_310298_, double p_310107_, double p_312929_, double p_311697_) {
      super(p_311214_, p_313053_, p_310306_, p_310298_);
      this.hasPhysics = false;
      this.xd = p_310107_ + (double)Mth.randomBetween(this.random, -0.4F, 0.4F);
      this.zd = p_311697_ + (double)Mth.randomBetween(this.random, -0.4F, 0.4F);
      double d0 = Math.random() * 2.0D;
      double d1 = Math.sqrt(this.xd * this.xd + this.yd * this.yd + this.zd * this.zd);
      this.xd = this.xd / d1 * d0 * (double)0.4F;
      this.zd = this.zd / d1 * d0 * (double)0.4F;
      this.quadSize *= 2.5F;
      this.xd *= (double)0.08F;
      this.zd *= (double)0.08F;
      this.lifetime = 18 + this.random.nextInt(4);
   }

   public void render(VertexConsumer p_312429_, Camera p_311875_, float p_313243_) {
      this.lerpColors(p_313243_);
      super.render(p_312429_, p_311875_, p_313243_);
   }

   private void lerpColors(float p_310280_) {
      float f = ((float)this.age + p_310280_) / (float)(this.lifetime + 1);
      Vector3f vector3f = (new Vector3f((Vector3fc)this.fromColor)).lerp(this.toColor, f);
      this.rCol = vector3f.x();
      this.gCol = vector3f.y();
      this.bCol = vector3f.z();
   }

   public ParticleRenderType getRenderType() {
      return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
   }

   public void tick() {
      if (this.age++ >= this.lifetime) {
         this.remove();
      } else {
         this.xo = this.x;
         this.zo = this.z;
         this.move(this.xd, 0.0D, this.zd);
         this.xd *= 0.99D;
         this.zd *= 0.99D;
      }
   }

   @OnlyIn(Dist.CLIENT)
   public static class GustDustParticleProvider implements ParticleProvider<SimpleParticleType> {
      private final SpriteSet sprite;

      public GustDustParticleProvider(SpriteSet p_312415_) {
         this.sprite = p_312415_;
      }

      public Particle createParticle(SimpleParticleType p_310506_, ClientLevel p_311922_, double p_310415_, double p_312261_, double p_309411_, double p_309754_, double p_313090_, double p_310064_) {
         GustDustParticle gustdustparticle = new GustDustParticle(p_311922_, p_310415_, p_312261_, p_309411_, p_309754_, p_313090_, p_310064_);
         gustdustparticle.pickSprite(this.sprite);
         return gustdustparticle;
      }
   }
}