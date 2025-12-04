package net.minecraft.network.chat;

import com.mojang.logging.LogUtils;
import java.time.Instant;
import java.util.UUID;
import java.util.function.BooleanSupplier;
import javax.annotation.Nullable;
import net.minecraft.util.SignatureValidator;
import net.minecraft.util.Signer;
import net.minecraft.world.entity.player.ProfilePublicKey;
import org.slf4j.Logger;

public class SignedMessageChain {
   private static final Logger LOGGER = LogUtils.getLogger();
   @Nullable
   private SignedMessageLink nextLink;
   private Instant lastTimeStamp = Instant.EPOCH;

   public SignedMessageChain(UUID p_250050_, UUID p_249127_) {
      this.nextLink = SignedMessageLink.root(p_250050_, p_249127_);
   }

   public SignedMessageChain.Encoder encoder(Signer p_248636_) {
      return (p_248067_) -> {
         SignedMessageLink signedmessagelink = this.advanceLink();
         return signedmessagelink == null ? null : new MessageSignature(p_248636_.sign((p_248065_) -> {
            PlayerChatMessage.updateSignature(p_248065_, signedmessagelink, p_248067_);
         }));
      };
   }

   public SignedMessageChain.Decoder decoder(ProfilePublicKey p_249122_) {
      SignatureValidator signaturevalidator = p_249122_.createSignatureValidator();
      return (p_308570_, p_308571_) -> {
         SignedMessageLink signedmessagelink = this.advanceLink();
         if (signedmessagelink == null) {
            throw new SignedMessageChain.DecodeException(Component.translatable("chat.disabled.chain_broken"), false);
         } else if (p_249122_.data().hasExpired()) {
            throw new SignedMessageChain.DecodeException(Component.translatable("chat.disabled.expiredProfileKey"), false);
         } else if (p_308571_.timeStamp().isBefore(this.lastTimeStamp)) {
            throw new SignedMessageChain.DecodeException(Component.translatable("multiplayer.disconnect.out_of_order_chat"), true);
         } else {
            this.lastTimeStamp = p_308571_.timeStamp();
            PlayerChatMessage playerchatmessage = new PlayerChatMessage(signedmessagelink, p_308570_, p_308571_, (Component)null, FilterMask.PASS_THROUGH);
            if (!playerchatmessage.verify(signaturevalidator)) {
               throw new SignedMessageChain.DecodeException(Component.translatable("multiplayer.disconnect.unsigned_chat"), true);
            } else {
               if (playerchatmessage.hasExpiredServer(Instant.now())) {
                  LOGGER.warn("Received expired chat: '{}'. Is the client/server system time unsynchronized?", (Object)p_308571_.content());
               }

               return playerchatmessage;
            }
         }
      };
   }

   @Nullable
   private SignedMessageLink advanceLink() {
      SignedMessageLink signedmessagelink = this.nextLink;
      if (signedmessagelink != null) {
         this.nextLink = signedmessagelink.advance();
      }

      return signedmessagelink;
   }

   public static class DecodeException extends ThrowingComponent {
      private final boolean shouldDisconnect;

      public DecodeException(Component p_249149_, boolean p_250401_) {
         super(p_249149_);
         this.shouldDisconnect = p_250401_;
      }

      public boolean shouldDisconnect() {
         return this.shouldDisconnect;
      }
   }

   @FunctionalInterface
   public interface Decoder {
      static SignedMessageChain.Decoder unsigned(UUID p_251747_, BooleanSupplier p_312636_) {
         return (p_308574_, p_308575_) -> {
            if (p_312636_.getAsBoolean()) {
               throw new SignedMessageChain.DecodeException(Component.translatable("chat.disabled.missingProfileKey"), false);
            } else {
               return PlayerChatMessage.unsigned(p_251747_, p_308575_.content());
            }
         };
      }

      PlayerChatMessage unpack(@Nullable MessageSignature p_249082_, SignedMessageBody p_250981_) throws SignedMessageChain.DecodeException;
   }

   @FunctionalInterface
   public interface Encoder {
      SignedMessageChain.Encoder UNSIGNED = (p_250548_) -> {
         return null;
      };

      @Nullable
      MessageSignature pack(SignedMessageBody p_250628_);
   }
}