package com.enzo.n2tmine.ic;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

public class IcPortBlockEntity extends BlockEntity {

    private int injectedPower = 0;
    private Direction inwardDirection = Direction.NORTH; // guardado so pra referencia/depuracao
    private PortRole role = PortRole.INPUT;              // toda porta comeca como ENTRADA

    public IcPortBlockEntity(BlockPos pos, BlockState state) {
        super(ModIcBlockEntities.IC_PORT_BLOCK_ENTITY, pos, state);
    }

    public int getInjectedPower() {
        // Uma porta de SAIDA nunca injeta sinal pra dentro, mesmo que por algum
        // motivo tenha um valor guardado. Isso e uma trava extra contra loops.
        return role == PortRole.INPUT ? injectedPower : 0;
    }

    public PortRole getRole() {
        return role;
    }

    /** Copia o papel de outra porta (usado ao clonar mestre -> instancia). */
    public void copyRoleFrom(IcPortBlockEntity other) {
        this.role = other.role;
        markDirty();
    }

    /** Alterna ENTRADA <-> SAIDA e retorna o novo papel. */
    public PortRole cycleRole(ServerWorld icWorld, BlockPos pos) {
        role = role.next();
        // Ao virar SAIDA, zera o que estava injetando (some o sinal que ela emitia
        // pra dentro); ao virar ENTRADA, o proximo tick reinjeta o valor externo.
        injectedPower = 0;
        markDirty();

        // Troca a propriedade OUTPUT do bloco pra mudar o modelo (lapis <-> esmeralda).
        // Como e o mesmo bloco (so muda a propriedade), o block entity e preservado.
        BlockState updated = getCachedState().with(IcPortBlock.OUTPUT, role == PortRole.OUTPUT);
        icWorld.setBlockState(pos, updated, net.minecraft.block.Block.NOTIFY_ALL);

        return role;
    }

    public void setInwardDirection(Direction direction) {
        this.inwardDirection = direction;
        markDirty();
    }

    public void setInjectedPower(ServerWorld icWorld, BlockPos pos, int power) {
        if (power != this.injectedPower) {
            this.injectedPower = power;
            markDirty();
            icWorld.updateNeighborsAlways(pos, getCachedState().getBlock());
        }
    }

    @Override
    protected void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registries) {
        super.writeNbt(nbt, registries);
        nbt.putInt("injectedPower", injectedPower);
        nbt.putString("inwardDirection", inwardDirection.getName());
        nbt.putString("role", role.name());
    }

    @Override
    protected void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registries) {
        super.readNbt(nbt, registries);
        injectedPower = nbt.getInt("injectedPower");
        String dirName = nbt.getString("inwardDirection");
        for (Direction dir : Direction.values()) {
            if (dir.getName().equals(dirName)) {
                inwardDirection = dir;
                break;
            }
        }
        String roleName = nbt.getString("role");
        if (!roleName.isEmpty()) {
            try {
                role = PortRole.valueOf(roleName);
            } catch (IllegalArgumentException ignored) {
                role = PortRole.INPUT;
            }
        }
    }
}
