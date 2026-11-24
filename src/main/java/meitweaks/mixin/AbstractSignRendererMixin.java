package meitweaks.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import meitweaks.signeditor.SignEditor;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.AbstractSignRenderer;
import net.minecraft.client.renderer.blockentity.state.SignRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractSignRenderer.class)
public class AbstractSignRendererMixin {
    /** No Render: strip the text before the sign is drawn so laggy signs cost nothing. */
    @Inject(method = "submit", at = @At("HEAD"))
    private void submit(SignRenderState state, PoseStack poseStack, SubmitNodeCollector collector,
                        CameraRenderState camera, CallbackInfo ci) {
        if (SignEditor.noRender) {
            state.frontText = null;
            state.backText = null;
        }
    }
}
