package com.catface.mods.glass.common.block;

import com.catface.mods.glass.common.tileentity.TileEntityBoxPortal;
import net.minecraft.block.material.Material;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

import javax.annotation.Nullable;

public class BlockBoxPortal extends BlockPortal{
    public BlockBoxPortal(Material materialIn) {
        super(materialIn);
    }

    @Nullable
    @Override
    public TileEntity createNewTileEntity(World worldIn, int meta) {
        return new TileEntityBoxPortal();
    }
}
