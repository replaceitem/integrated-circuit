package net.replaceitem.integratedcircuit.client.config;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.GameOptions;
import net.minecraft.client.util.InputUtil;
import net.replaceitem.integratedcircuit.mixin.KeyBindingAccessor;
import org.jetbrains.annotations.Nullable;
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
        GameOptions options = getGameOptions();
        return options == null ? InputUtil.Type.MOUSE.createFromCode(GLFW.GLFW_MOUSE_BUTTON_RIGHT) : ((KeyBindingAccessor) options.useKey).getBoundKey();
    }
    public InputUtil.Key getDestroyKeybind() {
        GameOptions options = getGameOptions();
        return options == null ? InputUtil.Type.MOUSE.createFromCode(GLFW.GLFW_MOUSE_BUTTON_LEFT) : ((KeyBindingAccessor) options.attackKey).getBoundKey();
    }
    public InputUtil.Key getPickKeybind() {
        GameOptions options = getGameOptions();
        return options == null ? InputUtil.Type.MOUSE.createFromCode(GLFW.GLFW_MOUSE_BUTTON_MIDDLE) : ((KeyBindingAccessor) options.pickItemKey).getBoundKey();
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
    public boolean getRenderCircuitName() {
        return true;
    };

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
    
    
    @Nullable
    public static GameOptions getGameOptions() {
        MinecraftClient client = MinecraftClient.getInstance();
        if(client == null) return null;
        return client.options;
    }
}
