package se.gory_moon.tallgates.proxy;

import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.ModelLoader;
import se.gory_moon.tallgates.items.ItemRegistry;

import java.util.HashMap;
import java.util.Map;

public class ClientProxy extends CommonProxy {

    @Override
    public Map<Integer, ResourceLocation> getItemModelMap(Item item) {
        Map<Integer, ResourceLocation> map = new HashMap<>();
        if (item instanceof ItemRegistry.IMultipleItemModelDefinition) {
            for (Map.Entry<Integer, ResourceLocation> model : ((ItemRegistry.IMultipleItemModelDefinition) item).getModels().entrySet()) {
                map.put(model.getKey(), new ModelResourceLocation(model.getValue(), "inventory"));
            }
        } else
            map.put(0, new ModelResourceLocation(item.getRegistryName().toString(), "inventory"));
        return map;
    }

    @Override
    public void registerDefaultItemRenderer(Item item) {
        Map<Integer, ResourceLocation> map = this.getItemModelMap(item);
        for(Map.Entry<Integer, ResourceLocation> entry : map.entrySet()) {
            ModelLoader.setCustomModelResourceLocation(item, entry.getKey(), (ModelResourceLocation) entry.getValue());
        }
    }
}
