package com.enzo.n2tmine.ic.screen;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;

/** Dados enviados ao cliente ao abrir o menu: qual sala (design) e o nome atual. */
public record IcExitData(int roomId, String name) {

    public static final PacketCodec<RegistryByteBuf, IcExitData> CODEC = PacketCodec.tuple(
            PacketCodecs.VAR_INT, IcExitData::roomId,
            PacketCodecs.STRING, IcExitData::name,
            IcExitData::new
    );
}
