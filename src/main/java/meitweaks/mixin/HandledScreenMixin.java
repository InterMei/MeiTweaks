package meitweaks.mixin;

import meitweaks.Colors;
import meitweaks.Tooltips;
import meitweaks.antichunkban.Saver;
import meitweaks.modules.AntiChunkbanModule;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractContainerScreen.class)
public class HandledScreenMixin extends Screen {
    protected HandledScreenMixin(Component title) {
        super(title);
    }

    @Inject(method = "init", at = @At("TAIL"))
    private void onInit(CallbackInfo ci) {
        if (AntiChunkbanModule.I == null || !AntiChunkbanModule.I.show()) return;

        int x = this.width - 154;
        int y = 4;
        int w = 150;
        int h = 16;

        this.addRenderableWidget(Tooltips.build(
            Button.builder(Component.literal("Anti-Chunkban: " + Colors.tf(Saver.chunkban)), button -> {
                Saver.chunkban = !Saver.chunkban;
                button.setMessage(Component.literal("Anti-Chunkban: " + Colors.tf(Saver.chunkban)));
                AntiChunkbanModule.pull();
            }).bounds(x, y, w, h)
        ));
    }
}
