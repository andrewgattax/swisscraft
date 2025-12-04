package net.minecraft.network.protocol.common;

import java.util.Optional;
import java.util.UUID;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;

public record ClientboundResourcePackPopPacket(Optional<UUID> id) implements Packet<ClientCommonPacketListener> {
   public ClientboundResourcePackPopPacket(FriendlyByteBuf p_310483_) {
      this(p_310483_.readOptional(FriendlyByteBuf::readUUID));
   }

   public void write(FriendlyByteBuf p_311086_) {
      p_311086_.writeOptional(this.id, FriendlyByteBuf::writeUUID);
   }

   public void handle(ClientCommonPacketListener p_311428_) {
      p_311428_.handleResourcePackPop(this);
   }
}