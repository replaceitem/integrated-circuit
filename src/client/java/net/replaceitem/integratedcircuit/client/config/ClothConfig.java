package net.replaceitem.integratedcircuit.client.config;

import com.mojang.blaze3d.platform.InputConstants;
import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry;

@Config(name = "integrated_circuit")
public class ClothConfig extends DefaultConfig implements ConfigData {
    public InputConstants.Key place_component = super.getPlaceKeybind();
    @Override
    public InputConstants.Key getPlaceKeybind() {
        return place_component;
    }

    public InputConstants.Key destroy_component = super.getDestroyKeybind();
    @Override
    public InputConstants.Key getDestroyKeybind() {
        return destroy_component;
    }

    public InputConstants.Key pick_component = super.getPickKeybind();
    @Override
    public InputConstants.Key getPickKeybind() {
        return pick_component;
    }

    public InputConstants.Key rotate_component = super.getRotateKeybind();
    @Override
    public InputConstants.Key getRotateKeybind() {
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
