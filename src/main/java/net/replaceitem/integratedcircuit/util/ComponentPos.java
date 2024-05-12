package net.replaceitem.integratedcircuit.util;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.util.math.Vec3i;

public class ComponentPos extends Vec3i { // using vec3, but only 2d required
    
    public static final MapCodec<ComponentPos> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.INT.fieldOf("x").forGetter(ComponentPos::getX),
            Codec.INT.fieldOf("y").forGetter(ComponentPos::getY)
    ).apply(instance, ComponentPos::new));
    
    public static final PacketCodec<ByteBuf,ComponentPos> PACKET_CODEC = new PacketCodec<>() {
        public ComponentPos decode(ByteBuf byteBuf) {
            int x = byteBuf.readShort();
            int y = byteBuf.readShort();
            return new ComponentPos(x, y);
        }

        public void encode(ByteBuf byteBuf, ComponentPos pos) {
            byteBuf.writeShort(pos.getX());
            byteBuf.writeShort(pos.getY());
        }
    };

    public ComponentPos(int x, int y) {
        super(x, y, 0);
    }

    public ComponentPos add(int x, int y) {
        return new ComponentPos(getX()+x, getY()+y);
    }

    public ComponentPos add(Vec3i vec3i) {
        return this.add(vec3i.getX(), vec3i.getY());
    }

    public ComponentPos multiply(int amount) {
        return new ComponentPos(getX()*amount, getY()*amount);
    }

    public ComponentPos offset(FlatDirection direction, int amount) {
        return this.add(direction.getOffset().multiply(amount));
    }

    public ComponentPos offset(FlatDirection direction) {
        return this.add(direction.getOffset());
    }
    
    public ComponentPos north() {
        return this.offset(FlatDirection.NORTH);
    }
    public ComponentPos east() {
        return this.offset(FlatDirection.EAST);
    }
    public ComponentPos south() {
        return this.offset(FlatDirection.SOUTH);
    }
    public ComponentPos west() {
        return this.offset(FlatDirection.WEST);
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof ComponentPos componentPos && this.getX() == componentPos.getX() && this.getY() == componentPos.getY();
    }
}
