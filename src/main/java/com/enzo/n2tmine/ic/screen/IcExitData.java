package com.enzo.n2tmine.ic.screen;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;

/**
 * Dados que o servidor envia pro cliente no momento em que o menu do bloco de
 * saida abre: qual sala e o nome atual dela.
 */
public record IcExitData(int roomId, String name) {

    public static final PacketCodec<RegistryByteBuf, IcExitData> CODEC = PacketCodec.tuple(
            PacketCodecs.VAR_INT, IcExitData::roomId,
            PacketCodecs.STRING, IcExitData::name,
            IcExitData::new
    );
}
