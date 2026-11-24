package meitweaks.mixin;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.DecoderException;
import meitweaks.antichunkban.Saver;
import net.minecraft.network.CompressionDecoder;
import net.minecraft.network.FriendlyByteBuf;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.zip.Inflater;

@Mixin(CompressionDecoder.class)
public class CompressionDecoderMixin {
    @Shadow
    private Inflater inflater;

    /** Replaces the vanilla decode so large (chunk-ban sized) compressed packets are accepted up to 100 MB. */
    @Inject(method = "decode", at = @At("HEAD"), cancellable = true)
    private void onDecode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out, CallbackInfo ci) throws Exception {
        if (!Saver.chunkban) return;
        ci.cancel();

        if (in.readableBytes() != 0) {
            FriendlyByteBuf buf = new FriendlyByteBuf(in);
            int size = buf.readVarInt();
            if (size == 0) {
                out.add(buf.readBytes(buf.readableBytes()));
            } else {
                if (size > 100000000) {
                    throw new DecoderException("Badly compressed packet - size of " + size / 1000000 + "MB is too large");
                }
                byte[] compressed = new byte[buf.readableBytes()];
                buf.readBytes(compressed);
                this.inflater.setInput(compressed);
                byte[] result = new byte[size];
                this.inflater.inflate(result);
                out.add(Unpooled.wrappedBuffer(result));
                this.inflater.reset();
            }
        }
    }
}
