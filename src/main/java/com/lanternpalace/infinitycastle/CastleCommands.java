package com.lanternpalace.infinitycastle;

import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

public final class CastleCommands {
    private CastleCommands() {}

    public static void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, buildContext, selection) ->
                dispatcher.register(Commands.literal("infinitycastle")
                        .then(Commands.literal("enter").executes(ctx -> TravelManager.enter(ctx.getSource())))
                        .then(Commands.literal("exit").executes(ctx -> TravelManager.exit(ctx.getSource())))
                        .executes(ctx -> {
                            ctx.getSource().sendSuccess(() -> Component.literal("Infinity Castle: /infinitycastle enter  |  /infinitycastle exit"), false);
                            return 1;
                        })));
    }
}
