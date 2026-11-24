package meitweaks.mixin;

import io.netty.channel.ChannelHandlerContext;
import meitweaks.signeditor.SignEditor;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundOpenSignEditorPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerInputPacket;
import net.minecraft.network.protocol.game.ServerboundSignUpdatePacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Input;
import net.minecraft.world.level.block.CeilingHangingSignBlock;
import net.minecraft.world.level.block.WallHangingSignBlock;
import net.minecraft.world.level.block.entity.SignTextSlot;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Mixin(Connection.class)
public class ConnectionMixin {
    @Unique private final Random rand = new Random();
    @Unique private BlockPos editRearPos = null;
    @Unique private List<String> pendingRearLines = null;

    /** Auto Sign: answers "open sign editor" packets with a generated sign update (front, then optionally back). */
    @Inject(method = "channelRead0*", at = @At("HEAD"), cancellable = true)
    private void onPacketReceive(ChannelHandlerContext ctx, Packet<?> packet, CallbackInfo ci) {
        if (!SignEditor.autoSign) return;
        if (SignEditor.mc.player == null) return;

        if (packet instanceof ClientboundOpenSignEditorPacket(BlockPos pos, SignTextSlot slot)) {
            ci.cancel();

            boolean front = editRearPos == null || !editRearPos.equals(pos);
            if (front) {
                editRearPos = null;
                pendingRearLines = null;

                List<String> lines = createSignLines();
                SignEditor.mc.player.connection.send(new ServerboundSignUpdatePacket(pos, lines, SignTextSlot.FRONT));

                if (SignEditor.BackMode) {
                    editRearPos = pos;
                    pendingRearLines = lines;
                    SignEditor.scheduleNextTick(() -> reopenForBack(pos));
                }
            } else {
                List<String> lines = pendingRearLines != null ? pendingRearLines : createSignLines();
                SignEditor.mc.player.connection.send(new ServerboundSignUpdatePacket(pos, lines, SignTextSlot.BACK));
                editRearPos = null;
                pendingRearLines = null;
            }
        }
    }

    @Unique
    private List<String> createSignLines() {
        List<String> lines = new ArrayList<>();

        if (SignEditor.CustomMode) {
            lines.add(SignEditor.customText1);
            lines.add(SignEditor.customText2);
            lines.add(SignEditor.customText3);
            lines.add(SignEditor.customText4);
        } else {
            for (int i = 0; i < 4; i++) {
                StringBuilder line = new StringBuilder();
                for (int j = 0; j < SignEditor.signLength; j++) {
                    if (SignEditor.MAXMODE) {
                        line.appendCodePoint(65537);
                    } else {
                        char c = (char) (rand.nextInt(55263) + 32);
                        line.append(c);
                    }
                }
                lines.add(line.toString());
            }
        }

        return lines;
    }

    @Unique
    private void reopenForBack(BlockPos pos) {
        Minecraft mc = SignEditor.mc;
        if (mc.player == null || mc.gameMode == null || mc.level == null) {
            editRearPos = null;
            pendingRearLines = null;
            return;
        }
        if (!pos.equals(editRearPos)) return;

        BlockState state = mc.level.getBlockState(pos);
        Direction face = Direction.DOWN;
        if (state.getBlock() instanceof WallHangingSignBlock) {
            face = state.getValue(WallHangingSignBlock.FACING).getOpposite();
        } else if (state.getBlock() instanceof CeilingHangingSignBlock) {
            int rotation = state.getValue(CeilingHangingSignBlock.ROTATION);
            face = Direction.fromYRot((double) rotation).getOpposite();
        }

        BlockHitResult hit = new BlockHitResult(new Vec3(pos.getX(), pos.getY(), pos.getZ()), face, pos, false);

        // Right-click the sign without sneaking so the editor opens for the back side.
        boolean sneaking = mc.player.isShiftKeyDown();
        mc.player.setShiftKeyDown(false);
        Input noShift = new Input(
            mc.player.input.keyPresses.forward(),
            mc.player.input.keyPresses.backward(),
            mc.player.input.keyPresses.left(),
            mc.player.input.keyPresses.right(),
            mc.player.input.keyPresses.jump(),
            false,
            mc.player.input.keyPresses.sprint()
        );
        mc.player.connection.send(new ServerboundPlayerInputPacket(noShift));

        mc.gameMode.useItemOn(mc.player, InteractionHand.MAIN_HAND, hit);

        mc.player.setShiftKeyDown(sneaking);
        Input restored = new Input(
            mc.player.input.keyPresses.forward(),
            mc.player.input.keyPresses.backward(),
            mc.player.input.keyPresses.left(),
            mc.player.input.keyPresses.right(),
            mc.player.input.keyPresses.jump(),
            sneaking,
            mc.player.input.keyPresses.sprint()
        );
        mc.player.connection.send(new ServerboundPlayerInputPacket(restored));
    }
}
