package com.catface.mods.glass.client.render;

import com.catface.mods.glass.common.CFGlass;
import com.catface.mods.glass.common.block.MirrorPlacement;
import com.catface.mods.glass.common.tileentity.mirror.TileEntityMirrorBase;
import com.catface.mods.glass.common.tileentity.mirror.TileEntityMirrorMaster;
import me.ichun.mods.ichunutil.common.core.util.EntityHelper;
import me.ichun.mods.ichunutil.common.module.worldportals.client.render.WorldPortalRenderer;
import me.ichun.mods.ichunutil.common.module.worldportals.common.portal.EntityTransformationStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelEnderCrystal;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;

public class TileEntityMirrorRenderer extends TileEntitySpecialRenderer<TileEntityMirrorBase> {

    public static final ResourceLocation ENDER_CRYSTAL_TEXTURES = new ResourceLocation("textures/entity/endercrystal/endercrystal.png");
    public ModelEnderCrystal modelEnderCrystal;

    public TileEntityMirrorRenderer()
    {
        modelEnderCrystal = new ModelEnderCrystal(0F, false);
    }

    @Override
    public void render(TileEntityMirrorBase te, double x, double y, double z, float partialTick, int destroyStage, float alpha1)
    {
        GlStateManager.pushMatrix();

        GlStateManager.translate(x + 0.5D, y + 0.5D, z + 0.5D);

        GlStateManager.color(1F, 1F, 1F, 1F);

        //Render Plane
        GlStateManager.disableLighting();
        GlStateManager.disableNormalize();
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        GlStateManager.alphaFunc(GL11.GL_GREATER, 0.00625F);
        GlStateManager.enableCull();
        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240F, 240F);

        boolean isMaster = te instanceof TileEntityMirrorMaster;

        
        te.lastDraw = 8;

        GlStateManager.disableTexture2D();

        if(te.active)
        {
            drawScene(te, partialTick);
        }

        drawPlanes(te, 1F, 1F, 1F, -1F, isMaster ? 0.501D : 0.502D, partialTick);

        GlStateManager.enableTexture2D();

        int i = te.getWorld().getCombinedLight(te.getPos(), 0);
        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, (float)(i % 65536) / 1.0F, (float)(i / 65536) / 1.0F);
        GlStateManager.disableBlend();
        GlStateManager.alphaFunc(GL11.GL_GREATER, 0.1F);
        GlStateManager.enableNormalize();
        GlStateManager.enableLighting();

        GlStateManager.popMatrix();
    }

    public void drawScene(TileEntityMirrorBase te, float partialTick)
    {
        boolean master = te instanceof TileEntityMirrorMaster;
        if(WorldPortalRenderer.renderLevel == 0 && !CFGlass.eventHandlerClient.drawnChannels.contains(te.channel))
        {
            //Draw scene
            CFGlass.eventHandlerClient.drawnChannels.add(te.channel);

            MirrorPlacement placement = CFGlass.eventHandlerClient.getMirrorPlacement(te.channel);

            //if(master) CFGlass.LOGGER.logger.info(placement);

            if(placement != null)
            {
                //CFGlass.LOGGER.logger.info("placement is not null");
                placement.renderCaller = te;

                Minecraft mc = Minecraft.getMinecraft();
                Entity entity = mc.getRenderViewEntity();
                double centerX = placement.master.getPos().getX() + 0.5D;
                double centerY = placement.master.getPos().getY() + 0.5D;
                double centerZ = placement.master.getPos().getZ() + 0.5D;

                double destX = placement.master.getPos().getX() + 0.5D;
                double destY = placement.master.getPos().getY() + 0.5D;
                double destZ = placement.master.getPos().getZ() + 0.5D;

                float[] appliedOffset = placement.getQuaternionFormula().applyPositionalRotation(new float[] { EntityHelper.interpolateValues((float)entity.prevPosX, (float)entity.posX, partialTick) - (float)centerX, EntityHelper.interpolateValues((float)entity.prevPosY, (float)entity.posY, partialTick) + entity.getEyeHeight() - (float)centerY, EntityHelper.interpolateValues((float)entity.prevPosZ, (float)entity.posZ, partialTick) - (float)centerZ });
                float[] appliedRotation = placement.getQuaternionFormula().applyRotationalRotation(new float[] { EntityHelper.interpolateValues(entity.prevRotationYaw, entity.rotationYaw, partialTick), EntityHelper.interpolateValues(entity.prevRotationPitch, entity.rotationPitch, partialTick), WorldPortalRenderer.getRollFactor(WorldPortalRenderer.renderLevel, partialTick) });

                EntityTransformationStack ets = new EntityTransformationStack(entity).moveEntity(destX, destY, destZ, new float[] { 0F, 0F, 0F }, appliedRotation, partialTick);
                //mc.entityRenderer.updateFogColor(partialTick);
                ets.reset();
                //End Transform the player position for fog.

                GlStateManager.enableCull();
                for(TileEntityMirrorBase base : placement.activeBlocks)
                {
                    if(base.active && base.lastDraw > 0)
                    {
                        GlStateManager.pushMatrix();
                        GlStateManager.translate(base.getPos().getX() - te.getPos().getX(), base.getPos().getY() - te.getPos().getY(), base.getPos().getZ() - te.getPos().getZ());

                        TileEntityMirrorRenderer.drawPlanes(base, 1.0f, 1.0f, 1.0f, 1F, 0.501D, partialTick);

                        GlStateManager.popMatrix();
                    }
                }

                //Draw the new scene
              //GlStateManager.pushMatrix();
                WorldPortalRenderer.renderWorldPortal(mc, placement, entity, appliedOffset, appliedRotation, partialTick);// EXPLOSIONS
                //GlStateManager.popMatrix();
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
            } else if (te.channel.equals("default") && te instanceof TileEntityMirrorMaster){
                //CFGlass.LOGGER.logger.info("default - is a master");
                placement = new MirrorPlacement(te.getWorld(), (TileEntityMirrorMaster) te,te, CFGlass.eventHandlerClient.getActiveMirror(te.channel));
                placement.renderCaller = te;
                Minecraft mc = Minecraft.getMinecraft();
                Entity entity = mc.getRenderViewEntity();
                double centerX = te.getPos().getX() + 0.5D;
                double centerY = te.getPos().getY() + 0.5D;
                double centerZ = te.getPos().getZ() + 0.5D;

                double destX = te.getPos().getX() + 0.5D;
                double destY = te.getPos().getY() + 0.5D;
                double destZ = te.getPos().getZ() + 0.5D;

                float[] appliedOffset = placement.getQuaternionFormula().applyPositionalRotation(new float[] { EntityHelper.interpolateValues((float)entity.prevPosX, (float)entity.posX, partialTick) - (float)centerX, EntityHelper.interpolateValues((float)entity.prevPosY, (float)entity.posY, partialTick) + entity.getEyeHeight() - (float)centerY, EntityHelper.interpolateValues((float)entity.prevPosZ, (float)entity.posZ, partialTick) - (float)centerZ });
                float[] appliedRotation = placement.getQuaternionFormula().applyRotationalRotation(new float[] { EntityHelper.interpolateValues(entity.prevRotationYaw, entity.rotationYaw, partialTick)+180.0f, EntityHelper.interpolateValues(entity.prevRotationPitch, entity.rotationPitch, partialTick), WorldPortalRenderer.getRollFactor(WorldPortalRenderer.renderLevel, partialTick) });

                EntityTransformationStack ets = new EntityTransformationStack(entity).moveEntity(destX, destY, destZ, new float[] { 0F, 0F, 0F }, appliedRotation, partialTick);
                //mc.entityRenderer.updateFogColor(partialTick);
                ets.reset();
                //End Transform the player position for fog.

                GlStateManager.enableCull();
                for(TileEntityMirrorBase base : placement.activeBlocks)
                {
                    if(base.active && base.lastDraw > 0)
                    {
                        GlStateManager.pushMatrix();
                        GlStateManager.translate(base.getPos().getX() - te.getPos().getX(), base.getPos().getY() - te.getPos().getY(), base.getPos().getZ() - te.getPos().getZ());

                        TileEntityMirrorRenderer.drawPlanes(base, 1.0f, 1.0f, 1.0f, 1F, 0.501D, partialTick);

                        GlStateManager.popMatrix();
                    }
                }

                //Draw the new scene
                WorldPortalRenderer.renderWorldPortal(mc, placement, entity, appliedOffset, appliedRotation, partialTick);// EXPLOSIONS

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
    }

    public static void drawPlanes(TileEntityMirrorBase te, float r, float g, float b, float alpha, double pushback, float partialTick)
    {
        boolean calcAlpha = alpha == -1;
        if(calcAlpha) //calculate the alpha. not drawing planes.
        {
            alpha = (float)Math.pow(MathHelper.clamp((te.fadeoutTime - partialTick) / (float)TileEntityMirrorBase.FADEOUT_TIME, 0F, 1F), 0.5D);
        }
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferbuilder = tessellator.getBuffer();

        if(te instanceof TileEntityMirrorMaster)
        {
            TileEntityMirrorMaster master = (TileEntityMirrorMaster)te;
            if(!master.wirelessPos.isEmpty() && master.setChannel.equalsIgnoreCase(master.channel))
            {
                if(master.wirelessPos.size() == 1)
                {
                    return;
                }
                else //draw planes
                {
                    EnumFacing face = master.placingFace;

                    GlStateManager.color(r, g, b, alpha);

                    GlStateManager.glBegin(GL11.GL_TRIANGLE_STRIP);
                    GlStateManager.glVertex3f((float)(face.getFrontOffsetX() * pushback), (float)(face.getFrontOffsetY() * pushback), (float)(face.getFrontOffsetZ() * pushback));
                    ArrayList<BlockPos> wirelessPos = master.wirelessPos;
                    for(int i = 0; i < wirelessPos.size(); i++)
                    {
                        if(master.active && master.wirelessTime > (i - 1) * TileEntityMirrorBase.PROPAGATE_TIME)
                        {
                            if(calcAlpha) //triangles = size() - 1
                            {
                                alpha = (float)Math.pow(MathHelper.clamp(1.0F - (((TileEntityMirrorMaster)te).wirelessTime + partialTick - ((i - 1) * TileEntityMirrorBase.PROPAGATE_TIME)) / (float)TileEntityMirrorBase.FADEOUT_TIME, 0F, 1F), 0.5D);
                            }
                            GlStateManager.color(r, g, b, alpha);
                        }
                        else if(!master.active)
                        {
                            GlStateManager.color(r, g, b, alpha);
                        }
                        else
                        {
                            GlStateManager.color(r, g, b, 0F);
                        }
                        BlockPos pos = wirelessPos.get(i);

                        float pX = (float)(face.getFrontOffsetX() * pushback * (face.getFrontOffsetX() > 0 && pos.getX() > master.getPos().getX() ? -1D : 1D));
                        float pY = (float)(face.getFrontOffsetY() * pushback * (face.getFrontOffsetY() > 0 && pos.getY() > master.getPos().getY() ? -1D : 1D));
                        float pZ = (float)(face.getFrontOffsetZ() * pushback * (face.getFrontOffsetZ() > 0 && pos.getZ() > master.getPos().getZ() ? -1D : 1D));
                        GlStateManager.glVertex3f(pos.getX() - master.getPos().getX() + pX, pos.getY() - master.getPos().getY() + pY, pos.getZ() - master.getPos().getZ() + pZ);
                    }
                    GlStateManager.glEnd();

                    GlStateManager.color(1F, 1F, 1F, 1F);
                }
                return;
            }
        }

        for(EnumFacing face : te.activeFaces)
        {
            if(!CFGlass.blockGlass.shouldSideBeRendered(te.getWorld().getBlockState(te.getPos()), te.getWorld(), te.getPos(), face))
            {
                continue;
            }
            GlStateManager.pushMatrix();
            int horiOrient = (face.getAxis() == EnumFacing.Axis.Y ? EnumFacing.UP : face).getOpposite().getHorizontalIndex();
            GlStateManager.rotate((face.getIndex() > 0 ? 180F : 0F) + -horiOrient * 90F, 0F, 1F, 0F);
            if(face.getAxis() == EnumFacing.Axis.Y)
            {
                GlStateManager.rotate(face == EnumFacing.UP ? -90F : 90F, 1F, 0F, 0F);
            }
            GlStateManager.translate(0F, 0F, pushback);
            float halfSize = 0.501F;
            bufferbuilder.begin(7, DefaultVertexFormats.POSITION_COLOR);
            bufferbuilder.pos(-halfSize,  halfSize, 0F).color(r, g, b, alpha).endVertex();
            bufferbuilder.pos(-halfSize, -halfSize, 0F).color(r, g, b, alpha).endVertex();
            bufferbuilder.pos( halfSize, -halfSize, 0F).color(r, g, b, alpha).endVertex();
            bufferbuilder.pos( halfSize,  halfSize, 0F).color(r, g, b, alpha).endVertex();


            tessellator.draw();

            GlStateManager.popMatrix();
        }
    }

    @Override
    public boolean isGlobalRenderer(TileEntityMirrorBase te)
    {
        return true;
    }
}
