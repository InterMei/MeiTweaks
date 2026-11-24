package meitweaks.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;

import java.util.List;

/** Lets the chat/suggestion text box be dragged around the screen and remembers chat history while focused. */
public final class ChatDrag {
    private static EditBox cur;
    private static int baseX;
    private static int baseY;
    private static int offX;
    private static int offY;
    private static boolean dragging;
    private static int dragButton;
    private static int grabX;
    private static int grabY;
    private static int histPos = -1;
    private static String histBuf = "";
    private static boolean resetHeld;
    private static boolean pending;
    private static double pressX;
    private static double pressY;

    private static final int LEFT = 1;
    private static final int MIDDLE = 2;
    private static final int RIGHT = 3;

    private ChatDrag() {
    }

    private static int toPanelKey(int key) {
        return switch (key) {
            case 13 -> 257;          // Enter
            case 9 -> 258;           // Tab
            case 1073741899 -> 266;  // Page Up
            case 1073741902 -> 267;  // Page Down
            case 1073741912 -> 335;  // Keypad Enter
            default -> key;
        };
    }

    private static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(value, max));
    }

    private static void sync(EditBox box) {
        if (box != cur) {
            cur = box;
            baseX = box.getX();
            baseY = box.getY();
            dragging = false;
            pending = false;
            histPos = -1;
        }
    }

    private static void place(EditBox box, int screenW, int screenH) {
        box.setX(clamp(baseX + offX, 0, screenW - box.getWidth()));
        box.setY(clamp(baseY + offY, 0, screenH - box.getHeight()));
    }

    private static void layout(EditBox box) {
        List<Button> suggestions = Panel.sug;
        if (suggestions == null || suggestions.isEmpty()) return;

        int count = suggestions.size();
        boolean above = box.getY() < 16 * count;

        for (int i = 0; i < count; i++) {
            Button button = suggestions.get(i);
            button.setX(box.getX());
            if (above) {
                button.setY(box.getY() - 16 * (i + 1));
            } else {
                button.setY(box.getY() + box.getHeight() + 2 + 16 * i);
            }
        }
    }

    private static EditBox active(List<?> children) {
        EditBox chat = Panel.chat;
        return chat != null && children.contains(chat) ? chat : null;
    }

    public static void tick(int screenW, int screenH, List<?> children) {
        EditBox box = active(children);
        if (box == null) return;
        sync(box);
        if (!dragging) place(box, screenW, screenH);
        layout(box);
    }

    public static boolean click(int screenW, int screenH, List<?> children, double mouseX, double mouseY, int button) {
        if (button != LEFT && button != MIDDLE && button != RIGHT) return false;

        EditBox box = active(children);
        if (box == null || !box.isMouseOver(mouseX, mouseY)) return false;
        sync(box);

        if (button == MIDDLE) {
            dragging = false;
            pending = false;
            offX = 0;
            offY = 0;
            resetHeld = true;
            place(box, screenW, screenH);
            layout(box);
            return true;
        }

        grabX = (int) (mouseX - box.getX());
        grabY = (int) (mouseY - box.getY());

        if (button == LEFT) {
            pending = true;
            pressX = mouseX;
            pressY = mouseY;
            return false;
        }

        dragging = true;
        dragButton = button;
        return true;
    }

    public static boolean drag(int screenW, int screenH, double mouseX, double mouseY) {
        EditBox box = Panel.chat;
        if (box == null || box != cur) return false;

        if (!dragging && pending) {
            if (Math.abs(mouseX - pressX) < 4.0 && Math.abs(mouseY - pressY) < 4.0) return false;
            dragging = true;
            dragButton = LEFT;
            pending = false;
        }

        if (!dragging) return false;

        int x = clamp((int) mouseX - grabX, 0, screenW - box.getWidth());
        int y = clamp((int) mouseY - grabY, 0, screenH - box.getHeight());
        offX = x - baseX;
        offY = y - baseY;
        box.setX(x);
        box.setY(y);
        layout(box);
        return true;
    }

    public static boolean release(int button) {
        if (button == LEFT) {
            pending = false;
        }
        if (button == MIDDLE && resetHeld) {
            resetHeld = false;
            return true;
        }
        if (!dragging || button != dragButton) return false;
        dragging = false;
        return true;
    }

    private static ChatComponent vanillaChat(Minecraft mc) {
        if (mc == null || mc.gui == null || mc.gui.hud == null) return null;
        return mc.gui.hud.getChat();
    }

    private static void moveInHistory(Minecraft mc, EditBox box, int direction) {
        HistNav.move(box, direction);

        ChatComponent chat = vanillaChat(mc);
        if (chat == null) return;

        var recent = chat.getRecentChat();
        int size = recent.size();
        int pos = clamp(histPos < 0 || histPos > size ? size : histPos, 0, size);
        int next = clamp(pos + direction, 0, size);
        if (next == pos) return;

        if (next == size) {
            histPos = size;
            box.setValue(histBuf);
            return;
        }
        if (pos == size) {
            histBuf = box.getValue();
        }
        box.setValue(recent.get(next));
        histPos = next;
    }

    public static boolean type(List<?> children, CharacterEvent event) {
        EditBox box = active(children);
        return box != null && box.canConsumeInput() && box.charTyped(event);
    }

    public static boolean key(Minecraft mc, List<?> children, KeyEvent event) {
        EditBox box = active(children);
        if (box == null || !box.canConsumeInput()) return false;
        if (event.isEscape()) return false;

        int shortcut = event.shortcutKey();
        if (shortcut == 1073741899 || shortcut == 1073741906) {
            moveInHistory(mc, box, -1);
            return true;
        }
        if (shortcut == 1073741902 || shortcut == 1073741905) {
            moveInHistory(mc, box, 1);
            return true;
        }

        int panelKey = toPanelKey(shortcut);
        boolean isEnter = panelKey == 257 || panelKey == 335;
        String toSend = isEnter ? box.getValue() : null;

        if (!Panel.onKey(panelKey)) {
            box.keyPressed(event);
        }

        if (isEnter && toSend != null && !toSend.isBlank() && box.getValue().isEmpty()) {
            ChatComponent chat = vanillaChat(mc);
            if (chat != null) chat.addRecentChat(toSend);
            histPos = -1;
        }

        return true;
    }
}
