package com.catface.mods.glass.common.block;

import com.catface.mods.glass.client.render.TileEntityBoxPortalRenderer;
import com.catface.mods.glass.client.render.TileEntityPortalRenderer;
import com.catface.mods.glass.common.CFGlass;
import com.catface.mods.glass.common.tileentity.TileEntityBoxPortal;
import com.catface.mods.glass.common.tileentity.TileEntityPortal;
import me.ichun.mods.ichunutil.common.module.worldportals.common.portal.WorldPortal;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class PortalPlacement extends WorldPortal {

    public TileEntityPortal te;
    public PortalPlacement(World world) {
        super(world);
    }
    public PortalPlacement(TileEntityPortal te){
        super(te.getWorld(), new Vec3d(te.getPos()).addVector(0.5,0,0.5), EnumFacing.getFront(te.getBlockMetadata()), EnumFacing.UP, 0.0f, 0.0f);
        this.te = te;
        //this.setSize((float) te.dimensions.x, (float) te.dimensions.y);

        Vec3d tpRot = te.tpRotation.addVector(180,0,0);

        PortalPlacement pair = new PortalPlacement(world);
        pair.te=te;
        pair.setPosition(te.tpLoc);

        pair.setFace(te.getFaceFromRotation(), EnumFacing.UP);
        setPair(pair);
        pair.setPair(this);

        setCullRender(false);


    }

    @Override
    public float getPlaneOffset() {
        return 0;
    }

    @Override
    public boolean canCollideWithBorders() {
        return false;
    }

    @Override
    public String owner() {
        return "GLASS_PORTAL";
    }

//    @Override
//    public int getRenderDistanceChunks() {
//        return 512;
//    }

    @Override
    public void drawPlane(float partialTicks) {
        GlStateManager.pushMatrix();
        //GlStateManager.translate(te.getPos().getX()+0.5+te.portalOffset.x, te.getPos().getY()+0.5+te.portalOffset.y, te.getPos().getZ()+0.5+te.portalOffset.z);
        if(te instanceof TileEntityBoxPortal){
            TileEntityBoxPortalRenderer.drawBox((TileEntityBoxPortal) te, 1F, 1F, 1F, 1F, -0.001, partialTicks);
        } else {
            TileEntityPortalRenderer.drawPlanes(te, 1F, 1F, 1F, 1F, -0.001, partialTicks);
        }


        GlStateManager.popMatrix();
    }



    @Override
    public <T extends WorldPortal> T createFakeInstance(NBTTagCompound nbtTagCompound) {
        return null;
    }
}
