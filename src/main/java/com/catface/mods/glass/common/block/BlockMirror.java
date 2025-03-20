package com.catface.mods.glass.common.block;

import com.catface.mods.glass.common.CFGlass;
import com.catface.mods.glass.common.tileentity.mirror.TileEntityMirrorBase;

import com.catface.mods.glass.common.tileentity.mirror.TileEntityMirrorMaster;
import net.minecraft.block.Block;
import net.minecraft.block.BlockGlass;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.HashSet;

public class BlockMirror extends BlockGlass implements ITileEntityProvider {
    public static final PropertyBool MASTER = PropertyBool.create("master");
    public BlockMirror(Material materialIn, boolean ignoreSimilarity) {
        super(materialIn, ignoreSimilarity);
        this.setCreativeTab(CreativeTabs.REDSTONE);
        this.setDefaultState(this.blockState.getBaseState().withProperty(MASTER, false));
    }

    @Override
    public BlockStateContainer createBlockState()
    {
        return new BlockStateContainer(this, MASTER);
    }

    @Override
    public IBlockState getStateFromMeta(int meta)
    {
        return this.getDefaultState().withProperty(MASTER, meta == 1);
    }

    @Override
    public int getMetaFromState(IBlockState state)
    {
        return state.getValue(MASTER) ? 1 : 0;
    }
    @Override
    public void onBlockClicked(World worldIn, BlockPos pos, EntityPlayer playerIn)
    {
        if(worldIn.isRemote)
        {
            TileEntity te = worldIn.getTileEntity(pos);
            if(te instanceof TileEntityMirrorBase)
            {
                TileEntityMirrorBase base = (TileEntityMirrorBase)te;
                if(base.active)
                {
                    CFGlass.eventHandlerClient.clickedPos = pos;

                    base.fadeoutTime = TileEntityMirrorBase.FADEOUT_TIME;
                    base.fadePropagate = TileEntityMirrorBase.PROPAGATE_TIME;
                    base.fadeDistance = 2;
                    base.fadePropagate();
                }
            }
        }
    }

    @Override
    public void getSubBlocks(CreativeTabs itemIn, NonNullList<ItemStack> items)
    {
        items.add(new ItemStack(this, 1, 1));
        items.add(new ItemStack(this, 1, 0));
    }

    @Nullable
    @Override
    public TileEntity createNewTileEntity(World worldIn, int meta) {
        return meta == 1 ? new TileEntityMirrorMaster() : new TileEntityMirrorBase();
    }

    public void neighborChanged(IBlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos)
    {
        if(!worldIn.isRemote)
        {
            TileEntity tileentity = worldIn.getTileEntity(pos);
            if(tileentity instanceof TileEntityMirrorBase)
            {
                TileEntityMirrorBase base = (TileEntityMirrorBase)tileentity;

                //new base placed
                TileEntity tileentity1 = worldIn.getTileEntity(fromPos);
                if(tileentity1 instanceof TileEntityMirrorBase)
                {
                    TileEntityMirrorBase base1 = (TileEntityMirrorBase)tileentity1;
                    if(base.active)
                    {
                        HashSet<EnumFacing> propagationFaces = new HashSet<>();
                        for(EnumFacing facing : base.activeFaces)
                        {
                            propagationFaces.addAll(TileEntityMirrorBase.PROPAGATION_FACES.get(facing));
                        }
                        for(EnumFacing facing : propagationFaces)
                        {
                            BlockPos pos1 = pos.offset(facing, -1);
                            TileEntity te = worldIn.getTileEntity(pos1);
                            if(te instanceof TileEntityMirrorBase)
                            {
                                TileEntityMirrorBase base2 = (TileEntityMirrorBase)te;
                                if(base2.active && base2.channel.equals(base.channel) && base2.distance < base.distance) //this is the origin
                                {
                                    base.checkFacesToTurnOn(base2);
                                    if(!base1.active || base1.channel.equals(base.channel))
                                    {
                                        base1.bePropagatedTo(base, base.channel, base.active);
                                    }
                                }
                            }
                        }
                    }
                }

                //block was removed
                if(blockIn == this)
                {
                    if(base.active)
                    {
                        int distance = base.distance;
                        HashSet<EnumFacing> propagationFaces = new HashSet<>();
                        for(EnumFacing facing : base.activeFaces)
                        {
                            propagationFaces.addAll(TileEntityMirrorBase.PROPAGATION_FACES.get(facing));
                        }
                        for(EnumFacing facing : propagationFaces)
                        {
                            BlockPos pos1 = pos.offset(facing);
                            TileEntity te = worldIn.getTileEntity(pos1);
                            if(te instanceof TileEntityMirrorBase)
                            {
                                TileEntityMirrorBase base1 = (TileEntityMirrorBase)te;
                                if(base1.active && base1.channel.equalsIgnoreCase(base.channel) && base1.distance < distance)
                                {
                                    distance = base1.distance;
                                }
                            }
                        }
                        if(distance == base.distance)
                        {
                            base.bePropagatedTo(base, base.channel, false); //turn off if we can't find a close base.
                        }
                        else
                        {
                            for(EnumFacing facing : propagationFaces)
                            {
                                BlockPos pos1 = pos.offset(facing, -1);
                                TileEntity te = worldIn.getTileEntity(pos1);
                                if(te instanceof TileEntityMirrorBase)
                                {
                                    TileEntityMirrorBase base2 = (TileEntityMirrorBase)te;
                                    if(base2.active && base2.channel.equals(base.channel) && base2.distance < base.distance) //this is the origin
                                    {
                                        base.checkFacesToTurnOn(base2);
                                    }
                                }
                            }
                        }
                    }
                }

                if(base instanceof TileEntityMirrorMaster)
                {
                    TileEntityMirrorMaster player = (TileEntityMirrorMaster)base;
                    boolean flag = worldIn.isBlockPowered(pos);

                    if(player.powered != flag)
                    {
                        player.changeRedstoneState(flag);
                        player.powered = flag;
                    }
                }
            }
        }
    }
}
