package net.replaceitem.integratedcircuit.client.config;

import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry;
import net.minecraft.client.util.InputUtil;

@Config(name = "integrated_circuit")
public class ClothConfig extends DefaultConfig implements ConfigData {
    public InputUtil.Key place_component = super.getPlaceKeybind();
    @Override
    public InputUtil.Key getPlaceKeybind() {
        return place_component;
    }

    public InputUtil.Key destroy_component = super.getDestroyKeybind();
    @Override
    public InputUtil.Key getDestroyKeybind() {
        return destroy_component;
    }

    public InputUtil.Key pick_component = super.getPickKeybind();
    @Override
    public InputUtil.Key getPickKeybind() {
        return pick_component;
    }

    public InputUtil.Key rotate_component = super.getRotateKeybind();
    @Override
    public InputUtil.Key getRotateKeybind() {
        return rotate_component;
    }
    
    @ConfigEntry.Gui.EnumHandler(option = ConfigEntry.Gui.EnumHandler.EnumDisplayOption.BUTTON)
    public ScrollBehaviour scroll_behaviour = super.getScrollBehaviour();
    @Override
    public ScrollBehaviour getScrollBehaviour() {
        return scroll_behaviour;
    }
    
    public boolean invert_scroll_direction = super.getInvertScrollDirection();
    @Override
    public boolean getInvertScrollDirection() {
        return invert_scroll_direction;
    }

    public boolean render_circuit_name = super.getRenderCircuitName();
    @Override
    public boolean getRenderCircuitName() {
        return render_circuit_name;
    }
}
