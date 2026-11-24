package meitweaks.gui;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.suggestion.Suggestion;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.commands.Commands;
import meteordevelopment.meteorclient.systems.config.Config;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.commands.CommandSourceStack;

import java.util.List;

/** Feeds the game's own command/chat suggestion system into the Sign Editor / UI Utils text boxes. */
public final class ChatSuggest {
    private ChatSuggest() {
    }

    private static String prefix() {
        try {
            String prefix = Config.get().prefix.get();
            return prefix != null ? prefix : "";
        } catch (Throwable t) {
            return "";
        }
    }

    public static void suggest(String value) {
        Minecraft mc = MeteorClient.mc;
        ClientPacketListener connection = mc.getConnection();
        String prefix = prefix();

        CommandDispatcher<CommandSourceStack> dispatcher;
        String command;

        if (!prefix.isEmpty() && value.startsWith(prefix)) {
            dispatcher = Commands.DISPATCHER;
            command = value.substring(prefix.length());
        } else if (connection != null && value.startsWith("/")) {
            dispatcher = connection.getCommands();
            command = value.substring(1);
        } else {
            Panel.showSuggestions(value, List.of());
            return;
        }

        if (connection == null || dispatcher == null) {
            Panel.showSuggestions(value, List.of());
            return;
        }

        try {
            ParseResults<CommandSourceStack> parsed = dispatcher.parse(command, connection.getSuggestionsProvider());
            dispatcher.getCompletionSuggestions(parsed, command.length())
                .thenAccept(suggestions -> mc.execute(() -> Panel.showSuggestions(value, suggestions.getList())))
                .exceptionally(t -> null);
        } catch (Throwable t) {
            Panel.showSuggestions(value, List.of());
        }
    }

    public static void pick(Suggestion suggestion, EditBox box) {
        String value = box.getValue();
        if (value == null || value.isEmpty()) return;

        String prefix = prefix();
        if (!prefix.isEmpty() && value.startsWith(prefix)) {
            box.setValue(prefix + suggestion.apply(value.substring(prefix.length())));
        } else if (value.startsWith("/")) {
            box.setValue("/" + suggestion.apply(value.substring(1)));
        }
    }
}
