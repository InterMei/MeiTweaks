package meitweaks.gui;

import meitweaks.Colors;
import meitweaks.Tooltips;
import meitweaks.modules.UiUtilsModule;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.gui.GuiThemes;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.AbstractContainerMenu;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

/** Builds and drives the UI Utils / Anti-Chunkban button panel added to every container screen. */
public final class Panel {
    private static Button sync;
    private static Button send;
    private static Button delay;
    private static Button queue;
    private static Button spam;

    public static EditBox chat;

    private static Consumer<AbstractWidget> adder;
    private static Consumer<AbstractWidget> remover;

    public static List<Button> sug = new ArrayList<>();
    public static final List<String> history = new ArrayList<>();
    public static int hi;
    private static List<com.mojang.brigadier.suggestion.Suggestion> shown = List.of();

    private Panel() {
    }

    public static void build(Consumer<AbstractWidget> adder, Consumer<AbstractWidget> remover) {
        UiUtilsModule m = UiUtilsModule.I;
        if (m == null || !m.isActive()) return;

        Panel.adder = adder;
        Panel.remover = remover;
        shown = List.of();
        sug = new ArrayList<>();

        int x = 4;
        int y = 4;
        int w = 150;
        int h = 16;
        int gap = 2;
        int colHalf = (w - gap) / 2;
        int colThird = (w - 2 * gap) / 3;

        sync = btn(x, y, w, h, () -> {
        }, adder);
        sync.active = false;
        y += h + gap;

        btn("Close without packet", x, y, w, h, Objects.requireNonNull(m)::closeWithoutPacket, adder);
        y += h + gap;
        btn("Desync", x, y, w, h, Objects.requireNonNull(m)::desync, adder);
        y += h + gap;

        send = btn(x, y, w, h, Objects.requireNonNull(m)::toggleSend, adder);
        y += h + gap;

        delay = btn(x, y, w, h, Objects.requireNonNull(m)::toggleDelay, adder);
        y += h + gap;

        btn("Leave & Send Packets", x, y, w, h, Objects.requireNonNull(m)::leaveAndSend, adder);
        y += h + gap;

        btn("Fabricate packet", x, y, w, h, () -> MeteorClient.mc.gui.setScreen(new FabricateScreen(GuiThemes.get())), adder);
        y += h + gap;

        btn("Copy GUI Title JSON", x, y, w, h, Objects.requireNonNull(m)::copyTitleJson, adder);
        y += h + gap;

        btn("Save GUI", x, y, colHalf, h, Objects.requireNonNull(m)::saveGui, adder);
        btn("Load GUI", x + colHalf + gap, y, colHalf, h, Objects.requireNonNull(m)::loadGui, adder);
        y += h + gap;

        btn("Clear Queue", x, y, colHalf, h, Objects.requireNonNull(m)::clearQueue, adder);
        queue = btn(x + colHalf + gap, y, colHalf, h, () -> {
        }, adder);
        queue.active = false;
        y += h + gap;

        btn("Resync Inv", x, y, colHalf, h, Objects.requireNonNull(m)::resyncInventory, adder);
        btn("Disconnect", x + colHalf + gap, y, colHalf, h, Objects.requireNonNull(m)::disconnect, adder);
        y += h + gap + 48;

        chat = new EditBox(MeteorClient.mc.font, x, y, w, h, Component.literal("Chat"));
        chat.setMaxLength(256);
        chat.setHint(Component.literal("Chat..."));
        chat.setResponder(Panel::suggest);
        adder.accept(chat);

        refresh();
    }

    public static boolean onKey(int key) {
        if (chat == null || !chat.isFocused()) return false;

        switch (key) {
            case 257, 335 -> {
                String value = chat.getValue();
                if (!value.isBlank() && MeteorClient.mc.getConnection() != null) {
                    if (history.isEmpty() || !history.get(history.size() - 1).equals(value)) {
                        history.add(value);
                    }
                    hi = history.size();
                    ChatSend.send(value);
                    chat.setValue("");
                }
            }
            case 258 -> {
                if (!shown.isEmpty()) pick(shown.get(0));
            }
            case 266 -> {
                if (hi > 0) {
                    hi--;
                    chat.setValue(history.get(hi));
                }
            }
            case 267 -> {
                if (hi < history.size() - 1) {
                    hi++;
                    chat.setValue(history.get(hi));
                } else {
                    hi = history.size();
                    chat.setValue("");
                }
            }
            default -> {
                return false;
            }
        }
        return true;
    }

    private static void suggest(String value) {
        ChatSuggest.suggest(value);
    }

    private static void pick(com.mojang.brigadier.suggestion.Suggestion suggestion) {
        ChatSuggest.pick(suggestion, chat);
    }

    public static void showSuggestions(String value, List<com.mojang.brigadier.suggestion.Suggestion> suggestions) {
        if (chat == null || adder == null || !value.equals(chat.getValue())) return;

        sug.forEach(remover);
        sug.clear();

        shown = suggestions.subList(0, Math.min(3, suggestions.size()));

        int y = chat.getY() - 16;
        for (com.mojang.brigadier.suggestion.Suggestion suggestion : shown) {
            Button button = Tooltips.build(
                Button.builder(Component.literal(suggestion.getText()), b -> pick(suggestion))
                    .bounds(chat.getX(), y, chat.getWidth(), 14)
            );
            y -= 16;
            sug.add(button);
            adder.accept(button);
        }
    }

    public static void refresh() {
        UiUtilsModule m = UiUtilsModule.I;
        if (m == null || sync == null || MeteorClient.mc.player == null) return;

        AbstractContainerMenu menu = (AbstractContainerMenu) MeteorClient.mc.player.containerMenu;
        Tooltips.setMsg(sync, Component.literal(
            "Sync: " + menu.containerId + "  Rev: " + menu.getStateId()));
        Tooltips.setMsg(send, Component.literal("Send packets: " + Colors.tf(m.sendPackets)));
        Tooltips.setMsg(delay, Component.literal("Delay packets: " + Colors.tf(m.delayPackets)));
        Tooltips.setMsg(queue, Component.literal("Queue: " + m.queue.size()));
    }

    private static Button btn(String label, int x, int y, int w, int h, Runnable action, Consumer<AbstractWidget> adder) {
        Button button = Tooltips.build(
            Button.builder(Component.literal(label), b -> action.run()).bounds(x, y, w, h)
        );
        adder.accept(button);
        return button;
    }

    private static Button btn(int x, int y, int w, int h, Runnable action, Consumer<AbstractWidget> adder) {
        return btn("", x, y, w, h, action, adder);
    }
}
