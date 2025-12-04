package net.minecraft.network;

import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketFlow;

public interface PacketListener {
   PacketFlow flow();

   ConnectionProtocol protocol();

   void onDisconnect(Component p_130552_);

   boolean isAcceptingMessages();

   default boolean shouldHandleMessage(Packet<?> p_299735_) {
      return this.isAcceptingMessages();
   }

   default boolean shouldPropagateHandlingExceptions() {
      return true;
   }

   default void fillCrashReport(CrashReport p_311292_) {
      CrashReportCategory crashreportcategory = p_311292_.addCategory("Connection");
      crashreportcategory.setDetail("Protocol", () -> {
         return this.protocol().id();
      });
      crashreportcategory.setDetail("Flow", () -> {
         return this.flow().toString();
      });
      this.fillListenerSpecificCrashDetails(crashreportcategory);
   }

   default void fillListenerSpecificCrashDetails(CrashReportCategory p_310872_) {
   }
}