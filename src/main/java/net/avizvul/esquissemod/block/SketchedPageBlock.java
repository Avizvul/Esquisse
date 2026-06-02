package net.avizvul.esquissemod.block;

import net.avizvul.esquissemod.block.entity.SketchedPageBlockEntity;
import net.avizvul.esquissemod.component.ModDataComponents;
import net.avizvul.esquissemod.item.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import org.jetbrains.annotations.Nullable;

public class SketchedPageBlock extends Block implements EntityBlock {
    public static final net.minecraft.world.level.block.state.properties.DirectionProperty FACING =
            net.minecraft.world.level.block.state.properties.BlockStateProperties.FACING;

    public SketchedPageBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new SketchedPageBlockEntity(pos, state);
    }

    @Override
    public boolean canSurvive(BlockState state, net.minecraft.world.level.LevelReader level, BlockPos pos) {
        // Узнаем, к какой стороне мы прикреплены
        net.minecraft.core.Direction facing = state.getValue(FACING);
        // Получаем координаты блока-опоры (позади рисунка)
        BlockPos attachedPos = pos.relative(facing.getOpposite());
        // Проверяем, является ли поверхность опоры твердой и подходящей для установки
        return level.getBlockState(attachedPos).isFaceSturdy(level, attachedPos, facing);
    }

    @Override
    public BlockState updateShape(BlockState state, net.minecraft.core.Direction direction, BlockState neighborState, net.minecraft.world.level.LevelAccessor level, BlockPos currentPos, BlockPos neighborPos) {
        // Если блок больше не может "выжить" на этом месте (опору сломали)
        if (!state.canSurvive(level, currentPos)) {
            // Превращаем блок в Воздух (Air). При этом автоматически вызовется ваш метод onRemove!
            return net.minecraft.world.level.block.Blocks.AIR.defaultBlockState();
        }
        return super.updateShape(state, direction, neighborState, level, currentPos, neighborPos);
    }

    // Клик предметом
    @Override
    protected net.minecraft.world.ItemInteractionResult useItemOn(net.minecraft.world.item.ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, net.minecraft.world.InteractionHand hand, net.minecraft.world.phys.BlockHitResult hitResult) {
        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof SketchedPageBlockEntity pageEntity) {
            // Вращаем мгновенно на обеих сторонах (и на клиенте, и на сервере)
            pageEntity.setRotation(pageEntity.getRotation() + 1);
            return net.minecraft.world.ItemInteractionResult.sidedSuccess(level.isClientSide());
        }
        // Пропускаем клик, если это не наш BlockEntity
        return net.minecraft.world.ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    // Клик пустой рукой
    @Override
    protected net.minecraft.world.InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, net.minecraft.world.phys.BlockHitResult hitResult) {
        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof SketchedPageBlockEntity pageEntity) {
            // Вращаем мгновенно на обеих сторонах
            pageEntity.setRotation(pageEntity.getRotation() + 1);
            return net.minecraft.world.InteractionResult.sidedSuccess(level.isClientSide());
        }
        return net.minecraft.world.InteractionResult.PASS;
    }

    @Override
    public net.minecraft.world.level.block.RenderShape getRenderShape(BlockState state) {
        return net.minecraft.world.level.block.RenderShape.INVISIBLE;
    }

    protected static final net.minecraft.world.phys.shapes.VoxelShape NORTH_AABB = net.minecraft.world.level.block.Block.box(1.0D, 1.0D, 15.0D, 15.0D, 15.0D, 16.0D);
    protected static final net.minecraft.world.phys.shapes.VoxelShape SOUTH_AABB = net.minecraft.world.level.block.Block.box(1.0D, 1.0D, 0.0D, 15.0D, 15.0D, 1.0D);
    protected static final net.minecraft.world.phys.shapes.VoxelShape WEST_AABB = net.minecraft.world.level.block.Block.box(15.0D, 1.0D, 1.0D, 16.0D, 15.0D, 15.0D);
    protected static final net.minecraft.world.phys.shapes.VoxelShape EAST_AABB = net.minecraft.world.level.block.Block.box(0.0D, 1.0D, 1.0D, 1.0D, 15.0D, 15.0D);
    // НОВЫЕ: Хитбоксы для пола и потолка
    protected static final net.minecraft.world.phys.shapes.VoxelShape UP_AABB = net.minecraft.world.level.block.Block.box(1.0D, 0.0D, 1.0D, 15.0D, 1.0D, 15.0D);
    protected static final net.minecraft.world.phys.shapes.VoxelShape DOWN_AABB = net.minecraft.world.level.block.Block.box(1.0D, 15.0D, 1.0D, 15.0D, 16.0D, 15.0D);

    @Override
    public net.minecraft.world.phys.shapes.VoxelShape getShape(BlockState state, net.minecraft.world.level.BlockGetter level, net.minecraft.core.BlockPos pos, net.minecraft.world.phys.shapes.CollisionContext context) {
        net.minecraft.core.Direction dir = state.getValue(FACING);
        return switch (dir) {
            case NORTH -> NORTH_AABB;
            case SOUTH -> SOUTH_AABB;
            case WEST -> WEST_AABB;
            case EAST -> EAST_AABB;
            case UP -> UP_AABB;       // Если прикреплено к полу
            case DOWN -> DOWN_AABB;   // Если прикреплено к потолку
        };
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (state.getBlock() != newState.getBlock()) {
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (blockEntity instanceof SketchedPageBlockEntity pageEntity) {
                ItemStack drop = new ItemStack(ModItems.SKETCHED_PAGE.get());
                drop.set(ModDataComponents.PAGE_DATA.get(), pageEntity.getSketchData());
                net.minecraft.world.Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(), drop);
            }
            super.onRemove(state, level, pos, newState, isMoving);
        }
    }
}