package net.replaceitem.integratedcircuit.mixin.client;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.util.math.BlockPos;
import net.replaceitem.integratedcircuit.IntegratedCircuitBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayNetworkHandler.class)
public abstract class ClientPlayNetworkHandlerMixin {
    @Inject(method = "method_38542(Lnet/minecraft/network/packet/s2c/play/BlockEntityUpdateS2CPacket;Lnet/minecraft/block/entity/BlockEntity;)V", at = @At("TAIL"))
    private void onBlockEntityUpdate(BlockEntityUpdateS2CPacket blockEntityUpdateS2CPacket, BlockEntity blockEntity, CallbackInfo ci) {
        if(blockEntity instanceof IntegratedCircuitBlockEntity && blockEntity.getWorld() != null) {
            BlockPos pos = blockEntity.getPos();
            BlockState state = blockEntity.getWorld().getBlockState(pos);
            blockEntity.getWorld().updateListeners(pos, state, state, Block.NOTIFY_LISTENERS);
        }
    }
}
