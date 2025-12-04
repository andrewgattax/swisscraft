package net.minecraft.world.level.block.entity;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.RandomizableContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

public abstract class RandomizableContainerBlockEntity extends BaseContainerBlockEntity implements RandomizableContainer {
   @Nullable
   protected ResourceLocation lootTable;
   protected long lootTableSeed;

   protected RandomizableContainerBlockEntity(BlockEntityType<?> p_155629_, BlockPos p_155630_, BlockState p_155631_) {
      super(p_155629_, p_155630_, p_155631_);
   }

   @Nullable
   public ResourceLocation getLootTable() {
      return this.lootTable;
   }

   public void setLootTable(@Nullable ResourceLocation p_59627_) {
      this.lootTable = p_59627_;
   }

   public long getLootTableSeed() {
      return this.lootTableSeed;
   }

   public void setLootTableSeed(long p_311658_) {
      this.lootTableSeed = p_311658_;
   }

   public boolean isEmpty() {
      this.unpackLootTable((Player)null);
      return this.getItems().stream().allMatch(ItemStack::isEmpty);
   }

   public ItemStack getItem(int p_59611_) {
      this.unpackLootTable((Player)null);
      return this.getItems().get(p_59611_);
   }

   public ItemStack removeItem(int p_59613_, int p_59614_) {
      this.unpackLootTable((Player)null);
      ItemStack itemstack = ContainerHelper.removeItem(this.getItems(), p_59613_, p_59614_);
      if (!itemstack.isEmpty()) {
         this.setChanged();
      }

      return itemstack;
   }

   public ItemStack removeItemNoUpdate(int p_59630_) {
      this.unpackLootTable((Player)null);
      return ContainerHelper.takeItem(this.getItems(), p_59630_);
   }

   public void setItem(int p_59616_, ItemStack p_59617_) {
      this.unpackLootTable((Player)null);
      this.getItems().set(p_59616_, p_59617_);
      if (p_59617_.getCount() > this.getMaxStackSize()) {
         p_59617_.setCount(this.getMaxStackSize());
      }

      this.setChanged();
   }

   public boolean stillValid(Player p_59619_) {
      return Container.stillValidBlockEntity(this, p_59619_);
   }

   public void clearContent() {
      this.getItems().clear();
   }

   protected abstract NonNullList<ItemStack> getItems();

   protected abstract void setItems(NonNullList<ItemStack> p_59625_);

   public boolean canOpen(Player p_59643_) {
      return super.canOpen(p_59643_) && (this.lootTable == null || !p_59643_.isSpectator());
   }

   @Nullable
   public AbstractContainerMenu createMenu(int p_59637_, Inventory p_59638_, Player p_59639_) {
      if (this.canOpen(p_59639_)) {
         this.unpackLootTable(p_59638_.player);
         return this.createMenu(p_59637_, p_59638_);
      } else {
         return null;
      }
   }
}