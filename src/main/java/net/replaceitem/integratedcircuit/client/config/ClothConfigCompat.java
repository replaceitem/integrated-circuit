package net.replaceitem.integratedcircuit.client.config;

import com.google.gson.GsonBuilder;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.ConfigHolder;
import me.shedaniel.autoconfig.gui.registry.GuiRegistry;
import me.shedaniel.autoconfig.serializer.GsonConfigSerializer;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;

import java.util.Collections;

import static me.shedaniel.autoconfig.util.Utils.getUnsafely;
import static me.shedaniel.autoconfig.util.Utils.setUnsafely;

public class ClothConfigCompat {

    public static DefaultConfig createConfig() {
        registerKeyCodeProvider(AutoConfig.getGuiRegistry(ClothConfig.class));
        AutoConfig.register(ClothConfig.class, (definition, configClass) ->
                new GsonConfigSerializer<>(definition, configClass, new GsonBuilder().setPrettyPrinting().registerTypeAdapter(InputUtil.Key.class, new KeySerializer()).create())
        );
        ConfigHolder<ClothConfig> configHolder = AutoConfig.getConfigHolder(ClothConfig.class);
        return configHolder.getConfig();
    }


    private static final ConfigEntryBuilder ENTRY_BUILDER = ConfigEntryBuilder.create();

    public static void registerKeyCodeProvider(GuiRegistry guiRegistry) {
        guiRegistry.registerTypeProvider((i18n, field, config, defaults, guiProvider) -> Collections.singletonList(
                        ENTRY_BUILDER.startKeyCodeField(
                                        Text.translatable(i18n),
                                        getUnsafely(field, config, InputUtil.UNKNOWN_KEY)
                                )
                                .setDefaultValue(() -> getUnsafely(field, defaults))
                                .setKeySaveConsumer(newValue -> setUnsafely(field, config, newValue))
                                .build()
                ),
                InputUtil.Key.class
        );
    }

}
