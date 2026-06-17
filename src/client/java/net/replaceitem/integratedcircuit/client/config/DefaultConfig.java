package net.replaceitem.integratedcircuit.client.config;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.replaceitem.integratedcircuit.client.mixin.KeyBindingAccessor;
import org.jspecify.annotations.Nullable;
import org.lwjgl.glfw.GLFW;


public class DefaultConfig {
    
    public static boolean clothConfigEnabled = false;
    @Nullable
    private static DefaultConfig config;
    
    public static void initialize() {
        clothConfigEnabled = FabricLoader.getInstance().isModLoaded("cloth-config2");
        if(clothConfigEnabled) {
            config = ClothConfigCompat.createConfig();
        } else {
            config = new DefaultConfig();
        }
    }

    public static DefaultConfig getConfig() {
        if(config == null) throw new IllegalStateException("Config not initialized");
        return config;
    }

    public InputConstants.Key getPlaceKeybind() {
        Options options = getGameOptions();
        return options == null ? InputConstants.Type.MOUSE.getOrCreate(GLFW.GLFW_MOUSE_BUTTON_RIGHT) : ((KeyBindingAccessor) options.keyUse).getKey();
    }
    public InputConstants.Key getDestroyKeybind() {
        Options options = getGameOptions();
        return options == null ? InputConstants.Type.MOUSE.getOrCreate(GLFW.GLFW_MOUSE_BUTTON_LEFT) : ((KeyBindingAccessor) options.keyAttack).getKey();
    }
    public InputConstants.Key getPickKeybind() {
        Options options = getGameOptions();
        return options == null ? InputConstants.Type.MOUSE.getOrCreate(GLFW.GLFW_MOUSE_BUTTON_MIDDLE) : ((KeyBindingAccessor) options.keyPickItem).getKey();
    }
    public InputConstants.Key getRotateKeybind() {
        return InputConstants.Type.KEYSYM.getOrCreate(GLFW.GLFW_KEY_R);
    }
    public ScrollBehaviour getScrollBehaviour() {
        return ScrollBehaviour.ROTATE;
    }
    public boolean getInvertScrollDirection() {
        return false;
    }
    public boolean getRenderCircuitName() {
        return true;
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

    @Nullable
    public static Options getGameOptions() {
        Minecraft client = Minecraft.getInstance();
        if(client == null) return null;
        return client.options;
    }
}
