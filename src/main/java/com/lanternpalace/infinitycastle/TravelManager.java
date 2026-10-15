package com.lanternpalace.infinitycastle;

import com.lanternpalace.infinitycastle.gen.Dim;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap;

/**
 * Entering / leaving the dimension. Teleports go through vanilla `execute in <dim> run tp`, which handles the dimension
 * change, chunk loading and client sync for us. The server-side permission check is the normal command system.
 */
public final class TravelManager {
    private TravelManager() {}

    private record ReturnPoint(ResourceKey<Level> dim, double x, double y, double z, float yaw) {}

    private static final Map<UUID, ReturnPoint> RETURNS = new ConcurrentHashMap<>();

    public static void register() {
        // Safety net: anyone who somehow falls out of the world is lifted back to the palace courtyard.
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            if (server.getTickCount() % 10 != 0) return;
            for (ServerPlayer p : server.getPlayerList().getPlayers()) {
                if (p.level().dimension().equals(Dimensions.CASTLE) && p.getY() < Dim.MIN_Y + 24) {
                    teleport(server, p, Dimensions.CASTLE_ID, Dim.SPAWN_X, Dim.SPAWN_Y, Dim.SPAWN_Z, 180f);
                    slowFall(server, p);
                }
            }
        });
    }

    private static void run(MinecraftServer server, String command) {
        server.getCommands().performPrefixedCommand(server.createCommandSourceStack().withSuppressedOutput(), command);
    }

    private static void teleport(MinecraftServer server, ServerPlayer p, String dim, double x, double y, double z, float yaw) {
        run(server, String.format(Locale.ROOT, "execute in %s run tp %s %.3f %.3f %.3f %.2f 0", dim, p.getStringUUID(), x, y, z, yaw));
    }

    private static void slowFall(MinecraftServer server, ServerPlayer p) {
        run(server, "effect give " + p.getStringUUID() + " minecraft:slow_falling 8 0 true");
    }

    public static int enter(CommandSourceStack src) throws CommandSyntaxException {
        ServerPlayer player = src.getPlayerOrException();
        MinecraftServer server = src.getServer();
        ServerLevel castle = server.getLevel(Dimensions.CASTLE);
        if (castle == null) {
            src.sendFailure(Component.literal("The Infinity Castle dimension is not loaded on this server (is the mod data pack active?)."));
            return 0;
        }
        if (player.level().dimension().equals(Dimensions.CASTLE)) {
            src.sendFailure(Component.literal("You are already inside the Infinity Castle."));
            return 0;
        }
        RETURNS.put(player.getUUID(), new ReturnPoint(player.level().dimension(), player.getX(), player.getY(), player.getZ(), player.getYRot()));
        // Generate the arrival chunks first so the courtyard floor exists before the player lands.
        int cx = ((int) Math.floor(Dim.SPAWN_X)) >> 4, cz = ((int) Math.floor(Dim.SPAWN_Z)) >> 4;
        for (int dx = -1; dx <= 1; dx++) for (int dz = -1; dz <= 1; dz++) castle.getChunk(cx + dx, cz + dz);
        teleport(server, player, Dimensions.CASTLE_ID, Dim.SPAWN_X, Dim.SPAWN_Y, Dim.SPAWN_Z, 180f);
        slowFall(server, player);
        src.sendSuccess(() -> Component.literal("Welcome to the Infinity Castle - a peaceful realm. No fighting, no hostile mobs. /infinitycastle exit to leave."), false);
        return 1;
    }

    public static int exit(CommandSourceStack src) throws CommandSyntaxException {
        ServerPlayer player = src.getPlayerOrException();
        MinecraftServer server = src.getServer();
        if (!player.level().dimension().equals(Dimensions.CASTLE)) {
            src.sendFailure(Component.literal("You are not inside the Infinity Castle."));
            return 0;
        }
        ReturnPoint rp = RETURNS.remove(player.getUUID());
        if (rp != null && server.getLevel(rp.dim()) != null) {
            teleport(server, player, Dimensions.commandId(rp.dim()), rp.x(), rp.y(), rp.z(), rp.yaw());
        } else {
            ServerLevel overworld = server.overworld();
            int y = overworld.getHeight(Heightmap.Types.MOTION_BLOCKING, 0, 0);
            teleport(server, player, "minecraft:overworld", 0.5, y + 1, 0.5, 0f);
        }
        slowFall(server, player);
        src.sendSuccess(() -> Component.literal("Returned from the Infinity Castle."), false);
        return 1;
    }
}
