package net.minecraft.network.protocol.game;

import javax.annotation.Nullable;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;

public record ClientboundResetScorePacket(String owner, @Nullable String objectiveName) implements Packet<ClientGamePacketListener> {
   public ClientboundResetScorePacket(FriendlyByteBuf p_312061_) {
      this(p_312061_.readUtf(), p_312061_.readNullable(FriendlyByteBuf::readUtf));
   }

   public void write(FriendlyByteBuf p_310951_) {
      p_310951_.writeUtf(this.owner);
      p_310951_.writeNullable(this.objectiveName, FriendlyByteBuf::writeUtf);
   }

   public void handle(ClientGamePacketListener p_310650_) {
      p_310650_.handleResetScore(this);
   }
}