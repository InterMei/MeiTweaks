package meitweaks.modules;

import meitweaks.Colors;
import meitweaks.MeiTweaksAddon;
import meitweaks.antichunkban.Saver;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Module;

import java.util.function.Consumer;

public class AntiChunkbanModule extends Module implements Consumer<Boolean> {
    public static AntiChunkbanModule I;
    private static boolean P;

    private final Setting<Boolean> sShow;
    private final Setting<Boolean> sChat;
    private final Setting<Boolean> sChunkban;
    private final Setting<Boolean> sBookSign;
    private final Setting<Boolean> sBookDrop;

    public AntiChunkbanModule() {
        super(MeiTweaksAddon.CATEGORY, "anti-chunkban",
            "Anti chunk-ban packet protection plus the chunk-ban/book tools on container and book screens.");
        I = this;
        Saver.chunkban = false;

        SettingGroup sg = settings.getDefaultGroup();

        sShow = sg.add(new BoolSetting.Builder()
            .name("show-buttons")
            .description("Show the Anti-Chunkban buttons on container screens and the book screen.")
            .defaultValue(true)
            .onChanged(this)
            .build()
        );
        sChat = sg.add(new BoolSetting.Builder()
            .name("chat-feedback")
            .description("Print what each button did.")
            .defaultValue(true)
            .onChanged(this)
            .build()
        );
        sChunkban = sg.add(new BoolSetting.Builder()
            .name("anti-chunkban")
            .description("Same as the Anti-Chunkban button.")
            .defaultValue(true)
            .onChanged(this)
            .build()
        );
        sBookSign = sg.add(new BoolSetting.Builder()
            .name("book-sign")
            .description("Same as the Sign button on the book screen.")
            .defaultValue(true)
            .onChanged(this)
            .build()
        );
        sBookDrop = sg.add(new BoolSetting.Builder()
            .name("book-drop")
            .description("Same as the Drop button on the book screen.")
            .defaultValue(false)
            .onChanged(this)
            .build()
        );
    }

    @Override
    public void accept(Boolean value) {
        if (!P && isActive()) {
            announce();
            applyAll();
        }
    }

    public void announce() {
        if (!sChunkban.get().equals(Saver.chunkban)) say("Anti-Chunkban: ".concat(Colors.tf(sChunkban.get())));
        if (!sBookSign.get().equals(Saver.signEnabled)) say("Book sign: ".concat(Colors.tf(sBookSign.get())));
        if (!sBookDrop.get().equals(Saver.dropsEnabled)) say("Book drop: ".concat(Colors.tf(sBookDrop.get())));
    }

    public void applyAll() {
        Saver.chunkban = sChunkban.get();
        Saver.signEnabled = sBookSign.get();
        Saver.dropsEnabled = sBookDrop.get();
    }

    @Override
    public void onActivate() {
        applyAll();
    }

    @Override
    public void onDeactivate() {
        Saver.chunkban = false;
    }

    public void say(String message) {
        if (sChat.get()) info(message);
    }

    public boolean show() {
        return isActive() && sShow.get();
    }

    public static void pull() {
        if (I != null) I.pullAll();
    }

    /** Pulls the values from {@link Saver} back into the settings (used by the on-screen buttons). */
    public void pullAll() {
        if (isActive()) {
            P = true;
            if (!sChunkban.get().equals(Saver.chunkban)) say("Anti-Chunkban: ".concat(Colors.tf(Saver.chunkban)));
            sChunkban.set(Saver.chunkban);
            if (!sBookSign.get().equals(Saver.signEnabled)) say("Book sign: ".concat(Colors.tf(Saver.signEnabled)));
            sBookSign.set(Saver.signEnabled);
            if (!sBookDrop.get().equals(Saver.dropsEnabled)) say("Book drop: ".concat(Colors.tf(Saver.dropsEnabled)));
            sBookDrop.set(Saver.dropsEnabled);
            P = false;
        }
    }
}
