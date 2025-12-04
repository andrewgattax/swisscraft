package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.FrontAndTop;
import net.minecraft.core.dispenser.DefaultDispenseItemBehavior;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Container;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeCache;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.CrafterBlockEntity;
import net.minecraft.world.level.block.entity.HopperBlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

public class CrafterBlock extends BaseEntityBlock {
   public static final MapCodec<CrafterBlock> CODEC = simpleCodec(CrafterBlock::new);
   public static final BooleanProperty CRAFTING = BlockStateProperties.CRAFTING;
   public static final BooleanProperty TRIGGERED = BlockStateProperties.TRIGGERED;
   private static final EnumProperty<FrontAndTop> ORIENTATION = BlockStateProperties.ORIENTATION;
   private static final int MAX_CRAFTING_TICKS = 6;
   private static final int CRAFTING_TICK_DELAY = 4;
   private static final RecipeCache RECIPE_CACHE = new RecipeCache(10);

   public CrafterBlock(BlockBehaviour.Properties p_310228_) {
      super(p_310228_);
      this.registerDefaultState(this.stateDefinition.any().setValue(ORIENTATION, FrontAndTop.NORTH_UP).setValue(TRIGGERED, Boolean.valueOf(false)).setValue(CRAFTING, Boolean.valueOf(false)));
   }

   protected MapCodec<CrafterBlock> codec() {
      return CODEC;
   }

   public boolean hasAnalogOutputSignal(BlockState p_309929_) {
      return true;
   }

   public int getAnalogOutputSignal(BlockState p_311332_, Level p_310277_, BlockPos p_312038_) {
      BlockEntity blockentity = p_310277_.getBlockEntity(p_312038_);
      if (blockentity instanceof CrafterBlockEntity crafterblockentity) {
         return crafterblockentity.getRedstoneSignal();
      } else {
         return 0;
      }
   }

   public void neighborChanged(BlockState p_309741_, Level p_312714_, BlockPos p_310958_, Block p_313237_, BlockPos p_312468_, boolean p_309615_) {
      boolean flag = p_312714_.hasNeighborSignal(p_310958_);
      boolean flag1 = p_309741_.getValue(TRIGGERED);
      BlockEntity blockentity = p_312714_.getBlockEntity(p_310958_);
      if (flag && !flag1) {
         p_312714_.scheduleTick(p_310958_, this, 4);
         p_312714_.setBlock(p_310958_, p_309741_.setValue(TRIGGERED, Boolean.valueOf(true)), 2);
         this.setBlockEntityTriggered(blockentity, true);
      } else if (!flag && flag1) {
         p_312714_.setBlock(p_310958_, p_309741_.setValue(TRIGGERED, Boolean.valueOf(false)).setValue(CRAFTING, Boolean.valueOf(false)), 2);
         this.setBlockEntityTriggered(blockentity, false);
      }

   }

   public void tick(BlockState p_310321_, ServerLevel p_312701_, BlockPos p_311281_, RandomSource p_311092_) {
      this.dispenseFrom(p_310321_, p_312701_, p_311281_);
   }

   @Nullable
   public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level p_310928_, BlockState p_311648_, BlockEntityType<T> p_310343_) {
      return p_310928_.isClientSide ? null : createTickerHelper(p_310343_, BlockEntityType.CRAFTER, CrafterBlockEntity::serverTick);
   }

   private void setBlockEntityTriggered(@Nullable BlockEntity p_312888_, boolean p_312611_) {
      if (p_312888_ instanceof CrafterBlockEntity crafterblockentity) {
         crafterblockentity.setTriggered(p_312611_);
      }

   }

   public BlockEntity newBlockEntity(BlockPos p_311818_, BlockState p_310225_) {
      CrafterBlockEntity crafterblockentity = new CrafterBlockEntity(p_311818_, p_310225_);
      crafterblockentity.setTriggered(p_310225_.hasProperty(TRIGGERED) && p_310225_.getValue(TRIGGERED));
      return crafterblockentity;
   }

   public BlockState getStateForPlacement(BlockPlaceContext p_311294_) {
      Direction direction = p_311294_.getNearestLookingDirection().getOpposite();
      Direction direction2;
      switch (direction) {
         case DOWN:
            direction2 = p_311294_.getHorizontalDirection().getOpposite();
            break;
         case UP:
            direction2 = p_311294_.getHorizontalDirection();
            break;
         case NORTH:
         case SOUTH:
         case WEST:
         case EAST:
            direction2 = Direction.UP;
            break;
         default:
            throw new IncompatibleClassChangeError();
      }

      Direction direction1 = direction2;
      return this.defaultBlockState().setValue(ORIENTATION, FrontAndTop.fromFrontAndTop(direction, direction1)).setValue(TRIGGERED, Boolean.valueOf(p_311294_.getLevel().hasNeighborSignal(p_311294_.getClickedPos())));
   }

   public void setPlacedBy(Level p_311617_, BlockPos p_313069_, BlockState p_310230_, LivingEntity p_310379_, ItemStack p_311227_) {
      if (p_311227_.hasCustomHoverName()) {
         BlockEntity blockentity = p_311617_.getBlockEntity(p_313069_);
         if (blockentity instanceof CrafterBlockEntity) {
            CrafterBlockEntity crafterblockentity = (CrafterBlockEntity)blockentity;
            crafterblockentity.setCustomName(p_311227_.getHoverName());
         }
      }

      if (p_310230_.getValue(TRIGGERED)) {
         p_311617_.scheduleTick(p_313069_, this, 4);
      }

   }

   public void onRemove(BlockState p_310019_, Level p_310489_, BlockPos p_312335_, BlockState p_311081_, boolean p_310350_) {
      Containers.dropContentsOnDestroy(p_310019_, p_311081_, p_310489_, p_312335_);
      super.onRemove(p_310019_, p_310489_, p_312335_, p_311081_, p_310350_);
   }

   public InteractionResult use(BlockState p_309704_, Level p_312700_, BlockPos p_310945_, Player p_312953_, InteractionHand p_311801_, BlockHitResult p_309965_) {
      if (p_312700_.isClientSide) {
         return InteractionResult.SUCCESS;
      } else {
         BlockEntity blockentity = p_312700_.getBlockEntity(p_310945_);
         if (blockentity instanceof CrafterBlockEntity) {
            p_312953_.openMenu((CrafterBlockEntity)blockentity);
         }

         return InteractionResult.CONSUME;
      }
   }

   protected void dispenseFrom(BlockState p_313036_, ServerLevel p_310451_, BlockPos p_310774_) {
      BlockEntity $$5 = p_310451_.getBlockEntity(p_310774_);
      if ($$5 instanceof CrafterBlockEntity crafterblockentity) {
         Optional<CraftingRecipe> optional = getPotentialResults(p_310451_, crafterblockentity);
         if (optional.isEmpty()) {
            p_310451_.levelEvent(1050, p_310774_, 0);
         } else {
            crafterblockentity.setCraftingTicksRemaining(6);
            p_310451_.setBlock(p_310774_, p_313036_.setValue(CRAFTING, Boolean.valueOf(true)), 2);
            CraftingRecipe craftingrecipe = optional.get();
            ItemStack itemstack = craftingrecipe.assemble(crafterblockentity, p_310451_.registryAccess());
            itemstack.onCraftedBySystem(p_310451_);
            this.dispenseItem(p_310451_, p_310774_, crafterblockentity, itemstack, p_313036_);
            craftingrecipe.getRemainingItems(crafterblockentity).forEach((p_312864_) -> {
               this.dispenseItem(p_310451_, p_310774_, crafterblockentity, p_312864_, p_313036_);
            });
            crafterblockentity.getItems().forEach((p_312802_) -> {
               if (!p_312802_.isEmpty()) {
                  p_312802_.shrink(1);
               }
            });
            crafterblockentity.setChanged();
         }
      }
   }

   public static Optional<CraftingRecipe> getPotentialResults(Level p_311236_, CraftingContainer p_311957_) {
      return RECIPE_CACHE.get(p_311236_, p_311957_);
   }

   private void dispenseItem(Level p_311411_, BlockPos p_312358_, CrafterBlockEntity p_309887_, ItemStack p_310474_, BlockState p_310667_) {
      Direction direction = p_310667_.getValue(ORIENTATION).front();
      Container container = HopperBlockEntity.getContainerAt(p_311411_, p_312358_.relative(direction));
      ItemStack itemstack = p_310474_.copy();
      if (container != null && (container instanceof CrafterBlockEntity || p_310474_.getCount() > container.getMaxStackSize())) {
         while(!itemstack.isEmpty()) {
            ItemStack itemstack2 = itemstack.copyWithCount(1);
            ItemStack itemstack1 = HopperBlockEntity.addItem(p_309887_, container, itemstack2, direction.getOpposite());
            if (!itemstack1.isEmpty()) {
               break;
            }

            itemstack.shrink(1);
         }
      } else if (container != null) {
         while(!itemstack.isEmpty()) {
            int i = itemstack.getCount();
            itemstack = HopperBlockEntity.addItem(p_309887_, container, itemstack, direction.getOpposite());
            if (i == itemstack.getCount()) {
               break;
            }
         }
      }

      if (!itemstack.isEmpty()) {
         Vec3 vec3 = Vec3.atCenterOf(p_312358_).relative(direction, 0.7D);
         DefaultDispenseItemBehavior.spawnItem(p_311411_, itemstack, 6, direction, vec3);
         p_311411_.levelEvent(1049, p_312358_, 0);
         p_311411_.levelEvent(2010, p_312358_, direction.get3DDataValue());
      }

   }

   public RenderShape getRenderShape(BlockState p_311546_) {
      return RenderShape.MODEL;
   }

   public BlockState rotate(BlockState p_312403_, Rotation p_309910_) {
      return p_312403_.setValue(ORIENTATION, p_309910_.rotation().rotate(p_312403_.getValue(ORIENTATION)));
   }

   public BlockState mirror(BlockState p_310178_, Mirror p_311418_) {
      return p_310178_.setValue(ORIENTATION, p_311418_.rotation().rotate(p_310178_.getValue(ORIENTATION)));
   }

   protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> p_310076_) {
      p_310076_.add(ORIENTATION, TRIGGERED, CRAFTING);
   }
}