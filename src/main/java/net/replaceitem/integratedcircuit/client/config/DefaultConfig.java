package net.replaceitem.integratedcircuit.client.config;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.InputUtil;
import net.replaceitem.integratedcircuit.mixin.KeyBindingAccessor;
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
        return ((KeyBindingAccessor) MinecraftClient.getInstance().options.useKey).getBoundKey();
    }
    public InputUtil.Key getDestroyKeybind() {
        return ((KeyBindingAccessor) MinecraftClient.getInstance().options.attackKey).getBoundKey();
    }
    public InputUtil.Key getPickKeybind() {
        return ((KeyBindingAccessor) MinecraftClient.getInstance().options.pickItemKey).getBoundKey();
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
