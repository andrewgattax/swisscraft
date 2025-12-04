package net.minecraft.world.level.block.entity;

import com.mojang.logging.LogUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.Spawner;
import net.minecraft.world.level.block.TrialSpawnerBlock;
import net.minecraft.world.level.block.entity.trialspawner.PlayerDetector;
import net.minecraft.world.level.block.entity.trialspawner.TrialSpawner;
import net.minecraft.world.level.block.entity.trialspawner.TrialSpawnerState;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import org.slf4j.Logger;

public class TrialSpawnerBlockEntity extends BlockEntity implements Spawner, TrialSpawner.StateAccessor {
   private static final Logger LOGGER = LogUtils.getLogger();
   private TrialSpawner trialSpawner;

   public TrialSpawnerBlockEntity(BlockPos p_309527_, BlockState p_312341_) {
      super(BlockEntityType.TRIAL_SPAWNER, p_309527_, p_312341_);
      PlayerDetector playerdetector = PlayerDetector.PLAYERS;
      this.trialSpawner = new TrialSpawner(this, playerdetector);
   }

   public void load(CompoundTag p_313100_) {
      super.load(p_313100_);
      this.trialSpawner.codec().parse(NbtOps.INSTANCE, p_313100_).resultOrPartial(LOGGER::error).ifPresent((p_311010_) -> {
         this.trialSpawner = p_311010_;
      });
      if (this.level != null) {
         this.markUpdated();
      }

   }

   protected void saveAdditional(CompoundTag p_310285_) {
      super.saveAdditional(p_310285_);
      this.trialSpawner.codec().encodeStart(NbtOps.INSTANCE, this.trialSpawner).get().ifLeft((p_312114_) -> {
         p_310285_.merge((CompoundTag)p_312114_);
      }).ifRight((p_311966_) -> {
         LOGGER.warn("Failed to encode TrialSpawner {}", (Object)p_311966_.message());
      });
   }

   public ClientboundBlockEntityDataPacket getUpdatePacket() {
      return ClientboundBlockEntityDataPacket.create(this);
   }

   public CompoundTag getUpdateTag() {
      return this.trialSpawner.getData().getUpdateTag(this.getBlockState().getValue(TrialSpawnerBlock.STATE));
   }

   public boolean onlyOpCanSetNbt() {
      return true;
   }

   public void setEntityId(EntityType<?> p_312357_, RandomSource p_313173_) {
      this.trialSpawner.getData().setEntityId(this.trialSpawner, p_313173_, p_312357_);
      this.setChanged();
   }

   public TrialSpawner getTrialSpawner() {
      return this.trialSpawner;
   }

   public TrialSpawnerState getState() {
      return !this.getBlockState().hasProperty(BlockStateProperties.TRIAL_SPAWNER_STATE) ? TrialSpawnerState.INACTIVE : this.getBlockState().getValue(BlockStateProperties.TRIAL_SPAWNER_STATE);
   }

   public void setState(Level p_313150_, TrialSpawnerState p_310751_) {
      this.setChanged();
      p_313150_.setBlockAndUpdate(this.worldPosition, this.getBlockState().setValue(BlockStateProperties.TRIAL_SPAWNER_STATE, p_310751_));
   }

   public void markUpdated() {
      this.setChanged();
      if (this.level != null) {
         this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), 3);
      }

   }
}