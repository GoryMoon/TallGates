package se.gory_moon.tallgates.blocks;

import net.minecraft.block.Block;
import net.minecraft.block.BlockFenceGate;
import net.minecraft.block.BlockPlanks;
import net.minecraft.block.SoundType;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.statemap.StateMap;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.*;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import se.gory_moon.tallgates.items.ItemRegistry;
import se.gory_moon.tallgates.items.ItemTallGate;

import javax.annotation.Nullable;
import java.util.Random;

public class BlockTallGate extends BlockFenceGate implements BlockRegistry.IStateMappedBlock, BlockRegistry.ICustomItemBlock {

    public static final PropertyEnum<EnumGateHalf> HALF = PropertyEnum.create("half", EnumGateHalf.class);

    public BlockTallGate(BlockPlanks.EnumType type) {
        super(type);
        this.setDefaultState(this.blockState.getBaseState().withProperty(OPEN, Boolean.FALSE).withProperty(POWERED, Boolean.FALSE).withProperty(IN_WALL, Boolean.FALSE).withProperty(HALF, EnumGateHalf.LOWER));
        setCreativeTab(null);
        setHardness(2.0F);
        setResistance(5.0F);
        setSoundType(SoundType.WOOD);
    }

    @Override
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
        state = this.getActualState(state, source, pos);

        return state.getValue(FACING).getAxis() == EnumFacing.Axis.X ? AABB_HITBOX_XAXIS : AABB_HITBOX_ZAXIS;
    }

    @Override
    public IBlockState getActualState(IBlockState state, IBlockAccess worldIn, BlockPos pos) {
        if (state.getValue(HALF) == EnumGateHalf.LOWER) {
            IBlockState iblockstate = worldIn.getBlockState(pos.up());

            if (iblockstate.getBlock() == this) {
                state = state.withProperty(POWERED, iblockstate.getValue(POWERED));
            }
        } else {
            IBlockState iblockstate = worldIn.getBlockState(pos.down());

            if (iblockstate.getBlock() == this) {
                state = state.withProperty(FACING, iblockstate.getValue(FACING)).withProperty(OPEN, iblockstate.getValue(OPEN));
            }
        }

        return state;
    }

    public Item getItemDropped(IBlockState state, Random rand, int fortune) {
        return state.getValue(HALF) == EnumGateHalf.UPPER ? Items.AIR : ItemRegistry.TALL_GATE;
    }

    @Override
    public ItemStack getItem(World worldIn, BlockPos pos, IBlockState state) {
        return state.getValue(HALF) == EnumGateHalf.UPPER ? ItemStack.EMPTY: new ItemStack(ItemRegistry.TALL_GATE, 1, ItemTallGate.getMetaFromBlock(this));
    }

    @Override
    public void getDrops(NonNullList<ItemStack> drops, IBlockAccess world, BlockPos pos, IBlockState state, int fortune) {
        if (state.getValue(HALF) == EnumGateHalf.LOWER)
            drops.add(new ItemStack(ItemRegistry.TALL_GATE, 1, ItemTallGate.getMetaFromBlock(this)));
    }

    @Override
    public ItemStack getPickBlock(IBlockState state, RayTraceResult target, World world, BlockPos pos, EntityPlayer player) {
        return new ItemStack(ItemRegistry.TALL_GATE, 1, ItemTallGate.getMetaFromBlock(this));
    }

    @Override
    public IBlockState withRotation(IBlockState state, Rotation rot) {
        return state.getValue(HALF) != EnumGateHalf.LOWER ? state : state.withProperty(FACING, rot.rotate(state.getValue(FACING)));
    }

    @Override
    public IBlockState withMirror(IBlockState state, Mirror mirrorIn) {
        return mirrorIn == Mirror.NONE ? state : state.withRotation(mirrorIn.toRotation(state.getValue(FACING)));
    }

    @Override
    public boolean canPlaceBlockAt(World worldIn, BlockPos pos) {
        if (pos.getY() >= worldIn.getHeight() - 1) {
            return false;
        } else {
            IBlockState state = worldIn.getBlockState(pos.down());
            return state.getMaterial().isSolid() && worldIn.getBlockState(pos).getBlock().isReplaceable(worldIn, pos) && worldIn.getBlockState(pos).getBlock().isReplaceable(worldIn, pos.up());
        }
    }

    @Nullable
    @Override
    public AxisAlignedBB getCollisionBoundingBox(IBlockState blockState, IBlockAccess worldIn, BlockPos pos) {
        blockState = getActualState(blockState, worldIn, pos);
        if (blockState.getValue(OPEN)) {
            return NULL_AABB;
        } else {
            return blockState.getValue(FACING).getAxis() == EnumFacing.Axis.Z ? AABB_COLLISION_BOX_ZAXIS : AABB_COLLISION_BOX_XAXIS;
        }
    }

    @Override
    public boolean isPassable(IBlockAccess worldIn, BlockPos pos) {
        return isOpen(combineMetadata(worldIn, pos));
    }

    @Override
    public IBlockState getStateForPlacement(World worldIn, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer) {
        boolean flag = worldIn.isBlockPowered(pos);
        return this.getDefaultState().withProperty(FACING, placer.getHorizontalFacing()).withProperty(OPEN, flag).withProperty(POWERED, flag);
    }

    @Override
    public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        BlockPos blockpos = state.getValue(HALF) == EnumGateHalf.LOWER ? pos : pos.down();
        IBlockState stateDown = pos.equals(blockpos) ? state : worldIn.getBlockState(blockpos);

        if (stateDown.getBlock() != this) {
            return false;
        } else {
            if (stateDown.getValue(OPEN)) {
                stateDown = stateDown.withProperty(OPEN, Boolean.FALSE);
                worldIn.setBlockState(blockpos, stateDown, 10);
            } else {
                EnumFacing enumfacing = EnumFacing.fromAngle((double)playerIn.rotationYaw);

                if (stateDown.getValue(FACING) == enumfacing.getOpposite()) {
                    stateDown = stateDown.withProperty(FACING, enumfacing);
                }

                stateDown = stateDown.withProperty(OPEN, Boolean.TRUE);
                worldIn.setBlockState(blockpos, stateDown, 10);
            }

            worldIn.markBlockRangeForRenderUpdate(blockpos, pos);
            worldIn.playEvent(playerIn, state.getValue(OPEN) ? 1008 : 1014, pos, 0);
            return true;
        }
    }

    @Override
    public void neighborChanged(IBlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos) {
        if (state.getValue(HALF) == EnumGateHalf.UPPER) {
            BlockPos posDown = pos.down();
            IBlockState stateDown = worldIn.getBlockState(posDown);

            if (stateDown.getBlock() != this) {
                worldIn.setBlockToAir(pos);
            } else if (blockIn != this) {
                stateDown.neighborChanged(worldIn, posDown, blockIn, fromPos);
            }
        } else {
            BlockPos posUp = pos.up();
            IBlockState stateUp = worldIn.getBlockState(posUp);

            if (stateUp.getBlock() != this) {
                worldIn.setBlockToAir(pos);
                if (!worldIn.isRemote) {
                    this.dropBlockAsItem(worldIn, pos, state, 0);
                }
            } else {
                boolean flag = worldIn.isBlockPowered(pos) || worldIn.isBlockPowered(posUp);

                if (stateUp.getValue(POWERED) != flag) {
                    worldIn.setBlockState(posUp, stateUp.withProperty(POWERED, flag), 2);
                    worldIn.setBlockState(pos, state.withProperty(OPEN, flag), 2);
                    if (flag != state.getValue(OPEN)) {
                        worldIn.markBlockRangeForRenderUpdate(pos, pos);
                        worldIn.playEvent(null, flag ? 1008 : 1014, pos, 0);
                    }
                }
            }
        }
    }

    @Override
    public void onBlockHarvested(World worldIn, BlockPos pos, IBlockState state, EntityPlayer player) {
        BlockPos blockpos = pos.down();
        BlockPos blockpos1 = pos.up();

        if (player.capabilities.isCreativeMode && state.getValue(HALF) == EnumGateHalf.UPPER && worldIn.getBlockState(blockpos).getBlock() == this) {
            worldIn.setBlockToAir(blockpos);
        }

        if (state.getValue(HALF) == EnumGateHalf.LOWER && worldIn.getBlockState(blockpos1).getBlock() == this) {
            if (player.capabilities.isCreativeMode) {
                worldIn.setBlockToAir(pos);
            }

            worldIn.setBlockToAir(blockpos1);
        }
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        return (meta & 8) > 0 ? getDefaultState().withProperty(HALF, EnumGateHalf.UPPER).withProperty(POWERED, (meta & 1) > 0): getDefaultState().withProperty(FACING, EnumFacing.getHorizontal(meta)).withProperty(OPEN, (meta & 4) != 0);
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        int i = 0;

        if (state.getValue(HALF) == EnumGateHalf.UPPER) {
            i |= 8;

            if (state.getValue(POWERED))
                i |= 1;

        } else {
            i = i | (state.getValue(FACING)).getHorizontalIndex();

            if (state.getValue(OPEN))
                i |= 4;
        }

        return i;
    }

    public static int combineMetadata(IBlockAccess worldIn, BlockPos pos) {
        IBlockState iblockstate = worldIn.getBlockState(pos);
        int i = iblockstate.getBlock().getMetaFromState(iblockstate);
        boolean flag = isTop(i);
        IBlockState iblockstate1 = worldIn.getBlockState(pos.down());
        int j = iblockstate1.getBlock().getMetaFromState(iblockstate1);
        int k = flag ? j : i;
        IBlockState iblockstate2 = worldIn.getBlockState(pos.up());
        int l = iblockstate2.getBlock().getMetaFromState(iblockstate2);
        int i1 = flag ? i : l;
        boolean flag1 = (i1 & 1) != 0;
        boolean flag2 = (i1 & 2) != 0;
        return removeHalfBit(k) | (flag ? 8 : 0) | (flag1 ? 16 : 0) | (flag2 ? 32 : 0);
    }

    private static int removeHalfBit(int meta) {
        return meta & 7;
    }

    private static boolean isOpen(int combinedMeta) {
        return (combinedMeta & 4) != 0;
    }

    private static boolean isTop(int meta) {
        return (meta & 8) != 0;
    }

    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, HALF, FACING, OPEN, IN_WALL, POWERED);
    }

    @Override
    public BlockFaceShape getBlockFaceShape(IBlockAccess worldIn, IBlockState state, BlockPos pos, EnumFacing face) {
        state = getActualState(state, worldIn, pos);
        if (face != EnumFacing.UP && face != EnumFacing.DOWN) {
            return state.getValue(FACING).getAxis() == face.rotateY().getAxis() ? BlockFaceShape.MIDDLE_POLE : BlockFaceShape.UNDEFINED;
        } else {
            return BlockFaceShape.UNDEFINED;
        }
    }

    @Override
    public ItemStack getRenderedItem() {
        return new ItemStack(ItemRegistry.TALL_GATE);
    }

    @Override
    public void setStateMapper(StateMap.Builder builder) {
        builder.ignore(POWERED).ignore(IN_WALL);
    }

    public enum EnumGateHalf implements IStringSerializable {
        UPPER,
        LOWER;

        public String toString()
        {
            return this.getName();
        }

        public String getName() {
            return this.name().toLowerCase();
        }
    }
}
