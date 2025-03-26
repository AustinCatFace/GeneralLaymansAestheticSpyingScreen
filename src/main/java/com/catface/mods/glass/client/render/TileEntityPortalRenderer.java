package com.catface.mods.glass.client.render;

import com.catface.mods.glass.client.core.EventHandlerClient;
import com.catface.mods.glass.client.entity.PortalEntityPlacement;
import com.catface.mods.glass.common.CFGlass;
import com.catface.mods.glass.common.block.MirrorPlacement;
import com.catface.mods.glass.common.block.PortalPlacement;
import com.catface.mods.glass.common.tileentity.TileEntityPortal;
import com.catface.mods.glass.common.tileentity.mirror.TileEntityMirrorBase;
import com.catface.mods.glass.common.tileentity.mirror.TileEntityMirrorMaster;
import me.ichun.mods.ichunutil.common.core.util.EntityHelper;
import me.ichun.mods.ichunutil.common.module.worldportals.client.render.WorldPortalRenderer;
import me.ichun.mods.ichunutil.common.module.worldportals.common.portal.EntityTransformationStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;

public class TileEntityPortalRenderer extends TileEntitySpecialRenderer<TileEntityPortal> {

    public TileEntityPortalRenderer(){

    }

    @Override
    public void render(TileEntityPortal te, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
        super.render(te, x, y, z, partialTicks, destroyStage, alpha);

        GlStateManager.pushMatrix();

        GlStateManager.translate(x + 0.5D+te.portalOffset.x, y+te.portalOffset.y, z + 0.5D+te.portalOffset.z);

        GlStateManager.rotate((float) te.portalRotation.x,0,1,0);
        GlStateManager.rotate((float) te.portalRotation.y,1,0,0);
        GlStateManager.rotate((float) te.portalRotation.z,0,0,1);
        GlStateManager.color(1F, 1F, 1F, 1F);

        //Render Plane
        GlStateManager.disableLighting();
        GlStateManager.disableNormalize();
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        GlStateManager.alphaFunc(GL11.GL_GREATER, 0.00625F);
        GlStateManager.enableCull();
        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240F, 240F);


        GlStateManager.disableTexture2D();

        drawScene(te, partialTicks);

        GlStateManager.enableTexture2D();

        int i = te.getWorld().getCombinedLight(te.getPos(), 0);
        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, (float)(i % 65536) / 1.0F, (float)(i / 65536) / 1.0F);
        GlStateManager.disableBlend();
        GlStateManager.alphaFunc(GL11.GL_GREATER, 0.1F);
        GlStateManager.enableNormalize();
        GlStateManager.enableLighting();
        GlStateManager.disableCull();
        GlStateManager.popMatrix();
    }

    public void drawScene(TileEntityPortal te, float partialTick)
    {

        if(WorldPortalRenderer.renderLevel == 0 && !CFGlass.eventHandlerClient.drawnChannels.contains(te.name))
        {
            CFGlass.eventHandlerClient.drawnChannels.add(te.name);
            PortalPlacement placement = CFGlass.eventHandlerClient.getPortalPlacement(te.name);

            if(placement == null){
                CFGlass.LOGGER.logger.info("creating placement render");
                placement = new PortalPlacement(te);
            }

            Minecraft mc = Minecraft.getMinecraft();
            Entity entity = mc.getRenderViewEntity();
            float centerX = te.getPos().getX() + 0.5f;
            float centerY = te.getPos().getY();
            float centerZ = te.getPos().getZ() + 0.5f;

            double destX = te.tpLoc.x;
            double destY = te.tpLoc.y;
            double destZ = te.tpLoc.z;

            float[] appliedOffset = placement.getQuaternionFormula().applyPositionalRotation(new float[] { EntityHelper.interpolateValues((float)entity.prevPosX, (float)entity.posX, partialTick) - (float)centerX, EntityHelper.interpolateValues((float)entity.prevPosY, (float)entity.posY, partialTick) + entity.getEyeHeight() - (float)centerY, EntityHelper.interpolateValues((float)entity.prevPosZ, (float)entity.posZ, partialTick) - (float)centerZ });
            //float[] appliedOffset = new float[]{0.0f,0.0f,0.0f};
            float[] appliedRotation = placement.getQuaternionFormula().applyRotationalRotation(new float[] {(float) (EntityHelper.interpolateValues(entity.prevRotationYaw, entity.rotationYaw, partialTick)+te.tpRotation.x), (float) (EntityHelper.interpolateValues(entity.prevRotationPitch, entity.rotationPitch, partialTick)+te.tpRotation.y), (float) (WorldPortalRenderer.getRollFactor(WorldPortalRenderer.renderLevel, partialTick)+te.tpRotation.z)});

            EntityTransformationStack ets = new EntityTransformationStack(entity).moveEntity(destX, destY, destZ, new float[] { 0F, 0F, 0F }, appliedRotation, partialTick);
            ets.reset();

            GlStateManager.enableCull();

            GlStateManager.pushMatrix();
//            Vec3d distance = new Vec3d(EntityHelper.interpolateValues((float)entity.prevPosX, (float)entity.posX, partialTick) - (float)centerX, EntityHelper.interpolateValues((float)entity.prevPosY, (float)entity.posY, partialTick) + entity.getEyeHeight() - (float)centerY, EntityHelper.interpolateValues((float)entity.prevPosZ, (float)entity.posZ, partialTick) - (float)centerZ);
//            Vec3d scale = new Vec3d(1.0/distance.x,1.0/distance.y,1.0/distance.z);
//            GlStateManager.scale(scale.x,scale.y,scale.z);
            //Draw the new scene
            WorldPortalRenderer.renderWorldPortal(mc, placement, entity, appliedOffset, appliedRotation, partialTick);
            GlStateManager.popMatrix();
            //Reset the states
            GlStateManager.disableTexture2D();
            GlStateManager.color(1F, 1F, 1F, 1F);
            GlStateManager.disableLighting();
            GlStateManager.disableNormalize();
            GlStateManager.enableBlend();
            GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
            GlStateManager.alphaFunc(GL11.GL_GREATER, 0.00625F);
            GlStateManager.enableCull();
            OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240F, 240F);


        }
    }

    public static void drawPlanes(TileEntityPortal te, float r, float g, float b, float alpha, double pushback, float partialTick)
    {
        boolean calcAlpha = alpha == -1;
        if(calcAlpha) //calculate the alpha. not drawing planes.
        {
            alpha = 1.0f;
        }
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferbuilder = tessellator.getBuffer();

        GlStateManager.pushMatrix();
        GlStateManager.translate(0F, 0F, pushback);
        double width = te.dimensions.x/2;
        double height = te.dimensions.y;
        bufferbuilder.begin(7, DefaultVertexFormats.POSITION_COLOR);
        bufferbuilder.pos(-width,  0, 0F).color(r, g, b, alpha).endVertex();
        bufferbuilder.pos(-width, height, 0F).color(r, g, b, alpha).endVertex();
        bufferbuilder.pos( width, height, 0F).color(r, g, b, alpha).endVertex();
        bufferbuilder.pos( width,  0, 0F).color(r, g, b, alpha).endVertex();


        tessellator.draw();

        GlStateManager.popMatrix();

    }

    @Override
    public boolean isGlobalRenderer(TileEntityPortal te) {
        return true;
    }
}
