package com.catface.mods.glass.client.render;

import com.catface.mods.glass.common.CFGlass;
import com.catface.mods.glass.common.block.PortalPlacement;
import com.catface.mods.glass.common.tileentity.TileEntityBoxPortal;
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
import net.minecraft.util.math.BlockPos;
import org.lwjgl.opengl.GL11;

public class TileEntityBoxPortalRenderer extends TileEntitySpecialRenderer<TileEntityBoxPortal> {

    public TileEntityBoxPortalRenderer(){

    }

    @Override
    public void render(TileEntityBoxPortal te, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
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

    public void drawScene(TileEntityBoxPortal te, float partialTick)
    {

        if(WorldPortalRenderer.renderLevel == 0 && !CFGlass.eventHandlerClient.drawnChannels.contains(te.name))
        {
            CFGlass.eventHandlerClient.drawnChannels.add(te.name);
            PortalPlacement placement = CFGlass.eventHandlerClient.getPortalPlacement(te.name);

            if(placement == null){
                //CFGlass.LOGGER.logger.info("creating placement render");
                placement = new PortalPlacement(te);
            }

            Minecraft mc = Minecraft.getMinecraft();
            Entity entity = mc.getRenderViewEntity();
            double centerX = te.getPos().getX() + 0.5f+te.portalOffset.x;
            double centerY = te.getPos().getY()+te.portalOffset.y;
            double centerZ = te.getPos().getZ() + 0.5f+te.portalOffset.z;

            double destX = te.tpLoc.x;
            double destY = te.tpLoc.y;
            double destZ = te.tpLoc.z;
            float viewScale = 1.0f/te.scale;
            float[] appliedOffset = placement.getQuaternionFormula().applyPositionalRotation(new float[] { EntityHelper.interpolateValues((float)entity.prevPosX, (float)entity.posX, partialTick) - (float)centerX, EntityHelper.interpolateValues((float)entity.prevPosY, (float)entity.posY, partialTick) + entity.getEyeHeight() - (float)centerY, EntityHelper.interpolateValues((float)entity.prevPosZ, (float)entity.posZ, partialTick) - (float)centerZ });
            //float[] appliedOffset = new float[]{0.0f,0.0f,0.0f};
            appliedOffset[0] = appliedOffset[0]*viewScale;
            appliedOffset[1] = appliedOffset[1]*viewScale;
            appliedOffset[2] = appliedOffset[2]*viewScale;
            float[] appliedRotation = placement.getQuaternionFormula().applyRotationalRotation(new float[] {(float) (EntityHelper.interpolateValues(entity.prevRotationYaw, entity.rotationYaw, partialTick)+te.tpRotation.x), (float) (EntityHelper.interpolateValues(entity.prevRotationPitch, entity.rotationPitch, partialTick)+te.tpRotation.y), (float) (WorldPortalRenderer.getRollFactor(WorldPortalRenderer.renderLevel, partialTick)+te.tpRotation.z)});

            EntityTransformationStack ets = new EntityTransformationStack(entity).moveEntity(destX, destY, destZ, new float[] { 0F, 0F, 0F }, appliedRotation, partialTick);
            ets.reset();

            GlStateManager.enableCull();

            GlStateManager.pushMatrix();

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

    public static void drawBox(TileEntityBoxPortal te, float r, float g, float b, float a, double pushback, float partialTick)
    {

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();

        GlStateManager.pushMatrix();

        double w = te.dimensions.x;
        double l = te.dimensions.z;
        double h = te.dimensions.y;
        GlStateManager.translate(0, 0F, pushback);
        buffer.begin(7, DefaultVertexFormats.POSITION_COLOR);

        buffer.pos(-w / 2, 0, -l / 2).color(r, g, b, a).endVertex();
        buffer.pos(w / 2, 0, -l / 2).color(r, g, b, a).endVertex();
        buffer.pos(w / 2, h, -l / 2).color(r, g, b, a).endVertex();
        buffer.pos(-w / 2, h, -l / 2).color(r, g, b, a).endVertex();

        buffer.pos(w / 2, h, -l / 2).color(r, g, b, a).endVertex();
        buffer.pos(w / 2, 0, -l / 2).color(r, g, b, a).endVertex();
        buffer.pos(-w / 2, 0, -l / 2).color(r, g, b, a).endVertex();
        buffer.pos(-w / 2, h, -l / 2).color(r, g, b, a).endVertex();

        // Back face
        buffer.pos(-w / 2, 0, l / 2).color(r, g, b, a).endVertex();
        buffer.pos(w / 2, 0, l / 2).color(r, g, b, a).endVertex();
        buffer.pos(w / 2, h, l / 2).color(r, g, b, a).endVertex();
        buffer.pos(-w / 2, h, l / 2).color(r, g, b, a).endVertex();

        buffer.pos(w / 2, h, l / 2).color(r, g, b, a).endVertex();
        buffer.pos(w / 2, 0, l / 2).color(r, g, b, a).endVertex();
        buffer.pos(-w / 2, 0, l / 2).color(r, g, b, a).endVertex();
        buffer.pos(-w / 2, h, l / 2).color(r, g, b, a).endVertex();

        // Left face
        buffer.pos(-w / 2, 0, -l / 2).color(r, g, b, a).endVertex();
        buffer.pos(-w / 2, 0, l / 2).color(r, g, b, a).endVertex();
        buffer.pos(-w / 2, h, l / 2).color(r, g, b, a).endVertex();
        buffer.pos(-w / 2, h, -l / 2).color(r, g, b, a).endVertex();

        buffer.pos(-w / 2, h, l / 2).color(r, g, b, a).endVertex();
        buffer.pos(-w / 2, 0, l / 2).color(r, g, b, a).endVertex();
        buffer.pos(-w / 2, 0, -l / 2).color(r, g, b, a).endVertex();
        buffer.pos(-w / 2, h, -l / 2).color(r, g, b, a).endVertex();

        // Right face
        buffer.pos(w / 2, 0, -l / 2).color(r, g, b, a).endVertex();
        buffer.pos(w / 2, 0, l / 2).color(r, g, b, a).endVertex();
        buffer.pos(w / 2, h, l / 2).color(r, g, b, a).endVertex();
        buffer.pos(w / 2, h, -l / 2).color(r, g, b, a).endVertex();

        buffer.pos(w / 2, h, l / 2).color(r, g, b, a).endVertex();
        buffer.pos(w / 2, 0, l / 2).color(r, g, b, a).endVertex();
        buffer.pos(w / 2, 0, -l / 2).color(r, g, b, a).endVertex();
        buffer.pos(w / 2, h, -l / 2).color(r, g, b, a).endVertex();

        // Top face
        buffer.pos(-w / 2, h, -l / 2).color(r, g, b, a).endVertex();
        buffer.pos(w / 2, h, -l / 2).color(r, g, b, a).endVertex();
        buffer.pos(w / 2, h, l / 2).color(r, g, b, a).endVertex();
        buffer.pos(-w / 2, h, l / 2).color(r, g, b, a).endVertex();

        buffer.pos(w / 2, h, l / 2).color(r, g, b, a).endVertex();
        buffer.pos(w / 2, h, -l / 2).color(r, g, b, a).endVertex();
        buffer.pos(-w / 2, h, -l / 2).color(r, g, b, a).endVertex();
        buffer.pos(-w / 2, h, l / 2).color(r, g, b, a).endVertex();

        // Bottom face
        buffer.pos(-w / 2, 0, -l / 2).color(r, g, b, a).endVertex();
        buffer.pos(w / 2, 0, -l / 2).color(r, g, b, a).endVertex();
        buffer.pos(w / 2, 0, l / 2).color(r, g, b, a).endVertex();
        buffer.pos(-w / 2, 0, l / 2).color(r, g, b, a).endVertex();
        buffer.pos(w / 2, 0, l / 2).color(r, g, b, a).endVertex();
        buffer.pos(w / 2, 0, -l / 2).color(r, g, b, a).endVertex();
        buffer.pos(-w / 2, 0, -l / 2).color(r, g, b, a).endVertex();
        buffer.pos(-w / 2, 0, l / 2).color(r, g, b, a).endVertex();



        tessellator.draw();

        GlStateManager.popMatrix();

    }

    private static void drawQuad(BufferBuilder buffer, float x1, float y1, float z1, float x2, float y2, float z2, float r, float g, float b, float a) {
        buffer.pos(x1, y1, z1).color(r, g, b, a).endVertex();
        buffer.pos(x2, y1, z1).color(r, g, b, a).endVertex();
        buffer.pos(x2, y2, z1).color(r, g, b, a).endVertex();
        buffer.pos(x1, y2, z1).color(r, g, b, a).endVertex();
    }


    @Override
    public boolean isGlobalRenderer(TileEntityBoxPortal te) {
        return true;
    }
}
