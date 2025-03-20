package com.catface.mods.glass.common.tileentity;

import com.catface.mods.glass.common.CFGlass;
import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.ArrayList;

public class TileEntityGlassMaster extends TileEntityGlassBase
{
    public boolean powered;
    public String setChannel = "default";
    public EnumFacing placingFace = EnumFacing.NORTH;
    public ArrayList<BlockPos> wirelessPos = new ArrayList<>();

    public float rotationBeacon, rotationBeaconPrev;
    public int wirelessTime;

    @Override
    public void onLoad()
    {
        if(getWorld().isRemote && active && !channel.isEmpty())
        {
            CFGlass.eventHandlerClient.addActiveGlass(this, channel);

            for(BlockPos pos : wirelessPos)
            {
                TileEntity te = getWorld().getTileEntity(pos);
                if(te instanceof TileEntityGlassWireless)
                {
                    TileEntityGlassWireless wireless = (TileEntityGlassWireless)te;
                    wireless.users++;
                    CFGlass.eventHandlerClient.addActiveGlass(wireless, channel);
                }
            }
        }
    }

    @Override
    public void onChunkUnload()
    {
        if(getWorld().isRemote && active && !channel.isEmpty())
        {
            CFGlass.eventHandlerClient.removeActiveGlass(this, channel);

            for(BlockPos pos : wirelessPos)
            {
                TileEntity te = getWorld().getTileEntity(pos);
                if(te instanceof TileEntityGlassWireless)
                {
                    TileEntityGlassWireless wireless = (TileEntityGlassWireless)te;
                    wireless.users--;
                    CFGlass.eventHandlerClient.removeActiveGlass(wireless, channel);
                }
            }
        }
    }

    @Override
    public boolean shouldRefresh(World world, BlockPos pos, IBlockState oldState, IBlockState newState)
    {
        boolean flag = oldState != newState;
        if(flag && world.isRemote && active && !channel.isEmpty()) // new TE or removed
        {
            CFGlass.eventHandlerClient.removeActiveGlass(this, channel);

            for(BlockPos pos1 : wirelessPos)
            {
                TileEntity te = getWorld().getTileEntity(pos1);
                if(te instanceof TileEntityGlassWireless)
                {
                    TileEntityGlassWireless wireless = (TileEntityGlassWireless)te;
                    wireless.users--;
                    CFGlass.eventHandlerClient.removeActiveGlass(wireless, channel);
                }
            }
        }
        return flag;
    }

    @Override
    public void update()
    {
        super.update(); //Glass master is also a glass base. Let it do it's job.
        float rotationFactor = active && channel.equalsIgnoreCase(setChannel) ? (1.0F - (1.0F * (float)fadeoutTime / FADEOUT_TIME)) : (1.0F * (float)fadeoutTime / FADEOUT_TIME);
        rotationBeacon += 20F * rotationFactor;
        rotationBeaconPrev = rotationBeacon;

        if(wirelessPos.removeIf(pos -> !(getWorld().getTileEntity(pos) instanceof TileEntityGlassWireless)))
        {
            IBlockState state = getWorld().getBlockState(getPos());
            getWorld().notifyBlockUpdate(getPos(), state, state, 3);
        }

        if(active && channel.equalsIgnoreCase(setChannel) && !wirelessPos.isEmpty())
        {
            wirelessTime++;
        }
        else
        {
            wirelessTime = 0;
        }
    }

    public void changeRedstoneState(boolean newState)
    {
        if(!setChannel.isEmpty() && (!active || channel.equalsIgnoreCase(setChannel)))
        {
            if(newState)
            {
                active = true;
                channel = setChannel;
                distance = 1;
                activeFaces.add(placingFace);
            }
            else
            {
                active = false;
            }
            fadeoutTime = FADEOUT_TIME;
            propagateTime = PROPAGATE_TIME;
            IBlockState state = getWorld().getBlockState(getPos());
            getWorld().notifyBlockUpdate(getPos(), state, state, 3);
        }
    }

    @Override
    public boolean canPropagate()
    {
        return wirelessPos.isEmpty() && super.canPropagate();
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound tag)
    {
        super.writeToNBT(tag);
        tag.setBoolean("powered", powered);
        tag.setString("setChannel", setChannel);
        tag.setInteger("placingFace", placingFace.getIndex());
        tag.setInteger("wirelessPos", wirelessPos.size());
        for(int i = 0; i < wirelessPos.size(); i++)
        {
            BlockPos pos = wirelessPos.get(i);
            tag.setInteger("wPx_" + i, pos.getX());
            tag.setInteger("wPy_" + i, pos.getY());
            tag.setInteger("wPz_" + i, pos.getZ());
        }
        return tag;
    }

    @Override
    public void readFromNBT(NBTTagCompound tag)
    {
        super.readFromNBT(tag);
        powered = tag.getBoolean("powered");
        setChannel = tag.getString("setChannel");
        placingFace = EnumFacing.getFront(tag.getInteger("placingFace"));
        wirelessPos.clear();
        int pos = tag.getInteger("wirelessPos");
        for(int i = 0; i < pos; i++)
        {
            wirelessPos.add(new BlockPos(tag.getInteger("wPx_" + i), tag.getInteger("wPy_" + i), tag.getInteger("wPz_" + i)));
        }
    }

    @SideOnly(Side.CLIENT)
    @Override
    public AxisAlignedBB getRenderBoundingBox()
    {
        if(active && channel.equalsIgnoreCase(setChannel) && !wirelessPos.isEmpty())
        {
            int minX = getPos().getX();
            int minY = getPos().getY();
            int minZ = getPos().getZ();
            int maxX = getPos().getX();
            int maxY = getPos().getY();
            int maxZ = getPos().getZ();
            
            for(BlockPos pos : wirelessPos)
            {
                if(pos.getX() < minX)
                {
                    minX = pos.getX();
                }
                if(pos.getY() < minY)
                {
                    minY = pos.getY();
                }
                if(pos.getZ() < minZ)
                {
                    minZ = pos.getZ();
                }
                if(pos.getX() > maxX)
                {
                    maxX = pos.getX();
                }
                if(pos.getY() > maxY)
                {
                    maxY = pos.getY();
                }
                if(pos.getZ() > maxZ)
                {
                    maxZ = pos.getZ();
                }
            }
            return new AxisAlignedBB(new BlockPos(minX - 1, minY - 1, minZ - 1), new BlockPos(maxX + 2, maxY + 2, maxZ + 2));
        }
        return new AxisAlignedBB(getPos().add(-1, -1, -1), getPos().add(2, 2, 2));
    }
}
