package com.catface.mods.glass.client.entity;

import com.catface.mods.glass.common.entity.PortalEntity;
import me.ichun.mods.ichunutil.common.core.util.EntityHelper;
import me.ichun.mods.ichunutil.common.module.worldportals.client.render.WorldPortalRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;

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
        GlStateManager.enableCull();
        PortalEntityPlacement placement = new PortalEntityPlacement(entity);
        Minecraft mc = Minecraft.getMinecraft();
        Entity view = mc.getRenderViewEntity();
        float[] appliedOffset = placement.getQuaternionFormula().applyPositionalRotation(new float[] { EntityHelper.interpolateValues((float)view.prevPosX, (float)view.posX, partialTicks) - (float)entity.posX, EntityHelper.interpolateValues((float)view.prevPosY, (float)view.posY, partialTicks) + view.getEyeHeight() - (float)entity.posY, EntityHelper.interpolateValues((float)view.prevPosZ, (float)view.posZ, partialTicks) - (float)entity.posZ });
        float[] appliedRotation = placement.getQuaternionFormula().applyRotationalRotation(new float[] { EntityHelper.interpolateValues(view.prevRotationYaw, view.rotationYaw, partialTicks), EntityHelper.interpolateValues(view.prevRotationPitch, entity.rotationPitch, partialTicks), WorldPortalRenderer.getRollFactor(WorldPortalRenderer.renderLevel, partialTicks) });
        placement.drawPlane(partialTicks);
        WorldPortalRenderer.renderWorldPortal(mc,placement,entity,appliedOffset,appliedRotation,partialTicks);
        GlStateManager.disableCull();
        GlStateManager.popMatrix();
    }

    @Nullable
    @Override
    protected ResourceLocation getEntityTexture(PortalEntity entity) {
        return null;
    }
}
