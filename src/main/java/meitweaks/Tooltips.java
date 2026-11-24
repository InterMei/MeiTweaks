package meitweaks;

import meteordevelopment.meteorclient.gui.GuiTheme;
import meteordevelopment.meteorclient.gui.widgets.pressable.WButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;

import java.lang.reflect.Field;
import java.util.HashMap;

public final class Tooltips {
    private static final HashMap<String, String> M = new HashMap<>();
    private static final String DEF_MC = "Click to use this suggestion.";
    private static final String DEF_W = "Click to run this action.";

    static {
        p("", "Click to run this action.");
        p("Sync", "Info: Sync ID and Revision (state ID) of the currently open GUI. Display only.");
        p("Close without packet", "Close the GUI on the client side without sending a close packet to the server.");
        p("Desync", "Send a close packet to the server, but keep the GUI open on your screen.");
        p("Send packets", "Sending container click packets. When false, click packets are cancelled and not sent.");
        p("Delay packets", "When true, container click packets are held in a queue. Turning it off sends the queue.");
        p("Leave & Send Packets", "Send all queued packets, then disconnect from the server immediately.");
        p("Fabricate packet", "Open a window to build a manual container click packet (Sync ID, Revision, Slot, Button).");
        p("Copy GUI Title JSON", "Copy the open GUI title as JSON to the clipboard.");
        p("Save GUI", "Save the open GUI so it can be loaded again later.");
        p("Load GUI", "Reopen the GUI saved with Save GUI.");
        p("Clear Queue", "Remove all packets currently waiting in the queue.");
        p("Queue", "Info: number of packets currently waiting in the queue. Display only.");
        p("Resync Inv", "Ask the server to resync the inventory contents with the client.");
        p("Disconnect", "Disconnect from the current server.");
        p("Anti-Chunkban", "Anti-Chunkban protection.");
        p("Sign", "The book is sent with a title (signed) when sent to the server.");
        p("Drop", "The book is dropped from your inventory after being sent.");
        p("75KB", "Send a book edit packet with roughly 75 KB of random content.");
        p("150KB", "Send a book edit packet with roughly 150 KB of random content.");
        p("200KB", "Send a book edit packet with roughly 200 KB of random content.");
        p("No Render", "Hide sign text rendering in the world.");
        p("Auto Sign", "Fill in sign text automatically. Text length is set in the Sign Length box.");
        p("Custom", "Use custom text from the Custom Text 1-4 boxes.");
        p("Back", "Apply the text to the back side of the sign.");
        p("MAX", "MAX mode for Auto Sign (maximum text length).");
        p("w:Type", "Switch packet type: Click Slot or Button Click.");
        p("w:Action", "Cycle the click action (PICKUP, QUICK_MOVE, etc.).");
        p("w:Send", "Send this packet to the server now.");
        p("w:Queue", "Add this packet to the queue to send later.");
    }

    private static void p(String label, String tooltip) {
        M.put(label, tooltip);
    }

    public static String key(String label) {
        return label.split(":")[0].split("#")[0].trim();
    }

    public static String text(String label) {
        return M.getOrDefault(key(label), DEF_MC);
    }

    public static String wtext(String label) {
        return M.getOrDefault("w:" + key(label), DEF_W);
    }

    public static Button build(Button.Builder builder) {
        Button button = builder.build();
        button.setTooltip(Tooltip.create(Component.literal(text(button.getMessage().getString()))));
        return button;
    }

    public static void setMsg(Button button, Component message) {
        button.setMessage(message);
        button.setTooltip(Tooltip.create(Component.literal(text(message.getString()))));
    }

    public static WButton wbutton(GuiTheme theme, String label) {
        WButton button = theme.button(label);
        try {
            Field tooltip = WButton.class.getField("tooltip");
            tooltip.set(button, wtext(label));
        } catch (Throwable ignored) {
        }
        return button;
    }
}
