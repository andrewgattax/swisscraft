package net.minecraft.network.protocol.game;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;

public record ServerboundContainerSlotStateChangedPacket(int slotId, int containerId, boolean newState) implements Packet<ServerGamePacketListener> {
   public ServerboundContainerSlotStateChangedPacket(FriendlyByteBuf p_312822_) {
      this(p_312822_.readVarInt(), p_312822_.readVarInt(), p_312822_.readBoolean());
   }

   public void write(FriendlyByteBuf p_310021_) {
      p_310021_.writeVarInt(this.slotId);
      p_310021_.writeVarInt(this.containerId);
      p_310021_.writeBoolean(this.newState);
   }

   public void handle(ServerGamePacketListener p_309835_) {
      p_309835_.handleContainerSlotStateChanged(this);
   }
}