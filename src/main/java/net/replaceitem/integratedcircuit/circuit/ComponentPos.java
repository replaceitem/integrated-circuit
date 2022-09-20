package net.replaceitem.integratedcircuit.circuit;

import net.minecraft.util.math.Vec3i;
import net.replaceitem.integratedcircuit.util.Direction;

public class ComponentPos extends Vec3i { // using vec3, but only 2d required

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

    public ComponentPos offset(Direction direction, int amount) {
        return this.add(direction.getOffset().multiply(amount));
    }

    public ComponentPos offset(Direction direction) {
        return this.add(direction.getOffset());
    }
    
    public ComponentPos north() {
        return this.offset(Direction.NORTH);
    }
    public ComponentPos east() {
        return this.offset(Direction.EAST);
    }
    public ComponentPos south() {
        return this.offset(Direction.SOUTH);
    }
    public ComponentPos west() {
        return this.offset(Direction.WEST);
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof ComponentPos componentPos && this.getX() == componentPos.getX() && this.getY() == componentPos.getY();
    }
}
