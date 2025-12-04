package net.minecraft.network.protocol.common;

import java.util.UUID;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;

public record ServerboundResourcePackPacket(UUID id, ServerboundResourcePackPacket.Action action) implements Packet<ServerCommonPacketListener> {
   public ServerboundResourcePackPacket(FriendlyByteBuf p_299426_) {
      this(p_299426_.readUUID(), p_299426_.readEnum(ServerboundResourcePackPacket.Action.class));
   }

   public void write(FriendlyByteBuf p_298279_) {
      p_298279_.writeUUID(this.id);
      p_298279_.writeEnum(this.action);
   }

   public void handle(ServerCommonPacketListener p_298138_) {
      p_298138_.handleResourcePackResponse(this);
   }

   public static enum Action {
      SUCCESSFULLY_LOADED,
      DECLINED,
      FAILED_DOWNLOAD,
      ACCEPTED,
      DOWNLOADED,
      INVALID_URL,
      FAILED_RELOAD,
      DISCARDED;

      public boolean isTerminal() {
         return this != ACCEPTED && this != DOWNLOADED;
      }
   }
}