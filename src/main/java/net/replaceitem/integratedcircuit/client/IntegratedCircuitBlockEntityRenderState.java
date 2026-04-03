package net.replaceitem.integratedcircuit.client;

import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.network.chat.Component;
import org.joml.Quaternionf;
import org.jspecify.annotations.Nullable;

public class IntegratedCircuitBlockEntityRenderState extends BlockEntityRenderState {
    @Nullable
    public Component customName;
    @Nullable
    public Quaternionf orientation;
}
