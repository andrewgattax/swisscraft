package net.minecraft.client.multiplayer;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.logging.LogUtils;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.function.BooleanSupplier;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.CrashReportCategory;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.gui.screens.DisconnectedScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.client.resources.server.DownloadedPackSource;
import net.minecraft.client.telemetry.WorldSessionTelemetryManager;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.Connection;
import net.minecraft.network.ServerboundPacketListener;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketUtils;
import net.minecraft.network.protocol.common.ClientCommonPacketListener;
import net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket;
import net.minecraft.network.protocol.common.ClientboundDisconnectPacket;
import net.minecraft.network.protocol.common.ClientboundKeepAlivePacket;
import net.minecraft.network.protocol.common.ClientboundPingPacket;
import net.minecraft.network.protocol.common.ClientboundResourcePackPopPacket;
import net.minecraft.network.protocol.common.ClientboundResourcePackPushPacket;
import net.minecraft.network.protocol.common.ClientboundUpdateTagsPacket;
import net.minecraft.network.protocol.common.ServerboundKeepAlivePacket;
import net.minecraft.network.protocol.common.ServerboundPongPacket;
import net.minecraft.network.protocol.common.ServerboundResourcePackPacket;
import net.minecraft.network.protocol.common.custom.BrandPayload;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.common.custom.DiscardedPayload;
import net.minecraft.realms.DisconnectedRealmsScreen;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.tags.TagNetworkSerialization;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.slf4j.Logger;

@OnlyIn(Dist.CLIENT)
public abstract class ClientCommonPacketListenerImpl implements ClientCommonPacketListener {
   private static final Component GENERIC_DISCONNECT_MESSAGE = Component.translatable("disconnect.lost");
   private static final Logger LOGGER = LogUtils.getLogger();
   protected final Minecraft minecraft;
   protected final Connection connection;
   @Nullable
   protected final ServerData serverData;
   @Nullable
   protected String serverBrand;
   protected final WorldSessionTelemetryManager telemetryManager;
   @Nullable
   protected final Screen postDisconnectScreen;
   private final List<ClientCommonPacketListenerImpl.DeferredPacket> deferredPackets = new ArrayList<>();

   protected ClientCommonPacketListenerImpl(Minecraft p_300051_, Connection p_300688_, CommonListenerCookie p_300429_) {
      this.minecraft = p_300051_;
      this.connection = p_300688_;
      this.serverData = p_300429_.serverData();
      this.serverBrand = p_300429_.serverBrand();
      this.telemetryManager = p_300429_.telemetryManager();
      this.postDisconnectScreen = p_300429_.postDisconnectScreen();
   }

   public void handleKeepAlive(ClientboundKeepAlivePacket p_301155_) {
      this.sendWhen(new ServerboundKeepAlivePacket(p_301155_.getId()), () -> {
         return !RenderSystem.isFrozenAtPollEvents();
      }, Duration.ofMinutes(1L));
   }

   public void handlePing(ClientboundPingPacket p_300922_) {
      PacketUtils.ensureRunningOnSameThread(p_300922_, this, this.minecraft);
      this.send(new ServerboundPongPacket(p_300922_.getId()));
   }

   public void handleCustomPayload(ClientboundCustomPayloadPacket p_298103_) {
      if (net.minecraftforge.common.ForgeHooks.onCustomPayload(p_298103_, this.connection)) return;
      CustomPacketPayload custompacketpayload = p_298103_.payload();
      if (!(custompacketpayload instanceof DiscardedPayload)) {
         PacketUtils.ensureRunningOnSameThread(p_298103_, this, this.minecraft);
         if (custompacketpayload instanceof BrandPayload) {
            BrandPayload brandpayload = (BrandPayload)custompacketpayload;
            this.serverBrand = brandpayload.brand();
            this.telemetryManager.onServerBrandReceived(brandpayload.brand());
         } else {
            this.handleCustomPayload(custompacketpayload);
         }

      }
   }

   protected abstract void handleCustomPayload(CustomPacketPayload p_297976_);

   protected abstract RegistryAccess.Frozen registryAccess();

   public void handleResourcePackPush(ClientboundResourcePackPushPacket p_310071_) {
      PacketUtils.ensureRunningOnSameThread(p_310071_, this, this.minecraft);
      UUID uuid = p_310071_.id();
      URL url = parseResourcePackUrl(p_310071_.url());
      if (url == null) {
         this.connection.send(new ServerboundResourcePackPacket(uuid, ServerboundResourcePackPacket.Action.INVALID_URL));
      } else {
         String s = p_310071_.hash();
         boolean flag = p_310071_.required();
         ServerData.ServerPackStatus serverdata$serverpackstatus = this.serverData != null ? this.serverData.getResourcePackStatus() : ServerData.ServerPackStatus.PROMPT;
         if (serverdata$serverpackstatus != ServerData.ServerPackStatus.PROMPT && (!flag || serverdata$serverpackstatus != ServerData.ServerPackStatus.DISABLED)) {
            this.minecraft.getDownloadedPackSource().pushPack(uuid, url, s);
         } else {
            this.minecraft.setScreen(this.addOrUpdatePackPrompt(uuid, url, s, flag, p_310071_.prompt()));
         }

      }
   }

   public void handleResourcePackPop(ClientboundResourcePackPopPacket p_311803_) {
      PacketUtils.ensureRunningOnSameThread(p_311803_, this, this.minecraft);
      p_311803_.id().ifPresentOrElse((p_308277_) -> {
         this.minecraft.getDownloadedPackSource().popPack(p_308277_);
      }, () -> {
         this.minecraft.getDownloadedPackSource().popAll();
      });
   }

   static Component preparePackPrompt(Component p_299226_, @Nullable Component p_298885_) {
      return (Component)(p_298885_ == null ? p_299226_ : Component.translatable("multiplayer.texturePrompt.serverPrompt", p_299226_, p_298885_));
   }

   @Nullable
   private static URL parseResourcePackUrl(String p_298850_) {
      try {
         URL url = new URL(p_298850_);
         String s = url.getProtocol();
         return !"http".equals(s) && !"https".equals(s) ? null : url;
      } catch (MalformedURLException malformedurlexception) {
         return null;
      }
   }

   public void handleUpdateTags(ClientboundUpdateTagsPacket p_299537_) {
      PacketUtils.ensureRunningOnSameThread(p_299537_, this, this.minecraft);
      p_299537_.getTags().forEach(this::updateTagsForRegistry);
   }

   private <T> void updateTagsForRegistry(ResourceKey<? extends Registry<? extends T>> p_301094_, TagNetworkSerialization.NetworkPayload p_297701_) {
      if (!p_297701_.isEmpty()) {
         Registry<T> registry = this.registryAccess().registry(p_301094_).orElseThrow(() -> {
            return new IllegalStateException("Unknown registry " + p_301094_);
         });
         Map<TagKey<T>, List<Holder<T>>> map = new HashMap<>();
         TagNetworkSerialization.deserializeTagsFromNetwork((ResourceKey<? extends Registry<T>>)p_301094_, registry, p_297701_, map::put);
         registry.bindTags(map);
      }
   }

   public void handleDisconnect(ClientboundDisconnectPacket p_298016_) {
      this.connection.disconnect(p_298016_.getReason());
   }

   protected void sendDeferredPackets() {
      Iterator<ClientCommonPacketListenerImpl.DeferredPacket> iterator = this.deferredPackets.iterator();

      while(iterator.hasNext()) {
         ClientCommonPacketListenerImpl.DeferredPacket clientcommonpacketlistenerimpl$deferredpacket = iterator.next();
         if (clientcommonpacketlistenerimpl$deferredpacket.sendCondition().getAsBoolean()) {
            this.send(clientcommonpacketlistenerimpl$deferredpacket.packet);
            iterator.remove();
         } else if (clientcommonpacketlistenerimpl$deferredpacket.expirationTime() <= Util.getMillis()) {
            iterator.remove();
         }
      }

   }

   public void send(Packet<?> p_300175_) {
      this.connection.send(p_300175_);
   }

   public void onDisconnect(Component p_298766_) {
      this.telemetryManager.onDisconnect();
      this.minecraft.disconnect(this.createDisconnectScreen(p_298766_));
      LOGGER.warn("Client disconnected with reason: {}", (Object)p_298766_.getString());
   }

   public void fillListenerSpecificCrashDetails(CrashReportCategory p_309761_) {
      p_309761_.setDetail("Server type", () -> {
         return this.serverData != null ? this.serverData.type().toString() : "<none>";
      });
      p_309761_.setDetail("Server brand", () -> {
         return this.serverBrand;
      });
   }

   protected Screen createDisconnectScreen(Component p_299787_) {
      Screen screen = Objects.requireNonNullElseGet(this.postDisconnectScreen, () -> {
         return new JoinMultiplayerScreen(new TitleScreen());
      });
      return (Screen)(this.serverData != null && this.serverData.isRealm() ? new DisconnectedRealmsScreen(screen, GENERIC_DISCONNECT_MESSAGE, p_299787_) : new DisconnectedScreen(screen, GENERIC_DISCONNECT_MESSAGE, p_299787_));
   }

   @Nullable
   public String serverBrand() {
      return this.serverBrand;
   }

   private void sendWhen(Packet<? extends ServerboundPacketListener> p_300852_, BooleanSupplier p_299754_, Duration p_299011_) {
      if (p_299754_.getAsBoolean()) {
         this.send(p_300852_);
      } else {
         this.deferredPackets.add(new ClientCommonPacketListenerImpl.DeferredPacket(p_300852_, p_299754_, Util.getMillis() + p_299011_.toMillis()));
      }

   }

   private Screen addOrUpdatePackPrompt(UUID p_313077_, URL p_312880_, String p_309420_, boolean p_312218_, @Nullable Component p_309535_) {
      Screen screen = this.minecraft.screen;
      if (screen instanceof ClientCommonPacketListenerImpl.PackConfirmScreen clientcommonpacketlistenerimpl$packconfirmscreen) {
         return clientcommonpacketlistenerimpl$packconfirmscreen.update(this.minecraft, p_313077_, p_312880_, p_309420_, p_312218_, p_309535_);
      } else {
         return new ClientCommonPacketListenerImpl.PackConfirmScreen(this.minecraft, screen, List.of(new ClientCommonPacketListenerImpl.PackConfirmScreen.PendingRequest(p_313077_, p_312880_, p_309420_)), p_312218_, p_309535_);
      }
   }

   @OnlyIn(Dist.CLIENT)
   static record DeferredPacket(Packet<? extends ServerboundPacketListener> packet, BooleanSupplier sendCondition, long expirationTime) {
   }

   @OnlyIn(Dist.CLIENT)
   class PackConfirmScreen extends ConfirmScreen {
      private final List<ClientCommonPacketListenerImpl.PackConfirmScreen.PendingRequest> requests;
      @Nullable
      private final Screen parentScreen;

      PackConfirmScreen(Minecraft p_309743_, @Nullable Screen p_312679_, List<ClientCommonPacketListenerImpl.PackConfirmScreen.PendingRequest> p_312458_, boolean p_313140_, @Nullable Component p_312901_) {
         super((p_309396_) -> {
            p_309743_.setScreen(p_312679_);
            DownloadedPackSource downloadedpacksource = p_309743_.getDownloadedPackSource();
            if (p_309396_) {
               if (ClientCommonPacketListenerImpl.this.serverData != null) {
                  ClientCommonPacketListenerImpl.this.serverData.setResourcePackStatus(ServerData.ServerPackStatus.ENABLED);
               }

               downloadedpacksource.allowServerPacks();
            } else {
               downloadedpacksource.rejectServerPacks();
               if (p_313140_) {
                  ClientCommonPacketListenerImpl.this.connection.disconnect(Component.translatable("multiplayer.requiredTexturePrompt.disconnect"));
               } else if (ClientCommonPacketListenerImpl.this.serverData != null) {
                  ClientCommonPacketListenerImpl.this.serverData.setResourcePackStatus(ServerData.ServerPackStatus.DISABLED);
               }
            }

            for(ClientCommonPacketListenerImpl.PackConfirmScreen.PendingRequest clientcommonpacketlistenerimpl$packconfirmscreen$pendingrequest : p_312458_) {
               downloadedpacksource.pushPack(clientcommonpacketlistenerimpl$packconfirmscreen$pendingrequest.id, clientcommonpacketlistenerimpl$packconfirmscreen$pendingrequest.url, clientcommonpacketlistenerimpl$packconfirmscreen$pendingrequest.hash);
            }

            if (ClientCommonPacketListenerImpl.this.serverData != null) {
               ServerList.saveSingleServer(ClientCommonPacketListenerImpl.this.serverData);
            }

         }, p_313140_ ? Component.translatable("multiplayer.requiredTexturePrompt.line1") : Component.translatable("multiplayer.texturePrompt.line1"), ClientCommonPacketListenerImpl.preparePackPrompt(p_313140_ ? Component.translatable("multiplayer.requiredTexturePrompt.line2").withStyle(ChatFormatting.YELLOW, ChatFormatting.BOLD) : Component.translatable("multiplayer.texturePrompt.line2"), p_312901_), p_313140_ ? CommonComponents.GUI_PROCEED : CommonComponents.GUI_YES, p_313140_ ? CommonComponents.GUI_DISCONNECT : CommonComponents.GUI_NO);
         this.requests = p_312458_;
         this.parentScreen = p_312679_;
      }

      public ClientCommonPacketListenerImpl.PackConfirmScreen update(Minecraft p_312486_, UUID p_311436_, URL p_309404_, String p_312909_, boolean p_312985_, @Nullable Component p_309496_) {
         List<ClientCommonPacketListenerImpl.PackConfirmScreen.PendingRequest> list = ImmutableList.<ClientCommonPacketListenerImpl.PackConfirmScreen.PendingRequest>builderWithExpectedSize(this.requests.size() + 1).addAll(this.requests).add(new ClientCommonPacketListenerImpl.PackConfirmScreen.PendingRequest(p_311436_, p_309404_, p_312909_)).build();
         return ClientCommonPacketListenerImpl.this.new PackConfirmScreen(p_312486_, this.parentScreen, list, p_312985_, p_309496_);
      }

      @OnlyIn(Dist.CLIENT)
      static record PendingRequest(UUID id, URL url, String hash) {
      }
   }
}
