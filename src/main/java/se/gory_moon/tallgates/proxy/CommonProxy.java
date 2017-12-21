package se.gory_moon.tallgates.proxy;

import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;

import java.util.Collections;
import java.util.Map;

public class CommonProxy {

    public Map<Integer, ResourceLocation> getItemModelMap(Item item) {
        return Collections.emptyMap();
    }

    public void registerDefaultItemRenderer(Item item) {

    }
}
