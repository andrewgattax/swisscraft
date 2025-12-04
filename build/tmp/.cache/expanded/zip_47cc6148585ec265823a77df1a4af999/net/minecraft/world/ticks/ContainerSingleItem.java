package net.minecraft.world.ticks;

import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;

public interface ContainerSingleItem extends Container {
   ItemStack getTheItem();

   ItemStack splitTheItem(int p_312245_);

   void setTheItem(ItemStack p_310917_);

   BlockEntity getContainerBlockEntity();

   default ItemStack removeTheItem() {
      return this.splitTheItem(this.getMaxStackSize());
   }

   default int getContainerSize() {
      return 1;
   }

   default boolean isEmpty() {
      return this.getTheItem().isEmpty();
   }

   default void clearContent() {
      this.removeTheItem();
   }

   default ItemStack removeItemNoUpdate(int p_273409_) {
      return this.removeItem(p_273409_, this.getMaxStackSize());
   }

   default ItemStack getItem(int p_309780_) {
      return p_309780_ == 0 ? this.getTheItem() : ItemStack.EMPTY;
   }

   default ItemStack removeItem(int p_313221_, int p_309913_) {
      return p_313221_ != 0 ? ItemStack.EMPTY : this.splitTheItem(p_309913_);
   }

   default void setItem(int p_312121_, ItemStack p_312812_) {
      if (p_312121_ == 0) {
         this.setTheItem(p_312812_);
      }

   }

   default boolean stillValid(Player p_312825_) {
      return Container.stillValidBlockEntity(this.getContainerBlockEntity(), p_312825_);
   }
}