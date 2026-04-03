package net.replaceitem.integratedcircuit.mixin.client;

import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.replaceitem.integratedcircuit.IntegratedCircuitBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPacketListener.class)
public abstract class ClientPlayNetworkHandlerMixin {
    @Inject(method = "lambda$handleBlockEntityData$0", at = @At("TAIL"))
    private void onBlockEntityUpdate(ClientboundBlockEntityDataPacket packet, BlockEntity blockEntity, CallbackInfo ci) {
        if(blockEntity instanceof IntegratedCircuitBlockEntity && blockEntity.getLevel() != null) {
            BlockPos pos = blockEntity.getBlockPos();
            BlockState state = blockEntity.getLevel().getBlockState(pos);
            blockEntity.getLevel().sendBlockUpdated(pos, state, state, Block.UPDATE_CLIENTS);
        }
    }
}
