package se.gory_moon.tallgates.blocks;

import net.minecraft.block.Block;
import net.minecraft.block.BlockPlanks;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.block.statemap.StateMap;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.registries.IForgeRegistry;
import se.gory_moon.tallgates.TallGatesMod;

import javax.annotation.Nonnull;
import java.lang.reflect.Field;
import java.util.*;

import static se.gory_moon.tallgates.lib.ModInfo.MODID;

@Mod.EventBusSubscriber
public class BlockRegistry {

    public static final Block OAK_TALL_GATE = new BlockTallGate(BlockPlanks.EnumType.OAK);
    public static final Block SPRUCE_TALL_GATE = new BlockTallGate(BlockPlanks.EnumType.SPRUCE);
    public static final Block BIRCH_TALL_GATE = new BlockTallGate(BlockPlanks.EnumType.BIRCH);
    public static final Block JUNGLE_TALL_GATE = new BlockTallGate(BlockPlanks.EnumType.JUNGLE);
    public static final Block DARK_OAK_TALL_GATE = new BlockTallGate(BlockPlanks.EnumType.DARK_OAK);
    public static final Block ACACIA_TALL_GATE = new BlockTallGate(BlockPlanks.EnumType.ACACIA);

    public static final Set<Block> BLOCKS = new LinkedHashSet<>();
    public static final List<ItemBlock> ITEM_BLOCKS = new ArrayList<ItemBlock>();

    public static void preInit() {
        try {
            for (Field field : BlockRegistry.class.getDeclaredFields()) {
                Object obj = field.get(null);
                if (obj instanceof Block) {
                    Block block = (Block) obj;
                    String name = field.getName().toLowerCase(Locale.ENGLISH);
                    registerBlock(name, block);
                }
            }
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public static void registerBlock(String name, Block block) {
        BLOCKS.add(block);
        block.setRegistryName(MODID, name).setUnlocalizedName(MODID + "." + name);

        /*ItemBlock item;
        if (block instanceof ICustomItemBlock)
            item = ((ICustomItemBlock) block).getItemBlock();
        else
            item = new ItemBlock(block);
        ITEM_BLOCKS.add(item);
        item.setRegistryName(MODID, name).setUnlocalizedName(MODID + "." + name);*/
    }

    @SubscribeEvent
    public static void registerBlock(RegistryEvent.Register<Block> event) {
        final IForgeRegistry<Block> registry = event.getRegistry();
        for (Block block : BLOCKS) {
            registry.register(block);
        }
    }

    @SideOnly(Side.CLIENT)
    @SubscribeEvent
    public static void registerBlockRenderers(ModelRegistryEvent event) {
        for (Block block : BlockRegistry.BLOCKS) {
            if (block instanceof IStateMappedBlock) {
                StateMap.Builder builder = new StateMap.Builder();

                ((IStateMappedBlock) block).setStateMapper(builder);
                ModelLoader.setCustomStateMapper(block, builder.build());
            }
            if (block instanceof ICustomItemBlock) {
                ICustomItemBlock customItemBlock = (ICustomItemBlock) block;
                ItemStack renderedItem = customItemBlock.getRenderedItem();
                if (!renderedItem.isEmpty()) {
                    Map<Integer, ResourceLocation> map = TallGatesMod.proxy.getItemModelMap(renderedItem.getItem());
                    ModelResourceLocation model = (ModelResourceLocation) map.get(renderedItem.getMetadata());
                    ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(block), 0, model);
                    continue;
                }
            }
            ResourceLocation name = block.getRegistryName();
            ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(block), 0, new ModelResourceLocation(name, "inventory"));
        }
    }

    public interface ICustomItemBlock {
        /**
         * @return Returns a custom item for this block.
         */
        @Nonnull
        default ItemBlock getItemBlock() {
            if (Item.getItemFromBlock((Block) this) != Items.AIR)
                return (ItemBlock) Item.getItemFromBlock((Block) this);
            else
                return new ItemBlock((Block)this);
        }

        /**
         * @return Returns which item this block should be rendered as
         */
        @SideOnly(Side.CLIENT)
        default ItemStack getRenderedItem() {
            return ItemStack.EMPTY;
        }
    }

    public interface IStateMappedBlock {
        /**
         * Sets the statemap
         *
         * @param builder The statemap build to use
         */
        @SideOnly(Side.CLIENT)
        void setStateMapper(StateMap.Builder builder);
    }
}
