package meitweaks.mixin;

import meitweaks.gui.Panel;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.AbstractContainerMenu;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractContainerScreen.class)
public abstract class AbstractContainerScreenMixin<T extends AbstractContainerMenu> extends Screen {
    protected AbstractContainerScreenMixin(Component title) {
        super(title);
    }

    @Inject(method = "init()V", at = @At("TAIL"))
    private void uiutils$addPanel(CallbackInfo ci) {
        AbstractContainerScreenMixin<?> self = this;
        AbstractContainerScreenMixin<?> self2 = this;
        Panel.build(widget -> self.addRenderableWidget(widget), widget -> self2.removeWidget(widget));
    }

    @Inject(method = "keyPressed", at = @At("HEAD"), cancellable = true)
    private void uiutils$chatEnter(KeyEvent event, CallbackInfoReturnable<Boolean> cir) {
        if (Panel.onKey(event.key())) {
            cir.setReturnValue(true);
        }
    }
}
