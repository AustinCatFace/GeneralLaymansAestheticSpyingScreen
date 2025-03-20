package com.catface.mods.glass.common.block;

import com.catface.mods.glass.common.tileentity.mirror.TileEntityMirrorBase;
import com.catface.mods.glass.client.render.TileEntityMirrorRenderer;
import com.catface.mods.glass.common.tileentity.mirror.TileEntityMirrorMaster;
import me.ichun.mods.ichunutil.common.module.worldportals.common.portal.WorldPortal;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.HashSet;

public class MirrorPlacement extends WorldPortal {

    public TileEntityMirrorMaster master;
    public HashSet<TileEntityMirrorBase> activeBlocks;

    public TileEntityMirrorBase renderCaller;

    public boolean playedAmbience;

    public MirrorPlacement(World world){
        super((world));
    }

    public MirrorPlacement(World world, TileEntityMirrorMaster master, TileEntityMirrorBase terminal, HashSet<TileEntityMirrorBase> activeBlocks)
    {
        super(world, new Vec3d(master.getPos()).addVector(0.5D, 0.5D, 0.5D), master.placingFace, EnumFacing.UP, 0F, 0F);
        this.master = master;
        this.activeBlocks = activeBlocks;

        MirrorPlacement pair = new MirrorPlacement(world);
        pair.setPosition(new Vec3d(terminal.getPos().offset(master.placingFace.getOpposite(), -1)).addVector(0.5D, 0.5D, 0.5D));
        pair.setFace(master.placingFace, EnumFacing.UP);
        setPair(pair);
        pair.setPair(this);

        generateActiveFaces();
    }

    @Override
    public float getPlaneOffset()
    {
        return 0F;
    }

    @Override
    public boolean canCollideWithBorders()
    {
        return false;
    }

    @Override
    public String owner()
    {
        return "GLASS_MIRROR";
    }

    @Override
    public void drawPlane(float partialTick)
    {

        for(TileEntityMirrorBase base : activeBlocks)
        {
            if(base.active && base.lastDraw > 0)
            {
                GlStateManager.pushMatrix();
                GlStateManager.translate(base.getPos().getX() - renderCaller.getPos().getX(), base.getPos().getY() - renderCaller.getPos().getY(), base.getPos().getZ() - renderCaller.getPos().getZ());
                TileEntityMirrorRenderer.drawPlanes(base, 1F, 1F, 1F, 1F, 0.501D, partialTick);

                GlStateManager.popMatrix();
            }
        }
    }

    public void generateActiveFaces()
    {
        boolean cullRender = true;
        HashSet<EnumFacing> faces = new HashSet<>();
        for(TileEntityMirrorBase base : activeBlocks)
        {
            if(base instanceof TileEntityMirrorMaster && !((TileEntityMirrorMaster)base).wirelessPos.isEmpty())
            {
                cullRender = false;
                break;
            }
            if(base.active)
            {
                faces.addAll(base.activeFaces);
                if(faces.size() > 1)
                {
                    cullRender = false;
                    break;
                }
            }
        }
        setCullRender(cullRender);

    }

    public void addActiveMirror(TileEntityMirrorBase base)
    {
        generateActiveFaces();
    }

    public void removeActiveMirror(TileEntityMirrorBase base)
    {
        generateActiveFaces();
    }

    @Override
    public boolean canTeleportEntities()
    {
        return false;
    }

    @Override
    public <T extends WorldPortal> T createFakeInstance(NBTTagCompound tag)
    {
        return null;
    }

}
