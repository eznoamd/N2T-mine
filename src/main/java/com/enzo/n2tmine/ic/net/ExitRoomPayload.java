package com.enzo.n2tmine.ic.net;

import com.enzo.n2tmine.N2TMine;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;

/** Cliente -> Servidor: pede pra teleportar o jogador pra fora da sala roomId. */
public record ExitRoomPayload(int roomId) implements CustomPayload {

    public static final CustomPayload.Id<ExitRoomPayload> ID =
            new CustomPayload.Id<>(N2TMine.id("exit_room"));

    public static final PacketCodec<RegistryByteBuf, ExitRoomPayload> CODEC = PacketCodec.tuple(
            PacketCodecs.VAR_INT, ExitRoomPayload::roomId,
            ExitRoomPayload::new
    );

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
