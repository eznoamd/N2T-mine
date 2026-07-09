package com.enzo.n2tmine.ic;

import com.enzo.n2tmine.N2TMine;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.MapColor;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;

public final class ModBlocks {

    private ModBlocks() {}

    /** Bloco de circuito integrado, colocado pelo jogador. */
    public static final IcBlock IC_BLOCK = register(
            "ic_block",
            new IcBlock(AbstractBlock.Settings.create().mapColor(MapColor.IRON_GRAY).strength(1.0f).nonOpaque()),
            true
    );

    // Os blocos abaixo formam a estrutura fixa de uma sala e sao colocados apenas
    // pelo sistema. Hardness -1 os torna indestrutiveis em sobrevivencia; o handler
    // de quebra em N2TMine tambem os protege no criativo.

    public static final Block IC_WALL = register(
            "ic_wall",
            new Block(AbstractBlock.Settings.create().mapColor(MapColor.STONE_GRAY).strength(-1.0f, 3600000.0f)),
            false
    );

    public static final IcPortBlock IC_PORT_BLOCK = register(
            "ic_port_block",
            new IcPortBlock(AbstractBlock.Settings.create().mapColor(MapColor.LAPIS_BLUE).strength(-1.0f, 3600000.0f)),
            false
    );

    public static final IcExitBlock IC_EXIT_BLOCK = register(
            "ic_exit_block",
            new IcExitBlock(AbstractBlock.Settings.create().mapColor(MapColor.GOLD).strength(-1.0f, 3600000.0f)),
            false
    );

    /** Marcadores de orientacao no topo das paredes: vermelho = norte, azul = sul. */
    public static final Block IC_MARKER_NORTH = register(
            "ic_marker_north",
            new Block(AbstractBlock.Settings.create().mapColor(MapColor.RED).strength(-1.0f, 3600000.0f)),
            false
    );

    public static final Block IC_MARKER_SOUTH = register(
            "ic_marker_south",
            new Block(AbstractBlock.Settings.create().mapColor(MapColor.BLUE).strength(-1.0f, 3600000.0f)),
            false
    );

    private static <T extends Block> T register(String path, T block, boolean withItem) {
        T registered = Registry.register(Registries.BLOCK, N2TMine.id(path), block);
        if (withItem) {
            Registry.register(Registries.ITEM, N2TMine.id(path), new BlockItem(registered, new Item.Settings()));
        }
        return registered;
    }

    public static void initialize() {}
}
