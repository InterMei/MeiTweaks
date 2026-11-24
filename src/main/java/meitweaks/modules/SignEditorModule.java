package meitweaks.modules;

import meitweaks.Colors;
import meitweaks.MeiTweaksAddon;
import meitweaks.signeditor.SignEditor;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.settings.StringSetting;
import meteordevelopment.meteorclient.systems.modules.Module;

import java.util.function.Consumer;

public class SignEditorModule extends Module implements Consumer<Object> {
    public static SignEditorModule I;
    private static boolean P;

    private final Setting<Boolean> sShow;
    private final Setting<Boolean> sChat;
    private final Setting<Boolean> sAuto;
    private final Setting<Boolean> sNoRender;
    private final Setting<Boolean> sCustom;
    private final Setting<Boolean> sBack;
    private final Setting<Boolean> sMax;
    private final Setting<Integer> sLength;
    private final Setting<String> sText1;
    private final Setting<String> sText2;
    private final Setting<String> sText3;
    private final Setting<String> sText4;

    public SignEditorModule() {
        super(MeiTweaksAddon.CATEGORY, "sign-editor",
            "Sign editor tools (auto sign, custom text, back side, no sign render) on container screens.");
        I = this;

        SettingGroup sg = settings.getDefaultGroup();

        sShow = sg.add(new BoolSetting.Builder()
            .name("show-buttons")
            .description("Show the Sign Editor buttons and text boxes on container screens.")
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
        sAuto = sg.add(new BoolSetting.Builder()
            .name("auto-sign")
            .description("Same as the Auto Sign button.")
            .defaultValue(false)
            .onChanged(this)
            .build()
        );
        sNoRender = sg.add(new BoolSetting.Builder()
            .name("no-render")
            .description("Same as the No Render button.")
            .defaultValue(false)
            .onChanged(this)
            .build()
        );
        sCustom = sg.add(new BoolSetting.Builder()
            .name("custom-mode")
            .description("Same as the Custom button.")
            .defaultValue(false)
            .onChanged(this)
            .build()
        );
        sBack = sg.add(new BoolSetting.Builder()
            .name("back-mode")
            .description("Same as the Back button.")
            .defaultValue(false)
            .onChanged(this)
            .build()
        );
        sMax = sg.add(new BoolSetting.Builder()
            .name("max-mode")
            .description("Same as the MAX button.")
            .defaultValue(false)
            .onChanged(this)
            .build()
        );
        sLength = sg.add(new IntSetting.Builder()
            .name("sign-length")
            .description("Sign text length (1-256).")
            .defaultValue(80)
            .range(1, 256)
            .sliderRange(1, 256)
            .onChanged(this)
            .build()
        );
        sText1 = sg.add(new StringSetting.Builder()
            .name("text-1")
            .description("Custom text line 1.")
            .defaultValue("")
            .onChanged(this)
            .build()
        );
        sText2 = sg.add(new StringSetting.Builder()
            .name("text-2")
            .description("Custom text line 2.")
            .defaultValue("")
            .onChanged(this)
            .build()
        );
        sText3 = sg.add(new StringSetting.Builder()
            .name("text-3")
            .description("Custom text line 3.")
            .defaultValue("")
            .onChanged(this)
            .build()
        );
        sText4 = sg.add(new StringSetting.Builder()
            .name("text-4")
            .description("Custom text line 4.")
            .defaultValue("")
            .onChanged(this)
            .build()
        );
    }

    @Override
    public void accept(Object value) {
        if (!P && isActive()) {
            announce();
            applyAll();
        }
    }

    public void announce() {
        if (!sAuto.get().equals(SignEditor.autoSign)) say("Auto Sign: ".concat(Colors.tf(sAuto.get())));
        if (!sNoRender.get().equals(SignEditor.noRender)) say("No Render: ".concat(Colors.tf(sNoRender.get())));
        if (!sCustom.get().equals(SignEditor.CustomMode)) say("Custom: ".concat(Colors.tf(sCustom.get())));
        if (!sBack.get().equals(SignEditor.BackMode)) say("Back: ".concat(Colors.tf(sBack.get())));
        if (!sMax.get().equals(SignEditor.MAXMODE)) say("MAX: ".concat(Colors.tf(sMax.get())));
    }

    public void applyAll() {
        SignEditor.autoSign = sAuto.get();
        SignEditor.noRender = sNoRender.get();
        SignEditor.CustomMode = sCustom.get();
        SignEditor.BackMode = sBack.get();
        SignEditor.MAXMODE = sMax.get();
        SignEditor.signLength = sLength.get();
        SignEditor.customText1 = sText1.get();
        SignEditor.customText2 = sText2.get();
        SignEditor.customText3 = sText3.get();
        SignEditor.customText4 = sText4.get();
    }

    @Override
    public void onActivate() {
        applyAll();
    }

    @Override
    public void onDeactivate() {
        SignEditor.autoSign = false;
        SignEditor.noRender = false;
        SignEditor.CustomMode = false;
        SignEditor.BackMode = false;
        SignEditor.MAXMODE = false;
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

    /** Pulls the values from {@link SignEditor} back into the settings (used by the on-screen buttons). */
    public void pullAll() {
        if (isActive()) {
            P = true;
            if (!sAuto.get().equals(SignEditor.autoSign)) say("Auto Sign: ".concat(Colors.tf(SignEditor.autoSign)));
            sAuto.set(SignEditor.autoSign);
            if (!sNoRender.get().equals(SignEditor.noRender)) say("No Render: ".concat(Colors.tf(SignEditor.noRender)));
            sNoRender.set(SignEditor.noRender);
            if (!sCustom.get().equals(SignEditor.CustomMode)) say("Custom: ".concat(Colors.tf(SignEditor.CustomMode)));
            sCustom.set(SignEditor.CustomMode);
            if (!sBack.get().equals(SignEditor.BackMode)) say("Back: ".concat(Colors.tf(SignEditor.BackMode)));
            sBack.set(SignEditor.BackMode);
            if (!sMax.get().equals(SignEditor.MAXMODE)) say("MAX: ".concat(Colors.tf(SignEditor.MAXMODE)));
            sMax.set(SignEditor.MAXMODE);
            sLength.set(SignEditor.signLength);
            sText1.set(SignEditor.customText1);
            sText2.set(SignEditor.customText2);
            sText3.set(SignEditor.customText3);
            sText4.set(SignEditor.customText4);
            P = false;
        }
    }
}
