package meitweaks.mixin;

import meitweaks.Colors;
import meitweaks.Tooltips;
import meitweaks.modules.SignEditorModule;
import meitweaks.signeditor.SignEditor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Adds the Sign Editor buttons and text boxes to every container screen. */
@Mixin(AbstractContainerScreen.class)
public abstract class ScreenMixin extends Screen {
    @Unique private Button customButton;
    @Unique private Button backButton;
    @Unique private EditBox lengthBox;
    @Unique private EditBox customSign1;
    @Unique private EditBox customSign2;
    @Unique private EditBox customSign3;
    @Unique private EditBox customSign4;

    protected ScreenMixin(Component title) {
        super(title);
    }

    @Inject(method = "init", at = @At("TAIL"))
    private void init(CallbackInfo ci) {
        if (SignEditorModule.I == null || !SignEditorModule.I.show()) return;
        if (this.minecraft.player == null || this.minecraft.gui.screen() == null) return;

        int w = 150;
        int h = 16;
        int step = 22;
        int y = 18;

        Button noRender = Tooltips.build(
            Button.builder(
                Component.literal((SignEditor.noRender ? "§a" : "§c") + "No Render: " + SignEditor.noRender),
                button -> {
                    SignEditor.noRender = !SignEditor.noRender;
                    updatevision();
                    button.setMessage(Component.literal(
                        (SignEditor.noRender ? "§aNo Render: " : "§cNo Render: ") + SignEditor.noRender));
                    SignEditorModule.pull();
                }
            ).bounds(this.width - 154, y, w, h)
        );
        this.addRenderableWidget(noRender);
        y += step;

        Button autoSign = Tooltips.build(
            Button.builder(
                Component.literal((SignEditor.autoSign ? "§a" : "§c") + "Auto Sign: " + SignEditor.autoSign),
                button -> {
                    SignEditor.autoSign = !SignEditor.autoSign;
                    button.setMessage(Component.literal(
                        (SignEditor.autoSign ? "§aAuto Sign: " : "§cAuto Sign: ") + SignEditor.autoSign));
                    updatevision();
                    SignEditorModule.pull();
                }
            ).bounds(this.width - 154, y, w, h)
        );
        this.addRenderableWidget(autoSign);
        y += step;

        customButton = Tooltips.build(
            Button.builder(
                Component.literal((SignEditor.CustomMode ? "§a" : "§c") + "Custom: " + SignEditor.CustomMode),
                button -> {
                    SignEditor.CustomMode = !SignEditor.CustomMode;
                    button.setMessage(Component.literal(
                        (SignEditor.CustomMode ? "§aCustom: " : "§cCustom: ") + SignEditor.CustomMode));
                    updatevision();
                    SignEditorModule.pull();
                }
            ).bounds(this.width - 154, y, w, h)
        );
        customButton.visible = SignEditor.autoSign;
        this.addRenderableWidget(customButton);
        y += step;

        backButton = Tooltips.build(
            Button.builder(
                Component.literal((SignEditor.BackMode ? "§a" : "§c") + "Back: " + SignEditor.BackMode),
                button -> {
                    SignEditor.BackMode = !SignEditor.BackMode;
                    button.setMessage(Component.literal(
                        (SignEditor.BackMode ? "§aBack: " : "§cBack: ") + SignEditor.BackMode));
                    SignEditorModule.pull();
                }
            ).bounds(this.width - 154, y, w, h)
        );
        backButton.visible = SignEditor.autoSign;
        this.addRenderableWidget(backButton);
        y += step;

        lengthBox = new EditBox(this.font, this.width - 154, y, w, h, Component.literal("Sign Length"));
        lengthBox.setMaxLength(256);
        lengthBox.setValue(String.valueOf(SignEditor.signLength));
        lengthBox.setFocused(false);
        lengthBox.visible = SignEditor.autoSign;
        lengthBox.setResponder(value -> {
            try {
                SignEditor.signLength = (int) Math.clamp(value.isEmpty() ? 1 : Integer.parseInt(value), 1, 256);
            } catch (NumberFormatException e) {
                lengthBox.setValue(String.valueOf(SignEditor.signLength));
                if (SignEditor.mc.player != null) {
                    SignEditor.mc.player.sendOverlayMessage(Component.literal("§cPlease enter an Integer!"));
                }
            }
            SignEditorModule.pull();
        });
        this.addRenderableWidget(lengthBox);
        y += step;

        customSign1 = customTextBox("Custom Text 1", "Yo gurt", SignEditor.customText1, y,
            value -> { SignEditor.customText1 = value; SignEditorModule.pull(); });
        y += step;
        customSign2 = customTextBox("Custom Text 2", "Yo gurt 2", SignEditor.customText2, y,
            value -> { SignEditor.customText2 = value; SignEditorModule.pull(); });
        y += step;
        customSign3 = customTextBox("Custom Text 3", "Yo gurt 3", SignEditor.customText3, y,
            value -> { SignEditor.customText3 = value; SignEditorModule.pull(); });
        y += step;
        customSign4 = customTextBox("Custom Text 4", "Yo gurt 4", SignEditor.customText4, y,
            value -> { SignEditor.customText4 = value; SignEditorModule.pull(); });
        y += step;

        Button max = Tooltips.build(
            Button.builder(
                Component.literal((SignEditor.MAXMODE ? "§a" : "§c") + "MAX: " + SignEditor.MAXMODE),
                button -> {
                    SignEditor.MAXMODE = !SignEditor.MAXMODE;
                    updatevision();
                    button.setMessage(Component.literal(
                        (SignEditor.MAXMODE ? "§aMAX: " : "§cMAX: ") + SignEditor.MAXMODE));
                    SignEditorModule.pull();
                }
            ).bounds(this.width - 154, y, w, h)
        );
        this.addRenderableWidget(max);
    }

    @Unique
    private EditBox customTextBox(String title, String hint, String value, int y, java.util.function.Consumer<String> onChange) {
        EditBox box = new EditBox(this.font, this.width - 154, y, 150, 16, Component.literal(title));
        box.setMaxLength(256);
        box.setHint(Component.literal(hint));
        box.setValue(value);
        box.setFocused(false);
        box.visible = SignEditor.autoSign && SignEditor.CustomMode;
        box.setResponder(onChange::accept);
        this.addRenderableWidget(box);
        return box;
    }

    @Unique
    private void updatevision() {
        if (!SignEditor.MAXMODE) {
            if (customButton != null) customButton.visible = true;
            if (backButton != null) backButton.visible = true;
            if (lengthBox != null) lengthBox.visible = true;
            if (customSign1 != null) customSign1.visible = false;
            if (customSign2 != null) customSign2.visible = false;
            if (customSign3 != null) customSign3.visible = false;
            if (customSign4 != null) customSign4.visible = false;
            return;
        }

        if (customButton != null) customButton.visible = SignEditor.autoSign;
        if (backButton != null) backButton.visible = SignEditor.autoSign;
        if (lengthBox != null) lengthBox.visible = SignEditor.autoSign;

        boolean customVisible = SignEditor.autoSign && SignEditor.CustomMode;
        if (customSign1 != null) customSign1.visible = customVisible;
        if (customSign2 != null) customSign2.visible = customVisible;
        if (customSign3 != null) customSign3.visible = customVisible;
        if (customSign4 != null) customSign4.visible = customVisible;
    }
}
