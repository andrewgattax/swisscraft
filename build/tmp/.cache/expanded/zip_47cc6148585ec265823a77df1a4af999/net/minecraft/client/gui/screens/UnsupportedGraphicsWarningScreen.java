package net.minecraft.client.gui.screens;

import com.google.common.collect.ImmutableList;
import java.util.List;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.MultiLineLabel;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.chat.FormattedText;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class UnsupportedGraphicsWarningScreen extends Screen {
   private static final int BUTTON_PADDING = 20;
   private static final int BUTTON_MARGIN = 5;
   private static final int BUTTON_HEIGHT = 20;
   private final Component narrationMessage;
   private final FormattedText message;
   private final ImmutableList<UnsupportedGraphicsWarningScreen.ButtonOption> buttonOptions;
   private MultiLineLabel messageLines = MultiLineLabel.EMPTY;
   private int contentTop;
   private int buttonWidth;

   protected UnsupportedGraphicsWarningScreen(Component p_310653_, List<Component> p_312556_, ImmutableList<UnsupportedGraphicsWarningScreen.ButtonOption> p_312852_) {
      super(p_310653_);
      this.message = FormattedText.composite(p_312556_);
      this.narrationMessage = CommonComponents.joinForNarration(p_310653_, ComponentUtils.formatList(p_312556_, CommonComponents.EMPTY));
      this.buttonOptions = p_312852_;
   }

   public Component getNarrationMessage() {
      return this.narrationMessage;
   }

   public void init() {
      for(UnsupportedGraphicsWarningScreen.ButtonOption unsupportedgraphicswarningscreen$buttonoption : this.buttonOptions) {
         this.buttonWidth = Math.max(this.buttonWidth, 20 + this.font.width(unsupportedgraphicswarningscreen$buttonoption.message) + 20);
      }

      int l = 5 + this.buttonWidth + 5;
      int i1 = l * this.buttonOptions.size();
      this.messageLines = MultiLineLabel.create(this.font, this.message, i1);
      int i = this.messageLines.getLineCount() * 9;
      this.contentTop = (int)((double)this.height / 2.0D - (double)i / 2.0D);
      int j = this.contentTop + i + 9 * 2;
      int k = (int)((double)this.width / 2.0D - (double)i1 / 2.0D);

      for(UnsupportedGraphicsWarningScreen.ButtonOption unsupportedgraphicswarningscreen$buttonoption1 : this.buttonOptions) {
         this.addRenderableWidget(Button.builder(unsupportedgraphicswarningscreen$buttonoption1.message, unsupportedgraphicswarningscreen$buttonoption1.onPress).bounds(k, j, this.buttonWidth, 20).build());
         k += l;
      }

   }

   public void render(GuiGraphics p_310210_, int p_309572_, int p_312206_, float p_311484_) {
      super.render(p_310210_, p_309572_, p_312206_, p_311484_);
      p_310210_.drawCenteredString(this.font, this.title, this.width / 2, this.contentTop - 9 * 2, -1);
      this.messageLines.renderCentered(p_310210_, this.width / 2, this.contentTop);
   }

   public void renderBackground(GuiGraphics p_313020_, int p_312760_, int p_310409_, float p_312847_) {
      this.renderDirtBackground(p_313020_);
   }

   public boolean shouldCloseOnEsc() {
      return false;
   }

   @OnlyIn(Dist.CLIENT)
   public static final class ButtonOption {
      final Component message;
      final Button.OnPress onPress;

      public ButtonOption(Component p_311722_, Button.OnPress p_312192_) {
         this.message = p_311722_;
         this.onPress = p_312192_;
      }
   }
}