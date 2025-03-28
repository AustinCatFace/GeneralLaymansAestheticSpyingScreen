package com.catface.mods.glass.common.block;

import com.catface.mods.glass.common.tileentity.TileEntityGlassBase;
import com.catface.mods.glass.common.tileentity.TileEntityGlassMaster;
import com.catface.mods.glass.common.tileentity.TileEntityGlassTerminal;
import com.catface.mods.glass.client.render.TileEntityGlassRenderer;
import com.catface.mods.glass.client.sound.SoundGlassAmbience;
import com.catface.mods.glass.common.CFGlass;
import me.ichun.mods.ichunutil.common.module.worldportals.common.portal.WorldPortal;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.HashSet;

public class TerminalPlacement extends WorldPortal
{
    public TileEntityGlassMaster master;
    public TileEntityGlassBase fauxTerminal;
    public TileEntityGlassTerminal terminal;
    public HashSet<TileEntityGlassBase> activeBlocks;

    public TileEntityGlassBase renderCaller;

    public boolean playedAmbience;

    public TerminalPlacement(World world)
    {
        super(world);
        master = null;
        terminal = null;
        activeBlocks = null;
        //This should never NEVER NEVER be called except when creating a pair.
    }

    public TerminalPlacement(World world, TileEntityGlassMaster master, TileEntityGlassBase terminal, HashSet<TileEntityGlassBase> activeBlocks)
    {
        super(world, new Vec3d(master.getPos()).addVector(0.5D, 0.5D, 0.5D), master.placingFace, EnumFacing.UP, 0F, 0F);
        this.master = master;
        this.fauxTerminal = terminal;
        this.activeBlocks = activeBlocks;

        TerminalPlacement pair = new TerminalPlacement(world);
        pair.setPosition(new Vec3d(terminal.getPos().offset(master.placingFace, -1)).addVector(0.5D, 0.5D, 0.5D));
        pair.setFace(master.placingFace.getOpposite(), EnumFacing.UP);
        setPair(pair);
        pair.setPair(this);

        generateActiveFaces();
    }

    public TerminalPlacement(World world, TileEntityGlassMaster master, TileEntityGlassTerminal terminal, HashSet<TileEntityGlassBase> activeBlocks)
    {
        super(world, new Vec3d(master.getPos()).addVector(0.5D, 0.5D, 0.5D), master.placingFace, EnumFacing.UP, 0F, 0F);
        this.master = master;
        this.terminal = terminal;
        this.activeBlocks = activeBlocks;

        TerminalPlacement pair = new TerminalPlacement(world);
        pair.setPosition(new Vec3d(terminal.getPos().offset(terminal.facing, -1)).addVector(0.5D, 0.5D, 0.5D));
        pair.setFace(terminal.facing.getOpposite(), EnumFacing.UP);
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
        return "GLASS";
    }

    @Override
    public void drawPlane(float partialTick)
    {
        if(!playedAmbience)
        {
            playedAmbience = true;
            Minecraft.getMinecraft().getSoundHandler().playSound(new SoundGlassAmbience(CFGlass.soundAmb, SoundCategory.BLOCKS, 0.015F, 0.6F, this));
        }

        for(TileEntityGlassBase base : activeBlocks)
        {
            if(base.active && base.lastDraw > 0)
            {
                GlStateManager.pushMatrix();
                GlStateManager.translate(base.getPos().getX() - renderCaller.getPos().getX(), base.getPos().getY() - renderCaller.getPos().getY(), base.getPos().getZ() - renderCaller.getPos().getZ());

                TileEntityGlassRenderer.drawPlanes(base, 1F, 1F, 1F, 1F, 0.501D, partialTick);

                GlStateManager.popMatrix();
            }
        }
    }

    public void generateActiveFaces() //TODO fix this
    {
        boolean cullRender = true;
        HashSet<EnumFacing> faces = new HashSet<>();
        for(TileEntityGlassBase base : activeBlocks)
        {
            if(base instanceof TileEntityGlassMaster && !((TileEntityGlassMaster)base).wirelessPos.isEmpty())
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

/*
        ArrayList<BlockPos> poses = pair.getPoses();
        ArrayList<EnumFacing> faces = pair.getFacesOn();
        for(TileEntityGlassBase base : activeBlocks)
        {
            if(base.active)
            {
                BlockPos differencePos = base.getPos().subtract(master.getPos());
                float[] appliedOffset = pair.getQuaternionFormula().applyPositionalRotation(new float[] { differencePos.getX(), differencePos.getY(), differencePos.getZ() });
                BlockPos appliedPos = terminal.getPos().add(-appliedOffset[0] + 0.01D, -appliedOffset[1] + 0.01D, -appliedOffset[2] + 0.01D);
                for(EnumFacing activeFace : base.activeFaces)
                {
                    float horiAngle = activeFace.getOpposite().getHorizontalAngle();
                    float[] appliedRotation = pair.getQuaternionFormula().applyRotationalRotation(new float[] { horiAngle, 0F, 0F });
                    float angle = horiAngle - appliedRotation[0];
                    while(angle < 0)
                    {
                        angle += 360F;
                    }
                    EnumFacing appliedFace = EnumFacing.fromAngle(angle);
                    BlockPos referencePos = appliedPos.offset(appliedFace);

                    boolean found = false;
                    for(int i = 0; i < faces.size(); i++)
                    {
                        EnumFacing face = faces.get(i);
                        BlockPos pos = poses.get(i);
                        if(face.equals(appliedFace) && (appliedFace.getAxis() == EnumFacing.Axis.X && pos.getX() == referencePos.getX() || appliedFace.getAxis() == EnumFacing.Axis.Z && pos.getZ() == referencePos.getZ()))
                        {
                            found = true;
                            break;
                        }
                    }
                    if(!found)
                    {
                        pair.addFace(appliedFace, EnumFacing.UP, new Vec3d(referencePos).addVector(0.5D, 0.5D, 0.5D));
                    }
                }
            }
        }
*/
    }

    public void addActiveGlass(TileEntityGlassBase base)
    {
        generateActiveFaces();
    }

    public void removeActiveGlass(TileEntityGlassBase base)
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
