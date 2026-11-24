package meitweaks.mixin;

import io.netty.buffer.Unpooled;
import meitweaks.antichunkban.ChunkPacketState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.game.ClientboundLevelChunkPacketData;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.chunk.PalettedContainer;
import net.minecraft.world.level.chunk.Strategy;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.Map;

@Mixin(ClientboundLevelChunkPacketData.class)
public class ClientboundLevelChunkPacketDataMixin {
    @Shadow
    @Final
    @Mutable
    private List<?> blockEntitiesData;

    @Shadow
    @Final
    private byte[] buffer;

    /** Vanilla caps the chunk section buffer at 2 MiB; raise it so oversized (but valid) chunks still decode. */
    @ModifyArg(
        method = "<clinit>",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/network/codec/ByteBufCodecs;byteArray(I)Lnet/minecraft/network/codec/StreamCodec;"),
        index = 0
    )
    private static int increaseChunkDataLimit(int original) {
        return 16777216;
    }

    @Inject(method = "<init>(Ljava/util/Map;[BLjava/util/List;)V", at = @At("RETURN"))
    private void validateChunkBuffer(Map<?, ?> heightmaps, byte[] buffer, List<?> blockEntities, CallbackInfo ci) {
        if (!trySectionParse()) {
            ChunkPacketState.markBadBlockEntityData();
            ChunkPacketState.markBadLightData();
            this.blockEntitiesData = List.of();
        }
    }

    @Unique
    private boolean trySectionParse() {
        ClientLevel level = Minecraft.getInstance().level;
        if (level == null) return false;

        RegistryAccess registries = level.registryAccess();
        int sections = level.getSectionsCount();
        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.wrappedBuffer(this.buffer));

        try {
            Strategy<BlockState> blockStrategy = Strategy.createForBlockStates(Block.BLOCK_STATE_REGISTRY);
            Strategy<Holder<Biome>> biomeStrategy = Strategy.createForBiomes(
                registries.lookupOrThrow(Registries.BIOME).asHolderIdMap()
            );
            Holder.Reference<Biome> plains = registries.lookupOrThrow(Registries.BIOME).getOrThrow(Biomes.PLAINS);

            for (int i = 0; i < sections; i++) {
                PalettedContainer<BlockState> states = new PalettedContainer<>(Blocks.AIR.defaultBlockState(), blockStrategy);
                PalettedContainer<Holder<Biome>> biomes = new PalettedContainer<>(plains, biomeStrategy);
                LevelChunkSection section = new LevelChunkSection(states, biomes);
                section.read(buf);
            }

            return buf.readerIndex() == buf.writerIndex();
        } catch (Throwable t) {
            return false;
        } finally {
            buf.release();
        }
    }
}
