package meitweaks.signeditor;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;

import java.util.List;

public class SignInput {
    private static boolean isSign(EditBox box) {
        String message = box.getMessage().getString();
        return message.equals("Sign Length") || message.startsWith("Custom Text");
    }

    private static EditBox active(List<?> children) {
        for (Object child : children) {
            if (child instanceof EditBox box && box.isFocused() && box.visible && isSign(box)) {
                return box;
            }
        }
        return null;
    }

    public static boolean type(List<?> children, CharacterEvent event) {
        EditBox box = active(children);
        if (box != null && box.canConsumeInput()) {
            return box.charTyped(event);
        }
        return false;
    }

    public static boolean key(Minecraft mc, List<?> children, KeyEvent event) {
        EditBox box = active(children);
        if (box != null && box.canConsumeInput() && !event.isEscape()) {
            box.keyPressed(event);
            return true;
        }
        return false;
    }
}
