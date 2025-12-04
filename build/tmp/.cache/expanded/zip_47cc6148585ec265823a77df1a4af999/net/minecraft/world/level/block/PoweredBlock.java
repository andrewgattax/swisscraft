package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

public class PoweredBlock extends Block {
   public static final MapCodec<PoweredBlock> CODEC = simpleCodec(PoweredBlock::new);

   public MapCodec<PoweredBlock> codec() {
      return CODEC;
   }

   public PoweredBlock(BlockBehaviour.Properties p_55206_) {
      super(p_55206_);
   }

   public boolean isSignalSource(BlockState p_55213_) {
      return true;
   }

   public int getSignal(BlockState p_55208_, BlockGetter p_55209_, BlockPos p_55210_, Direction p_55211_) {
      return 15;
   }
}