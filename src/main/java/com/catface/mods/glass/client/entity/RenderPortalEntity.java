package com.catface.mods.glass.client.entity;

import com.catface.mods.glass.common.entity.PortalEntity;
import me.ichun.mods.ichunutil.common.core.util.EntityHelper;
import me.ichun.mods.ichunutil.common.module.worldportals.client.render.WorldPortalRenderer;
import me.ichun.mods.ichunutil.common.module.worldportals.common.portal.EntityTransformationStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

import javax.annotation.Nullable;

public class RenderPortalEntity extends Render<PortalEntity> {
    public RenderPortalEntity(RenderManager renderManager) {
        super(renderManager);
    }

    @Override
    public void doRender(PortalEntity entity, double x, double y, double z, float entityYaw, float partialTicks) {
        super.doRender(entity, x, y, z, entityYaw, partialTicks);
        GlStateManager.pushMatrix();
        GlStateManager.translate(x,y,z);
        GlStateManager.color(1f,1f,1f,1f);

        GlStateManager.disableLighting();
        GlStateManager.disableNormalize();
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        GlStateManager.alphaFunc(GL11.GL_GREATER, 0.00625F);
        GlStateManager.enableCull();
        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240F, 240F);
        GlStateManager.disableTexture2D();


        PortalEntityPlacement placement = new PortalEntityPlacement(entity);
        Minecraft mc = Minecraft.getMinecraft();
        Entity view = mc.getRenderViewEntity();
        float[] appliedOffset = placement.getQuaternionFormula().applyPositionalRotation(new float[] { EntityHelper.interpolateValues((float)view.prevPosX, (float)view.posX, partialTicks) - (float)entity.posX, EntityHelper.interpolateValues((float)view.prevPosY, (float)view.posY, partialTicks) + view.getEyeHeight() - (float)entity.posY, EntityHelper.interpolateValues((float)view.prevPosZ, (float)view.posZ, partialTicks) - (float)entity.posZ });
        float[] appliedRotation = placement.getQuaternionFormula().applyRotationalRotation(new float[] { EntityHelper.interpolateValues(view.prevRotationYaw, view.rotationYaw, partialTicks), EntityHelper.interpolateValues(view.prevRotationPitch, view.rotationPitch, partialTicks), WorldPortalRenderer.getRollFactor(WorldPortalRenderer.renderLevel, partialTicks) });


        EntityTransformationStack ets = new EntityTransformationStack(entity).moveEntity(entity.tpLoc.x, entity.tpLoc.y, entity.tpLoc.z, new float[] { 0F, 0F, 0F }, appliedRotation, partialTicks);
        ets.reset();

        if(WorldPortalRenderer.renderLevel == 0) {

            WorldPortalRenderer.renderWorldPortal(mc, placement, view, appliedOffset, appliedRotation, partialTicks);
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

        placement.drawPlane(partialTicks);

        GlStateManager.enableTexture2D();
        int i = entity.world.getCombinedLight(entity.getPosition(), 0);
        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, (float)(i % 65536) / 1.0F, (float)(i / 65536) / 1.0F);
        GlStateManager.disableBlend();
        GlStateManager.disableCull();
        GlStateManager.alphaFunc(GL11.GL_GREATER, 0.1F);
        GlStateManager.enableNormalize();
        GlStateManager.enableLighting();
        GlStateManager.popMatrix();
    }

    @Nullable
    @Override
    protected ResourceLocation getEntityTexture(PortalEntity entity) {
        return null;
    }
}
