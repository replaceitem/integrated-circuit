package net.replaceitem.integratedcircuit.client.config;

import com.google.gson.GsonBuilder;
import com.mojang.blaze3d.platform.InputConstants;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.AutoConfigClient;
import me.shedaniel.autoconfig.ConfigHolder;
import me.shedaniel.autoconfig.gui.registry.GuiRegistry;
import me.shedaniel.autoconfig.serializer.GsonConfigSerializer;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import java.util.Collections;

import static me.shedaniel.autoconfig.util.Utils.getUnsafely;
import static me.shedaniel.autoconfig.util.Utils.setUnsafely;

public class ClothConfigCompat {

    public static DefaultConfig createConfig() {
        registerKeyCodeProvider(AutoConfigClient.getGuiRegistry(ClothConfig.class));
        AutoConfig.register(ClothConfig.class, (definition, configClass) ->
                new GsonConfigSerializer<>(definition, configClass, new GsonBuilder().setPrettyPrinting().registerTypeAdapter(InputConstants.Key.class, new KeySerializer()).create())
        );
        ConfigHolder<ClothConfig> configHolder = AutoConfig.getConfigHolder(ClothConfig.class);
        return configHolder.getConfig();
    }


    private static final ConfigEntryBuilder ENTRY_BUILDER = ConfigEntryBuilder.create();

    public static void registerKeyCodeProvider(GuiRegistry guiRegistry) {
        guiRegistry.registerTypeProvider((i18n, field, config, defaults, guiProvider) -> Collections.singletonList(
                        ENTRY_BUILDER.startKeyCodeField(
                                        Component.translatable(i18n),
                                        getUnsafely(field, config, InputConstants.UNKNOWN)
                                )
                                .setDefaultValue(() -> getUnsafely(field, defaults))
                                .setKeySaveConsumer(newValue -> setUnsafely(field, config, newValue))
                                .build()
                ),
                InputConstants.Key.class
        );
    }

    public static Screen getConfigScreenFavtory(Screen parent) {
        return AutoConfigClient.getConfigScreen(ClothConfig.class, parent).get();
    }
}
