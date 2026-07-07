package com.enzo.n2tmine.ic;

import net.minecraft.block.Block;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.block.ShapeContext;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class IcBlock extends Block implements BlockEntityProvider {

    // Formato de slab (metade de baixo): 16 x 8 x 16.
    private static final VoxelShape SHAPE = Block.createCuboidShape(0.0, 0.0, 0.0, 16.0, 8.0, 16.0);

    public IcBlock(Settings settings) {
        super(settings);
    }

    @Override
    public @Nullable BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new IcBlockEntity(pos, state);
    }

    @Override
    protected VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return SHAPE;
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return SHAPE;
    }

    // Ao quebrar: em modo sobrevivencia, dropa o proprio IC Block ja com o roomId
    // guardado no item, e libera o vinculo da posicao antiga.
    // (A loot table foi esvaziada de proposito pra nao dropar duas vezes.)
    @Override
    public BlockState onBreak(World world, BlockPos pos, BlockState state, PlayerEntity player) {
        if (!world.isClient && world instanceof ServerWorld serverWorld
                && world.getBlockEntity(pos) instanceof IcBlockEntity be) {
            int designId = be.getDesignId();
            int instanceId = be.getInstanceRoomId();
            if (!player.isCreative()) {
                ItemStack stack = new ItemStack(ModIcBlocks.IC_BLOCK);
                if (designId >= 0) {
                    // O item leva o DESIGN (mestre), nao a instancia -- assim, ao
                    // recolocar, ele cria uma nova instancia do mesmo design.
                    stack.set(ModComponents.ROOM_ID, designId);
                    String roomName = IcRoomState.get(serverWorld.getServer()).getRoomName(designId);
                    if (!roomName.isEmpty()) {
                        stack.set(net.minecraft.component.DataComponentTypes.CUSTOM_NAME,
                                net.minecraft.text.Text.literal(roomName));
                    }
                }
                dropStack(world, pos, stack);
            }
            // Libera so a INSTANCIA deste bloco. O mestre (design) permanece pra
            // outras copias e pro item guardado.
            if (designId >= 0 && instanceId >= 0) {
                IcRoomState.get(serverWorld.getServer())
                        .releaseInstance(serverWorld.getServer(), designId, instanceId);
            }
        }
        return super.onBreak(world, pos, state, player);
    }

    // Ao colocar: se o item tinha um design guardado, entra nesse design (cria
    // uma instancia propria). Sem design, o primeiro tick cria um design novo.
    @Override
    public void onPlaced(World world, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack itemStack) {
        super.onPlaced(world, pos, state, placer, itemStack);
        if (!world.isClient && world instanceof ServerWorld serverWorld
                && world.getBlockEntity(pos) instanceof IcBlockEntity be) {
            Integer designId = itemStack.get(ModComponents.ROOM_ID);
            if (designId != null && designId >= 0) {
                be.adoptRoom(serverWorld, designId);
            }
        }
    }

    @Override
    protected boolean emitsRedstonePower(BlockState state) {
        return true;
    }

    @Override
    protected int getWeakRedstonePower(BlockState state, BlockView world, BlockPos pos, Direction direction) {
        if (world.getBlockEntity(pos) instanceof IcBlockEntity be) {
            // Convencao do Minecraft (confirmada no Observer vanilla): o parametro
            // `direction` e a direcao DO VIZINHO apontando pra ca -- ou seja, o
            // OPOSTO da face em que queremos emitir. Pra emitir na face F, retorna
            // o valor guardado pra F quando direction == F.getOpposite().
            return be.getOutputPower(direction.getOpposite());
        }
        return 0;
    }

    @Override
    protected int getStrongRedstonePower(BlockState state, BlockView world, BlockPos pos, Direction direction) {
        return getWeakRedstonePower(state, world, pos, direction);
    }

    // ATENCAO: a assinatura exata deste metodo (onUse) muda com frequencia entre
    // versoes do Minecraft. Se der erro de compilacao aqui, deixe o IDE sugerir
    // a assinatura correta pra sua versao (geralmente so muda a ordem/presenca
    // do parametro Hand) e me manda o erro que eu ajusto.
    @Override
    protected ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
        if (world.isClient) {
            return ActionResult.SUCCESS;
        }
        if (!(world instanceof ServerWorld serverWorld) || !(player instanceof ServerPlayerEntity serverPlayer)) {
            return ActionResult.PASS;
        }
        if (world.getBlockEntity(pos) instanceof IcBlockEntity be) {
            be.teleportPlayerInside(serverWorld, serverPlayer);
            return ActionResult.CONSUME;
        }
        return ActionResult.PASS;
    }

    @SuppressWarnings("unchecked")
    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
        if (type != ModIcBlockEntities.IC_BLOCK_ENTITY) {
            return null;
        }
        return (BlockEntityTicker<T>) (BlockEntityTicker<IcBlockEntity>) IcBlockEntity::tick;
    }
}
