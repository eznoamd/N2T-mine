package com.enzo.n2tmine.ic;

import com.enzo.n2tmine.N2TMine;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.MapColor;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;

public class ModIcBlocks {

    // Bloco visivel, colocavel pelo jogador
    public static final IcBlock IC_BLOCK = register(
            "ic_block",
            new IcBlock(AbstractBlock.Settings.create().mapColor(MapColor.IRON_GRAY).strength(3.0f)),
            true
    );

    // Porta interna (colocada automaticamente pelo IcRoomState, nunca pelo jogador)
    public static final IcPortBlock IC_PORT_BLOCK = register(
            "ic_port_block",
            new IcPortBlock(AbstractBlock.Settings.create().mapColor(MapColor.LAPIS_BLUE).strength(3.0f)),
            false
    );

    // Bloco de saida interno (colocado automaticamente)
    public static final IcExitBlock IC_EXIT_BLOCK = register(
            "ic_exit_block",
            new IcExitBlock(AbstractBlock.Settings.create().mapColor(MapColor.GOLD).strength(3.0f)),
            false
    );

    private static <T extends Block> T register(String path, T block, boolean withItem) {
        T registered = Registry.register(Registries.BLOCK, N2TMine.id(path), block);
        if (withItem) {
            Registry.register(Registries.ITEM, N2TMine.id(path),
                    new BlockItem(registered, new Item.Settings()));
        }
        return registered;
    }

    public static void initialize() {
        // Apenas forca o carregamento estatico desta classe
    }
}
