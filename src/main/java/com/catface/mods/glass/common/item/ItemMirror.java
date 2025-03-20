package com.catface.mods.glass.common.item;

import com.catface.mods.glass.common.tileentity.mirror.TileEntityMirrorMaster;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class ItemMirror extends ItemBlock
{
    public ItemMirror(Block block)
    {
        super(block);
        this.setMaxDamage(0);
        this.setHasSubtypes(true);
    }

    @Override
    public int getMetadata(int damage)
    {
        return damage;
    }

    @Override
    public String getUnlocalizedName(ItemStack stack)
    {
        return super.getUnlocalizedName() + "." + (stack.getMetadata() == 0 ? "base" : "master");
    }

    @Override
    public boolean placeBlockAt(ItemStack stack, EntityPlayer player, World world, BlockPos pos, EnumFacing side, float hitX, float hitY, float hitZ, IBlockState newState)
    {
        boolean flag = super.placeBlockAt(stack, player, world, pos, side, hitX, hitY, hitZ, newState);

        TileEntity te = world.getTileEntity(pos);
        if(te instanceof TileEntityMirrorMaster)
        {
            ((TileEntityMirrorMaster)te).placingFace = EnumFacing.getDirectionFromEntityLiving(pos, player);
        }

        return flag;
    }
}
