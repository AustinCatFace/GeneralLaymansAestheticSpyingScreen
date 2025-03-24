package com.catface.mods.glass.client.entity;

import com.catface.mods.glass.common.entity.PortalEntity;
import me.ichun.mods.ichunutil.common.module.worldportals.common.portal.WorldPortal;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.lwjgl.opengl.GL11;

public class PortalEntityPlacement extends WorldPortal {

    public PortalEntity entity;
    public PortalEntityPlacement(World world){
        super(world);
    }
    public PortalEntityPlacement(PortalEntity entity) {
        super(entity.world, entity.getPositionVector(), entity.getHorizontalFacing(), EnumFacing.UP, (float) entity.dimensions.x, (float) entity.dimensions.y);
        this.entity = entity;

        PortalEntityPlacement pair = new PortalEntityPlacement(world);
        pair.setPosition(entity.tpLoc);
        pair.setFace(entity.getHorizontalFacing(), EnumFacing.UP);
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
        return "PORTAL";
    }

    @Override
    public void drawPlane(float v) {
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferbuilder = tessellator.getBuffer();
        GlStateManager.pushMatrix();
        //GlStateManager.rotate(90,0,1,0);
        GlStateManager.color(1.0f,1.0f,1.0f,1.0f);

        bufferbuilder.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
        bufferbuilder.pos(-entity.dimensions.x/2,  entity.dimensions.y, 0F).color(1.0f,1.0f,1.0f,1.0f).endVertex();
        bufferbuilder.pos(-entity.dimensions.x/2, 0, 0F).color(1.0f,1.0f,1.0f,1.0f).endVertex();
        bufferbuilder.pos( entity.dimensions.x/2, 0, 0F).color(1.0f,1.0f,1.0f,1.0f).endVertex();
        bufferbuilder.pos( entity.dimensions.x/2,  entity.dimensions.y, 0F).color(1.0f,1.0f,1.0f,1.0f).endVertex();
        tessellator.draw();
        GlStateManager.popMatrix();

    }

    @Override
    public <T extends WorldPortal> T createFakeInstance(NBTTagCompound nbtTagCompound) {
        return null;
    }
}
