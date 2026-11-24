package meitweaks;

import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;

public final class BookFix {
    private BookFix() {
    }

    /** Moves the vanilla book Copy/Paste buttons out of the way of the Sign Editor panel. */
    public static void moveCopyPaste(Screen screen) {
        int y = 96;
        try {
            for (GuiEventListener listener : screen.children()) {
                if (!(listener instanceof Button button)) continue;
                if (button.getX() > 12) continue;

                String label = button.getMessage().getString().trim();
                if (label.equalsIgnoreCase("Copy")) {
                    button.setY(y);
                } else if (label.equalsIgnoreCase("Paste")) {
                    button.setY(y + 22);
                }
            }
        } catch (Throwable ignored) {
        }
    }
}
