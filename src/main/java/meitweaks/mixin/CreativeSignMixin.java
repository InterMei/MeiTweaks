package meitweaks.mixin;

import meitweaks.signeditor.SignInput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Pseudo
@Mixin(targets = "net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen")
public abstract class CreativeSignMixin extends Screen {
    protected CreativeSignMixin(Component title) {
        super(title);
    }

    @Inject(method = "charTyped", at = @At("HEAD"), cancellable = true, require = 0)
    private void mei$signChar(CharacterEvent event, CallbackInfoReturnable<Boolean> cir) {
        if (SignInput.type(this.children(), event)) {
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "keyPressed", at = @At("HEAD"), cancellable = true, require = 0)
    private void mei$signKey(KeyEvent event, CallbackInfoReturnable<Boolean> cir) {
        if (SignInput.key(this.minecraft, this.children(), event)) {
            cir.setReturnValue(true);
        }
    }
}
