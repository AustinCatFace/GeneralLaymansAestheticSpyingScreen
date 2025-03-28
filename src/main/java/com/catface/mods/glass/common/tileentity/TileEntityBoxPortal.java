package com.catface.mods.glass.common.tileentity;

import com.catface.mods.glass.common.CFGlass;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import java.util.List;

public class TileEntityBoxPortal extends TileEntityPortal{

    public TileEntityBoxPortal(){
        super();
        dimensions = new Vec3d(1.0,1.0,1.0);
        this.scale = 1.0f/16.0f;
        this.teleportsEntities = false;
    }

    public TileEntityBoxPortal(String name){
        super(name);
    }

    public String generateName(){
        BlockPos pos = this.getPos();
        if(pos == null){
            return "box000";
        }
        return "box"+"["+pos.getX()+","+pos.getY()+","+pos.getZ()+"]";
    }

    @Override
    public void displayBox() {

    }
}
