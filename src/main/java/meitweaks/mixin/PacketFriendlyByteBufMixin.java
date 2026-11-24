package meitweaks.mixin;

import io.netty.buffer.ByteBuf;
import meitweaks.antichunkban.ChunkPacketState;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.VarInt;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(FriendlyByteBuf.class)
public abstract class PacketFriendlyByteBufMixin {
    @Shadow
    public abstract Tag readNbt(NbtAccounter accounter);

    @Inject(method = "readNbt()Lnet/minecraft/nbt/CompoundTag;", at = @At("HEAD"), cancellable = true)
    private void onReadNbt(CallbackInfoReturnable<CompoundTag> cir) {
        Tag tag;
        try {
            tag = this.readNbt(NbtAccounter.create(2000000L));
        } catch (Exception e) {
            cir.setReturnValue(new CompoundTag());
            return;
        }
        cir.setReturnValue(tag instanceof CompoundTag compound ? compound : new CompoundTag());
    }

    @Inject(method = "readNbt(Lio/netty/buffer/ByteBuf;)Lnet/minecraft/nbt/CompoundTag;", at = @At("HEAD"), cancellable = true)
    private static void safeReadNbt(ByteBuf buf, CallbackInfoReturnable<CompoundTag> cir) {
        try {
            Tag tag = FriendlyByteBuf.readNbt(buf, NbtAccounter.defaultQuota());
            cir.setReturnValue(tag == null ? null : tag instanceof CompoundTag compound ? compound : new CompoundTag());
        } catch (RuntimeException e) {
            ChunkPacketState.markBadBlockEntityData();
            buf.setZero(buf.readerIndex(), buf.readableBytes());
            cir.setReturnValue(new CompoundTag());
        }
    }

    @Redirect(
        method = "readLongArray(Lio/netty/buffer/ByteBuf;)[J",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/network/VarInt;read(Lio/netty/buffer/ByteBuf;)I")
    )
    private static int handleHugeLongArray(ByteBuf buf) {
        int length;
        try {
            length = VarInt.read(buf);
        } catch (RuntimeException e) {
            ChunkPacketState.markBadLightData();
            buf.setZero(buf.readerIndex(), buf.readableBytes());
            return 0;
        }

        int max = buf.readableBytes() / 8;
        if (length < 0 || length > max) {
            ChunkPacketState.markBadLightData();
            buf.setZero(buf.readerIndex(), buf.readableBytes());
            return 0;
        }
        return length;
    }
}
