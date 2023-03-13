package net.replaceitem.integratedcircuit.util;

import net.minecraft.util.math.Vec3i;

public enum FlatDirection {

    NORTH(0, 0, -1, Axis.Y, net.minecraft.util.math.Direction.NORTH),
    EAST(1, 1, 0, Axis.X, net.minecraft.util.math.Direction.EAST),
    SOUTH(2, 0, 1, Axis.Y, net.minecraft.util.math.Direction.SOUTH),
    WEST(3, -1, 0, Axis.X, net.minecraft.util.math.Direction.WEST);

    private final int index;
    private final Vec3i offset;
    private final Axis axis;
    private final net.minecraft.util.math.Direction vanillaDirection;

    public static final FlatDirection[] VALUES = FlatDirection.values();

    FlatDirection(int index, int dx, int dy, Axis axis, net.minecraft.util.math.Direction vanillaDirection) {
        this.index = index;
        this.offset = new Vec3i(dx, dy, 0);
        this.axis = axis;
        this.vanillaDirection = vanillaDirection;
    }

    public int toInt() {
        return this.index;
    }

    public Vec3i getOffset() {
        return offset;
    }

    public FlatDirection getOpposite() {
        return this.rotated(2);
    }
    
    public Axis getAxis() {
        return axis;
    }

    public net.minecraft.util.math.Direction getVanillaDirection() {
        return vanillaDirection;
    }

    public FlatDirection rotated(int times) {
        return VALUES[Math.floorMod(this.toInt()+times,4)];
    }
    
    public static FlatDirection fromVanillaDirection(net.minecraft.util.math.Direction direction) {
        return switch (direction) {
            case DOWN, UP -> null;
            case NORTH -> NORTH;
            case SOUTH -> SOUTH;
            case WEST -> WEST;
            case EAST -> EAST;
        };
    }
    
    public enum Axis {
        X, Y
    }
}
