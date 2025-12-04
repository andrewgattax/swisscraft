package net.minecraft.network.protocol.common.custom;

import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.game.DebugEntityNameGenerator;
import net.minecraft.resources.ResourceLocation;

public record BreezeDebugPayload(BreezeDebugPayload.BreezeInfo breezeInfo) implements CustomPacketPayload {
   public static final ResourceLocation ID = new ResourceLocation("debug/breeze");

   public BreezeDebugPayload(FriendlyByteBuf p_309515_) {
      this(new BreezeDebugPayload.BreezeInfo(p_309515_));
   }

   public void write(FriendlyByteBuf p_309794_) {
      this.breezeInfo.write(p_309794_);
   }

   public ResourceLocation id() {
      return ID;
   }

   public static record BreezeInfo(UUID uuid, int id, Integer attackTarget, BlockPos jumpTarget) {
      public BreezeInfo(FriendlyByteBuf p_311987_) {
         this(p_311987_.readUUID(), p_311987_.readInt(), p_311987_.readNullable(FriendlyByteBuf::readInt), p_311987_.readNullable(FriendlyByteBuf::readBlockPos));
      }

      public void write(FriendlyByteBuf p_312731_) {
         p_312731_.writeUUID(this.uuid);
         p_312731_.writeInt(this.id);
         p_312731_.writeNullable(this.attackTarget, FriendlyByteBuf::writeInt);
         p_312731_.writeNullable(this.jumpTarget, FriendlyByteBuf::writeBlockPos);
      }

      public String generateName() {
         return DebugEntityNameGenerator.getEntityName(this.uuid);
      }

      public String toString() {
         return this.generateName();
      }
   }
}