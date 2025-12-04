package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;

public class PressurePlateBlock extends BasePressurePlateBlock {
   public static final MapCodec<PressurePlateBlock> CODEC = RecordCodecBuilder.mapCodec((p_310452_) -> {
      return p_310452_.group(BlockSetType.CODEC.fieldOf("block_set_type").forGetter((p_313030_) -> {
         return p_313030_.type;
      }), propertiesCodec()).apply(p_310452_, PressurePlateBlock::new);
   });
   public static final BooleanProperty POWERED = BlockStateProperties.POWERED;

   public MapCodec<PressurePlateBlock> codec() {
      return CODEC;
   }

   public PressurePlateBlock(BlockSetType p_273284_, BlockBehaviour.Properties p_273571_) {
      super(p_273571_, p_273284_);
      this.registerDefaultState(this.stateDefinition.any().setValue(POWERED, Boolean.valueOf(false)));
   }

   protected int getSignalForState(BlockState p_55270_) {
      return p_55270_.getValue(POWERED) ? 15 : 0;
   }

   protected BlockState setSignalForState(BlockState p_55259_, int p_55260_) {
      return p_55259_.setValue(POWERED, Boolean.valueOf(p_55260_ > 0));
   }

   protected int getSignalStrength(Level p_55264_, BlockPos p_55265_) {
      Class<Entity> oclass1;
      switch (this.type.pressurePlateSensitivity()) {
         case EVERYTHING:
            oclass1 = Entity.class;
            break;
         case MOBS:
            oclass1 = (Class)LivingEntity.class;
            break;
         default:
            throw new IncompatibleClassChangeError();
      }

      Class<? extends Entity> oclass = oclass1;
      return getEntityCount(p_55264_, TOUCH_AABB.move(p_55265_), oclass) > 0 ? 15 : 0;
   }

   protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> p_55262_) {
      p_55262_.add(POWERED);
   }
}