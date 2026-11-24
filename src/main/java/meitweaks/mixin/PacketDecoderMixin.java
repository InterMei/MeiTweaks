package meitweaks.mixin;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import meitweaks.antichunkban.Saver;
import net.minecraft.network.PacketDecoder;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.network.PacketListener;
import net.minecraft.network.protocol.game.ClientboundLevelChunkWithLightPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.List;

@Mixin(PacketDecoder.class)
public class PacketDecoderMixin<T extends PacketListener> {
    /** If a chunk packet still has unread bytes after decoding, throw them away instead of erroring out. */
    @Inject(
        method = "decode",
        at = @At(value = "INVOKE", target = "Lio/netty/buffer/ByteBuf;readableBytes()I", ordinal = 2),
        locals = LocalCapture.CAPTURE_FAILSOFT
    )
    private void salvageAbnormalChunk(ChannelHandlerContext ctx, ByteBuf in, List<Object> out, CallbackInfo ci,
                                      int start, Packet<? super T> packet, PacketType<? extends Packet<? super T>> type) {
        if (!Saver.chunkban) return;
        if (packet instanceof ClientboundLevelChunkWithLightPacket && in.readableBytes() > 0) {
            int remaining = in.readableBytes();
            in.skipBytes(remaining);
        }
    }
}
