package net.minecraft.world.scores;

import javax.annotation.Nullable;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.numbers.NumberFormat;
import net.minecraft.network.chat.numbers.NumberFormatTypes;

public class Score implements ReadOnlyScoreInfo {
   private static final String TAG_SCORE = "Score";
   private static final String TAG_LOCKED = "Locked";
   private static final String TAG_DISPLAY = "display";
   private static final String TAG_FORMAT = "format";
   private int value;
   private boolean locked = true;
   @Nullable
   private Component display;
   @Nullable
   private NumberFormat numberFormat;

   public int value() {
      return this.value;
   }

   public void value(int p_313056_) {
      this.value = p_313056_;
   }

   public boolean isLocked() {
      return this.locked;
   }

   public void setLocked(boolean p_83399_) {
      this.locked = p_83399_;
   }

   @Nullable
   public Component display() {
      return this.display;
   }

   public void display(@Nullable Component p_312952_) {
      this.display = p_312952_;
   }

   @Nullable
   public NumberFormat numberFormat() {
      return this.numberFormat;
   }

   public void numberFormat(@Nullable NumberFormat p_310093_) {
      this.numberFormat = p_310093_;
   }

   public CompoundTag write() {
      CompoundTag compoundtag = new CompoundTag();
      compoundtag.putInt("Score", this.value);
      compoundtag.putBoolean("Locked", this.locked);
      if (this.display != null) {
         compoundtag.putString("display", Component.Serializer.toJson(this.display));
      }

      if (this.numberFormat != null) {
         NumberFormatTypes.CODEC.encodeStart(NbtOps.INSTANCE, this.numberFormat).result().ifPresent((p_309357_) -> {
            compoundtag.put("format", p_309357_);
         });
      }

      return compoundtag;
   }

   public static Score read(CompoundTag p_313199_) {
      Score score = new Score();
      score.value = p_313199_.getInt("Score");
      score.locked = p_313199_.getBoolean("Locked");
      if (p_313199_.contains("display", 8)) {
         score.display = Component.Serializer.fromJson(p_313199_.getString("display"));
      }

      if (p_313199_.contains("format", 10)) {
         NumberFormatTypes.CODEC.parse(NbtOps.INSTANCE, p_313199_.get("format")).result().ifPresent((p_309359_) -> {
            score.numberFormat = p_309359_;
         });
      }

      return score;
   }
}