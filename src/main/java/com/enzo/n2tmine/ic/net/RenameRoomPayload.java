package com.enzo.n2tmine.ic.net;

import com.enzo.n2tmine.N2TMine;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;

/** Cliente -> Servidor: renomeia a sala (design) de roomId para `name`. */
public record RenameRoomPayload(int roomId, String name) implements CustomPayload {

    public static final CustomPayload.Id<RenameRoomPayload> ID =
            new CustomPayload.Id<>(N2TMine.id("rename_room"));

    public static final PacketCodec<RegistryByteBuf, RenameRoomPayload> CODEC = PacketCodec.tuple(
            PacketCodecs.VAR_INT, RenameRoomPayload::roomId,
            PacketCodecs.STRING, RenameRoomPayload::name,
            RenameRoomPayload::new
    );

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
