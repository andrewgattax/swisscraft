package net.minecraft.client.gui.components.debugchart;

import java.util.Locale;
import java.util.function.Supplier;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.util.SampleLogger;
import net.minecraft.util.TimeUtil;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class TpsDebugChart extends AbstractDebugChart {
   private static final int RED = -65536;
   private static final int YELLOW = -256;
   private static final int GREEN = -16711936;
   private final Supplier<Float> msptSupplier;

   public TpsDebugChart(Font p_298557_, SampleLogger p_298113_, Supplier<Float> p_309657_) {
      super(p_298557_, p_298113_);
      this.msptSupplier = p_309657_;
   }

   protected void renderAdditionalLinesAndLabels(GuiGraphics p_297354_, int p_298051_, int p_298343_, int p_299488_) {
      float f = (float)TimeUtil.MILLISECONDS_PER_SECOND / this.msptSupplier.get();
      this.drawStringWithShade(p_297354_, String.format("%.1f TPS", f), p_298051_ + 1, p_299488_ - 60 + 1);
   }

   protected String toDisplayString(double p_301254_) {
      return String.format(Locale.ROOT, "%d ms", (int)Math.round(toMilliseconds(p_301254_)));
   }

   protected int getSampleHeight(double p_299260_) {
      return (int)Math.round(toMilliseconds(p_299260_) * 60.0D / (double)this.msptSupplier.get().floatValue());
   }

   protected int getSampleColor(long p_300761_) {
      float f = this.msptSupplier.get();
      return this.getSampleColor(toMilliseconds((double)p_300761_), 0.0D, -16711936, (double)f / 2.0D, -256, (double)f, -65536);
   }

   private static double toMilliseconds(double p_300655_) {
      return p_300655_ / 1000000.0D;
   }
}