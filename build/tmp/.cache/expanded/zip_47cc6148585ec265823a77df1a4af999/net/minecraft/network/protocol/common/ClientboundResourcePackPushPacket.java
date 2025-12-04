package net.minecraft.network.protocol.common;

import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;

public record ClientboundResourcePackPushPacket(UUID id, String url, String hash, boolean required, @Nullable Component prompt) implements Packet<ClientCommonPacketListener> {
   public static final int MAX_HASH_LENGTH = 40;

   public ClientboundResourcePackPushPacket {
      if (hash.length() > 40) {
         throw new IllegalArgumentException("Hash is too long (max 40, was " + hash.length() + ")");
      }
   }

   public ClientboundResourcePackPushPacket(FriendlyByteBuf p_310933_) {
      this(p_310933_.readUUID(), p_310933_.readUtf(), p_310933_.readUtf(40), p_310933_.readBoolean(), p_310933_.readNullable(FriendlyByteBuf::readComponentTrusted));
   }

   public void write(FriendlyByteBuf p_310067_) {
      p_310067_.writeUUID(this.id);
      p_310067_.writeUtf(this.url);
      p_310067_.writeUtf(this.hash);
      p_310067_.writeBoolean(this.required);
      p_310067_.writeNullable(this.prompt, FriendlyByteBuf::writeComponent);
   }

   public void handle(ClientCommonPacketListener p_312649_) {
      p_312649_.handleResourcePackPush(this);
   }
}