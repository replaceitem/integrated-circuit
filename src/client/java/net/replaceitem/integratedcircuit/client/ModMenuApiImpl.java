package net.replaceitem.integratedcircuit.client;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import net.replaceitem.integratedcircuit.client.config.ClothConfigCompat;
import net.replaceitem.integratedcircuit.client.config.DefaultConfig;

public class ModMenuApiImpl implements ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return parent -> DefaultConfig.clothConfigEnabled ? ClothConfigCompat.getConfigScreenFavtory(parent) : null;
    }
}
