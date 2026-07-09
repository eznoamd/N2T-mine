package com.enzo.n2tmine;

import com.enzo.n2tmine.ic.ModComponents;
import com.enzo.n2tmine.ic.ModBlockEntities;
import com.enzo.n2tmine.ic.ModBlocks;
import com.enzo.n2tmine.ic.net.ModNetworking;
import com.enzo.n2tmine.ic.recipe.ModRecipes;
import com.enzo.n2tmine.ic.screen.ModScreenHandlers;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class N2TMine implements ModInitializer {
	public static final String MOD_ID = "n2t-mine";

	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	public static final Item RUBI_N2T = Registry.register(
			Registries.ITEM,
			id("rubi_n2t"),
			new Item(new Item.Settings())
	);

	@Override
	public void onInitialize() {
		LOGGER.info("Hello Fabric world!");

		// Registra os blocos, block entities e componentes do sistema de CI
		ModComponents.initialize();
		ModBlocks.initialize();
		ModBlockEntities.initialize();
		ModScreenHandlers.initialize();
		ModNetworking.initialize();
		ModRecipes.initialize();

		// Impede o jogador de quebrar os blocos estruturais internos do CI
		// (paredes, portas e o bloco de saida) -- inclusive no criativo. O jogador
		// so pode quebrar o que ELE colocou dentro (redstone, CIs, etc.).
		PlayerBlockBreakEvents.BEFORE.register((world, player, pos, state, blockEntity) -> {
			Block b = state.getBlock();
			boolean estrutural = b == ModBlocks.IC_WALL
					|| b == ModBlocks.IC_PORT_BLOCK
					|| b == ModBlocks.IC_EXIT_BLOCK
					|| b == ModBlocks.IC_MARKER_NORTH
					|| b == ModBlocks.IC_MARKER_SOUTH;
			return !estrutural; // false = cancela a quebra
		});

		ItemGroupEvents.modifyEntriesEvent(ItemGroups.INGREDIENTS).register(entries -> {
			entries.add(RUBI_N2T);
		});

		ItemGroupEvents.modifyEntriesEvent(ItemGroups.REDSTONE).register(entries -> {
			entries.add(ModBlocks.IC_BLOCK.asItem());
		});
	}

	public static Identifier id(String path) {
		return Identifier.of(MOD_ID, path);
	}
}
