package com.enzo.n2tmine;

import com.enzo.n2tmine.ic.ModIcBlockEntities;
import com.enzo.n2tmine.ic.ModIcBlocks;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
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

		// Registra os blocos e block entities do sistema de CI
		ModIcBlocks.initialize();
		ModIcBlockEntities.initialize();

		ItemGroupEvents.modifyEntriesEvent(ItemGroups.INGREDIENTS).register(entries -> {
			entries.add(RUBI_N2T);
		});

		ItemGroupEvents.modifyEntriesEvent(ItemGroups.REDSTONE).register(entries -> {
			entries.add(ModIcBlocks.IC_BLOCK.asItem());
		});
	}

	public static Identifier id(String path) {
		return Identifier.of(MOD_ID, path);
	}
}
