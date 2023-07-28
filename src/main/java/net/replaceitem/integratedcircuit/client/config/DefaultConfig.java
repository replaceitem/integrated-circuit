package net.replaceitem.integratedcircuit.client.config;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;


public class DefaultConfig {
    
    public static boolean clothConfigEnabled = false;
    public static DefaultConfig config;
    
    public static void initialize() {
        clothConfigEnabled = FabricLoader.getInstance().isModLoaded("cloth-config2");
        if(clothConfigEnabled) {
            config = ClothConfigCompat.createConfig();
        } else {
            config = new DefaultConfig();
        }
    }

    public InputUtil.Key getPlaceKeybind() {
        return InputUtil.Type.MOUSE.createFromCode(GLFW.GLFW_MOUSE_BUTTON_RIGHT);
    }
    public InputUtil.Key getDestroyKeybind() {
        return InputUtil.Type.MOUSE.createFromCode(GLFW.GLFW_MOUSE_BUTTON_LEFT);
    }
    public InputUtil.Key getPickKeybind() {
        return InputUtil.Type.MOUSE.createFromCode(GLFW.GLFW_MOUSE_BUTTON_MIDDLE);
    }
    public InputUtil.Key getRotateKeybind() {
        return InputUtil.Type.KEYSYM.createFromCode(GLFW.GLFW_KEY_R);
    }
    public ScrollBehaviour getScrollBehaviour() {
        return ScrollBehaviour.ROTATE;
    }
    public boolean getInvertScrollDirection() {
        return false;
    }
    
    public enum ScrollBehaviour {
        ROTATE("Rotate"),
        SELECT_COMPONENT("Select Component");
        
        private final String prettyName;

        ScrollBehaviour(String prettyName) {
            this.prettyName = prettyName;
        }

        @Override
        public String toString() {
            return prettyName;
        }
    }
}
