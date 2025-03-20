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
    public PortalEntityPlacement(PortalEntity entity) {
        super(entity.world, entity.tpLoc, entity.getHorizontalFacing(), EnumFacing.UP, (float) entity.dimensions.x, (float) entity.dimensions.y);
        this.entity = entity;
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

        GlStateManager.color(1.0f,1.0f,1.0f,1.0f);

//        GlStateManager.glBegin(GL11.GL_QUADS);
//        GlStateManager.glVertex3f((float) (-entity.dimensions.x/2),0,0);
//        GlStateManager.glVertex3f((float) (entity.dimensions.x/2), 0,0);
//        GlStateManager.glVertex3f((float) (entity.dimensions.x/2), (float) entity.dimensions.y,0);
//        GlStateManager.glVertex3f((float) (-entity.dimensions.x/2), (float) entity.dimensions.y,0);
//        GlStateManager.glEnd();

        bufferbuilder.begin(7, DefaultVertexFormats.POSITION_COLOR);
        bufferbuilder.pos(-entity.dimensions.x,  entity.dimensions.y, 0F).endVertex();
        bufferbuilder.pos(-entity.dimensions.x, 0, 0F).endVertex();
        bufferbuilder.pos( entity.dimensions.x, 0, 0F).endVertex();
        bufferbuilder.pos( entity.dimensions.x,  entity.dimensions.y, 0F).endVertex();
        tessellator.draw();
    }

    @Override
    public <T extends WorldPortal> T createFakeInstance(NBTTagCompound nbtTagCompound) {
        return null;
    }
}
