package net.minecraft.client.renderer.entity.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.BreezeModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.monster.breeze.Breeze;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class BreezeEyesLayer extends RenderLayer<Breeze, BreezeModel<Breeze>> {
   private final ResourceLocation textureLoc;
   private final BreezeModel<Breeze> model;

   public BreezeEyesLayer(RenderLayerParent<Breeze, BreezeModel<Breeze>> p_310165_, EntityModelSet p_312112_, ResourceLocation p_309706_) {
      super(p_310165_);
      this.model = new BreezeModel<>(p_312112_.bakeLayer(ModelLayers.BREEZE_EYES));
      this.textureLoc = p_309706_;
   }

   public void render(PoseStack p_312911_, MultiBufferSource p_312666_, int p_311532_, Breeze p_311391_, float p_311193_, float p_309423_, float p_310215_, float p_311406_, float p_311840_, float p_312197_) {
      this.model.prepareMobModel(p_311391_, p_311193_, p_309423_, p_310215_);
      this.getParentModel().copyPropertiesTo(this.model);
      VertexConsumer vertexconsumer = p_312666_.getBuffer(RenderType.breezeEyes(this.textureLoc));
      this.model.setupAnim(p_311391_, p_311193_, p_309423_, p_311406_, p_311840_, p_312197_);
      this.model.root().render(p_312911_, vertexconsumer, p_311532_, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);
   }

   protected ResourceLocation getTextureLocation(Breeze p_312494_) {
      return this.textureLoc;
   }
}