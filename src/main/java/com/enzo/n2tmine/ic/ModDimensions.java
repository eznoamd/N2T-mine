package com.enzo.n2tmine.ic;

import com.enzo.n2tmine.N2TMine;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.world.World;

public class ModDimensions {
    // Referencia a dimensao definida em:
    // src/main/resources/data/n2t-mine/dimension/ic_world.json
    public static final RegistryKey<World> IC_WORLD = RegistryKey.of(
            RegistryKeys.WORLD,
            N2TMine.id("ic_world")
    );
}
