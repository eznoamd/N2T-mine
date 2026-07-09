package com.enzo.n2tmine.ic.net;

import com.enzo.n2tmine.ic.IcExitBlockEntity;
import com.enzo.n2tmine.ic.IcRoomState;
import com.enzo.n2tmine.ic.ModDimensions;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;

public class ModNetworking {

    public static void initialize() {
        // Registra os tipos de pacote (C2S)
        PayloadTypeRegistry.playC2S().register(RenameRoomPayload.ID, RenameRoomPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(ExitRoomPayload.ID, ExitRoomPayload.CODEC);

        // Handler: renomear sala (design)
        ServerPlayNetworking.registerGlobalReceiver(RenameRoomPayload.ID, (payload, context) -> {
            ServerPlayerEntity player = context.player();
            MinecraftServer server = player.getServer();
            if (server == null) return;
            server.execute(() -> {
                IcRoomState.get(server).renameRoom(payload.roomId(), payload.name());
            });
        });

        // Handler: sair da sala (mestre) -> re-sincroniza todas as instancias
        // daquele design com o mestre editado, e so entao teleporta pra fora.
        ServerPlayNetworking.registerGlobalReceiver(ExitRoomPayload.ID, (payload, context) -> {
            ServerPlayerEntity player = context.player();
            MinecraftServer server = player.getServer();
            if (server == null) return;
            server.execute(() -> {
                int masterRoomId = payload.roomId();
                // Propaga o design editado pra todas as copias.
                IcRoomState.get(server).resyncInstances(server, masterRoomId);

                ServerWorld icWorld = server.getWorld(ModDimensions.IC_WORLD);
                if (icWorld == null) return;
                BlockPos exitPos = IcRoomState.get(server).getExitPos(masterRoomId);
                if (icWorld.getBlockEntity(exitPos) instanceof IcExitBlockEntity be) {
                    be.teleportPlayerOut(icWorld, player);
                }
            });
        });
    }
}
