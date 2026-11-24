package meitweaks.gui;

import net.minecraft.client.gui.components.EditBox;

import java.util.List;

/** Shared Up/Down chat-history navigation for the suggestion text boxes. */
public final class HistNav {
    private static String draft = "";

    private HistNav() {
    }

    public static void move(EditBox box, int direction) {
        List<String> history = Panel.history;
        if (history == null || history.isEmpty() || box == null) return;

        int size = history.size();
        int index = Panel.hi;
        if (index < 0 || index > size) index = size;

        try {
            if (direction < 0) {
                if (index <= 0) return;
                if (index == size) {
                    draft = box.getValue();
                }
                index--;
                box.setValue(history.get(index));
            } else {
                if (index >= size) return;
                index++;
                if (index >= size) {
                    box.setValue(draft != null ? draft : "");
                } else {
                    box.setValue(history.get(index));
                }
            }
            Panel.hi = index;
        } catch (Throwable ignored) {
        }
    }
}
