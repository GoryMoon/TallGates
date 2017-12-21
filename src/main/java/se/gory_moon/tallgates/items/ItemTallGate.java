package se.gory_moon.tallgates.items;

import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import se.gory_moon.tallgates.blocks.BlockTallGate;

import java.util.HashMap;
import java.util.Map;

import static se.gory_moon.tallgates.lib.ModInfo.MODID;
import static se.gory_moon.tallgates.blocks.BlockRegistry.*;

public class ItemTallGate extends Item implements ItemRegistry.IMultipleItemModelDefinition {

    private static Block[] GATE_BLOCKS = new Block[]{OAK_TALL_GATE, SPRUCE_TALL_GATE, BIRCH_TALL_GATE, JUNGLE_TALL_GATE, DARK_OAK_TALL_GATE, ACACIA_TALL_GATE};

    public ItemTallGate() {
        this.setCreativeTab(CreativeTabs.REDSTONE);
        setHasSubtypes(true);
    }

    @Override
    public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> items) {
        if (this.isInCreativeTab(tab)) {
            for (int i = 0; i < GATE_BLOCKS.length; i++)
                items.add(new ItemStack(this, 1, i));
        }
    }

    @Override
    public String getItemStackDisplayName(ItemStack stack) {
        if (stack.getMetadata() >= GATE_BLOCKS.length)
            return TextFormatting.RED + "" + TextFormatting.BOLD + "BAD METADATA";
        return super.getItemStackDisplayName(stack);
    }

    @Override
    public String getUnlocalizedName(ItemStack stack) {
        if (stack.getMetadata() >= GATE_BLOCKS.length)
            return TextFormatting.RED + "" + TextFormatting.BOLD + "BAD METADATA";
        return "item." + GATE_BLOCKS[stack.getMetadata()].getRegistryName().getResourcePath();
    }

    @Override
    public Map<Integer, ResourceLocation> getModels() {
        Map<Integer, ResourceLocation> models = new HashMap<>();
        for(int i = 0; i < GATE_BLOCKS.length; i++) {
            models.put(i, new ResourceLocation(MODID, GATE_BLOCKS[i].getRegistryName().getResourcePath()));
        }
        return models;
    }

    public static Block getBlockFromMeta(int meta) {
        if (meta >= 0 && meta < GATE_BLOCKS.length)
            return GATE_BLOCKS[meta];
        else
            return GATE_BLOCKS[0];
    }

    public static int getMetaFromBlock(Block block) {
        for (int i = 0; i < GATE_BLOCKS.length; i++)
            if (block == GATE_BLOCKS[i])
                return i;
        return 0;
    }

    @Override
    public EnumActionResult onItemUse(EntityPlayer player, World worldIn, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        if (facing != EnumFacing.UP) {
            return EnumActionResult.FAIL;
        } else {
            IBlockState blockState = worldIn.getBlockState(pos);
            Block block = blockState.getBlock();

            if (!block.isReplaceable(worldIn, pos)) {
                pos = pos.offset(facing);
            }

            Block gateBlock = getBlockFromMeta(player.getHeldItem(hand).getMetadata());
            if (player.canPlayerEdit(pos, facing, player.getHeldItem(hand)) && gateBlock.canPlaceBlockAt(worldIn, pos)) {
                placeGate(worldIn, pos, player.getHorizontalFacing(), gateBlock);
                SoundType soundtype = gateBlock.getSoundType();
                worldIn.playSound(player, pos, soundtype.getPlaceSound(), SoundCategory.BLOCKS, (soundtype.getVolume() + 1.0F) / 2.0F, soundtype.getPitch() * 0.8F);
                player.getHeldItem(hand).shrink(1);
                return EnumActionResult.SUCCESS;
            } else {
                return EnumActionResult.FAIL;
            }
        }
    }

    public static void placeGate(World worldIn, BlockPos pos, EnumFacing facing, Block door) {
        BlockPos blockpos = pos.up();
        boolean flag = worldIn.isBlockPowered(pos) || worldIn.isBlockPowered(blockpos);
        IBlockState iblockstate = door.getDefaultState().withProperty(BlockTallGate.FACING, facing).withProperty(BlockTallGate.POWERED, flag).withProperty(BlockTallGate.OPEN, flag);
        worldIn.setBlockState(pos, iblockstate.withProperty(BlockTallGate.HALF, BlockTallGate.EnumGateHalf.LOWER), 2);
        worldIn.setBlockState(blockpos, iblockstate.withProperty(BlockTallGate.HALF, BlockTallGate.EnumGateHalf.UPPER), 2);
        worldIn.notifyNeighborsOfStateChange(pos, door, false);
        worldIn.notifyNeighborsOfStateChange(blockpos, door, false);
    }
}
