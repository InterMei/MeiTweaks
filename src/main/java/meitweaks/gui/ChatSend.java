package meitweaks.gui;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.commands.Commands;
import meteordevelopment.meteorclient.systems.config.Config;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import net.minecraft.client.multiplayer.ClientPacketListener;

/** Sends a line of text as if it were typed into the real chat box (commands, client-commands, or chat). */
public final class ChatSend {
    private ChatSend() {
    }

    public static void send(String message) {
        ClientPacketListener connection = MeteorClient.mc.getConnection();
        if (connection == null) return;

        try {
            MeteorClient.mc.gui.hud.getChat().addRecentChat(message);
        } catch (Throwable ignored) {
        }

        String prefix = Config.get().prefix.get();
        if (prefix != null && !prefix.isEmpty() && message.startsWith(prefix)) {
            try {
                Commands.dispatch(message.substring(prefix.length()));
            } catch (CommandSyntaxException e) {
                ChatUtils.error(e.getMessage());
            }
            return;
        }

        if (message.startsWith("/")) {
            connection.sendCommand(message.substring(1));
        } else {
            connection.sendChat(message);
        }
    }
}
