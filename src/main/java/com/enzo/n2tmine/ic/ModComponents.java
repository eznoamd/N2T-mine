package com.enzo.n2tmine.ic;

import com.enzo.n2tmine.N2TMine;
import com.mojang.serialization.Codec;
import net.minecraft.component.ComponentType;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;

public class ModComponents {

    // Guarda o "endereco" (roomId) da sala na dimensao do CI dentro do item.
    // Quando o jogador quebra o bloco, esse valor vai pro item; quando recoloca,
    // o bloco readota a mesma sala.
    public static final ComponentType<Integer> ROOM_ID = Registry.register(
            Registries.DATA_COMPONENT_TYPE,
            N2TMine.id("room_id"),
            ComponentType.<Integer>builder()
                    .codec(Codec.INT)
                    .packetCodec(PacketCodecs.VAR_INT)
                    .build()
    );

    public static void initialize() {
        // Apenas forca o carregamento estatico desta classe
    }
}
