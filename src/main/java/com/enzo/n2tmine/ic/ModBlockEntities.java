package com.enzo.n2tmine.ic;

import com.enzo.n2tmine.N2TMine;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;

public class ModBlockEntities {

    public static final BlockEntityType<IcBlockEntity> IC_BLOCK_ENTITY = Registry.register(
            Registries.BLOCK_ENTITY_TYPE,
            N2TMine.id("ic_block_entity"),
            FabricBlockEntityTypeBuilder.create(IcBlockEntity::new, ModBlocks.IC_BLOCK).build()
    );

    public static final BlockEntityType<IcPortBlockEntity> IC_PORT_BLOCK_ENTITY = Registry.register(
            Registries.BLOCK_ENTITY_TYPE,
            N2TMine.id("ic_port_block_entity"),
            FabricBlockEntityTypeBuilder.create(IcPortBlockEntity::new, ModBlocks.IC_PORT_BLOCK).build()
    );

    public static final BlockEntityType<IcExitBlockEntity> IC_EXIT_BLOCK_ENTITY = Registry.register(
            Registries.BLOCK_ENTITY_TYPE,
            N2TMine.id("ic_exit_block_entity"),
            FabricBlockEntityTypeBuilder.create(IcExitBlockEntity::new, ModBlocks.IC_EXIT_BLOCK).build()
    );

    public static void initialize() {
    }
}
