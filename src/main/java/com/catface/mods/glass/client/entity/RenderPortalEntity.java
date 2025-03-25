package com.catface.mods.glass.client.entity;

import com.catface.mods.glass.common.entity.PortalEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.entity.Entity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Mod.EventBusSubscriber
@SideOnly(Side.CLIENT)
public class RenderPortalEntity extends Render<PortalEntity> {
    private static Minecraft mc = Minecraft.getMinecraft();
    public static RenderGlobal portalRenderGlobal = new PortalRenderGlobal(mc);

    public static boolean rendering = false;
    public static Entity renderEntity = null;
    public static Entity backupEntity = null;
    private static int quality = 1024;
    private static long renderEndNanoTime;

    private static Map<EntityPortalView, Integer> registeredPortals = new ConcurrentHashMap<>();
    private static List<Integer> pendingRemoval = Collections.synchronizedList(new ArrayList<Integer>());


    public RenderPortalEntity(RenderManager renderManager) {
        super(renderManager);
    }

    @Override
    public void doRender(PortalEntity entity, double x, double y, double z, float entityYaw, float partialTicks) {
        super.doRender(entity, x, y, z, entityYaw, partialTicks);
        if(TileEntityRendererDispatcher.instance.entity instanceof EntityPortalView)
            return;

        EntityPortalView portalView = entity.getPortalView();
        if(portalView == null)
            return;

        double w = entity.dimensions.x;
        double h = entity.dimensions.y;
        int qw = (int) (w*quality);
        int qh = (int) (h*quality);
        if(!registeredPortals.containsKey(portalView))
        {
            int newTextureId = GL11.glGenTextures();
            GlStateManager.bindTexture(newTextureId);
            GlStateManager.scale(w,h,1);
            GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGB, quality, quality, 0, GL11.GL_RGB, GL11.GL_UNSIGNED_BYTE, BufferUtils.createByteBuffer(3 * quality * quality));
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
            registeredPortals.put(portalView, newTextureId);
            return;
        }

        portalView.rendering = true;

        EnumFacing facing = entity.getHorizontalFacing();
        GlStateManager.pushMatrix();
        {
            GlStateManager.enableBlend();
            OpenGlHelper.glBlendFunc(770, 771, 1, 0);

            GlStateManager.disableLighting();
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            GlStateManager.bindTexture(registeredPortals.get(portalView));

//            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
//            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);

            GlStateManager.translate(x+w/2, y, z);
            GlStateManager.rotate((float) entity.portalRotation.x, 0, 1, 0);
            GlStateManager.rotate(180,0,0,1);
            GlStateManager.translate(0,-h,0);
            GlStateManager.scale(w,h,1);

            GlStateManager.enableRescaleNormal();
            int u = 0;
            int v = 0;
            int u2 = 1;
            int v2 = 1;
            // Render
            GL11.glBegin(GL11.GL_QUADS);
            {
//                GL11.glTexCoord2d(1, 0);
//                GL11.glVertex3d(w/2, h, 0);
//                GL11.glTexCoord2d(0, 0);
//                GL11.glVertex3d(-w/2, h, 0);
//                GL11.glTexCoord2d(0, 1);
//                GL11.glVertex3d(-w/2, 0, 0);
//                GL11.glTexCoord2d(1, 1);
//                GL11.glVertex3d(w/2, 0, 0);

                GL11.glTexCoord2d(u, v); // Bottom-left of texture
                GL11.glVertex3d(0, 1, 0);
                GL11.glTexCoord2d(u2, v); // Bottom-right of texture
                GL11.glVertex3d(1, 1, 0);
                GL11.glTexCoord2d(u2, v2); // Top-right of texture
                GL11.glVertex3d(1, 0, 0);
                GL11.glTexCoord2d(u, v2); // Top-left of texture
                GL11.glVertex3d(0, 0, 0);
            }
            GL11.glEnd();

            GlStateManager.disableRescaleNormal();
            GlStateManager.disableBlend();
            GlStateManager.enableLighting();
        }
        GlStateManager.popMatrix();
    }

    @Nullable
    @Override
    protected ResourceLocation getEntityTexture(PortalEntity entity) {
        return null;
    }

    public static void removeRegisteredPortal(EntityPortalView entity)
    {
        pendingRemoval.add(registeredPortals.get(entity));
        registeredPortals.remove(entity);
    }

    public static void clearRegisteredPortals()
    {
        registeredPortals.clear();
    }

    @SubscribeEvent
    public static void onTick(TickEvent.RenderTickEvent event)
    {
        if(event.phase.equals(TickEvent.Phase.END))
            return;


        if(!pendingRemoval.isEmpty())
        {
            for(Integer integer : pendingRemoval)
            {
                GlStateManager.deleteTexture(integer);
            }
            pendingRemoval.clear();
        }

        if(mc.inGameHasFocus)
        {
            for(EntityPortalView entity : registeredPortals.keySet())
            {
                if(entity == null)
                {
                    registeredPortals.remove(entity);
                    continue;
                }

                if(!entity.rendering)
                    continue;

                if(!mc.player.canEntityBeSeen(entity))
                    continue;

                if(entity.getDistance(mc.player) < 32)
                {
                    GameSettings settings = mc.gameSettings;
                    RenderGlobal renderBackup = mc.renderGlobal;
                    Entity entityBackup = mc.getRenderViewEntity();
                    int thirdPersonBackup = settings.thirdPersonView;
                    boolean hideGuiBackup = settings.hideGUI;
                    int mipmapBackup = settings.mipmapLevels;
                    float fovBackup = settings.fovSetting;
                    int widthBackup = mc.displayWidth;
                    int heightBackup = mc.displayHeight;

                    mc.renderGlobal = portalRenderGlobal;
                    mc.setRenderViewEntity(entity);
                    settings.fovSetting = 80;
                    settings.thirdPersonView = 0;
                    settings.hideGUI = true;
                    settings.mipmapLevels = 3;

                    double w = entity.tiedEntity.dimensions.x;
                    double h = entity.tiedEntity.dimensions.y;
                    int qw = (int) (w*quality);
                    int qh = (int) (h*quality);
                    mc.displayWidth = quality;
                    mc.displayHeight = quality;

                    RenderPortalEntity.rendering = true;
                    RenderPortalEntity.renderEntity = mc.player;

                    int fps = Math.max(30, settings.limitFramerate);
                    EntityRenderer entityRenderer = mc.entityRenderer;
                    entityRenderer.renderWorld(event.renderTickTime, renderEndNanoTime + (1000000000 / fps));

                    GlStateManager.bindTexture(registeredPortals.get(entity));
                    GL11.glCopyTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGB, 0, 0, quality, quality, 0);

                    renderEndNanoTime = System.nanoTime();

                    RenderPortalEntity.renderEntity = null;
                    RenderPortalEntity.rendering = false;

                    mc.renderGlobal = renderBackup;
                    mc.setRenderViewEntity(entityBackup);
                    settings.fovSetting = fovBackup;
                    settings.thirdPersonView = thirdPersonBackup;
                    settings.hideGUI = hideGuiBackup;
                    settings.mipmapLevels = mipmapBackup;
                    mc.displayWidth = widthBackup;
                    mc.displayHeight = heightBackup;
                }

                entity.rendering = false;
            }
        }
    }

    @SubscribeEvent
    public static void onPrePlayerRender(RenderPlayerEvent.Pre event)
    {
        if(!rendering) return;

        if(event.getEntityPlayer() == renderEntity)
        {
            backupEntity = Minecraft.getMinecraft().getRenderManager().renderViewEntity;
            Minecraft.getMinecraft().getRenderManager().renderViewEntity = renderEntity;
        }
    }

    @SubscribeEvent
    public static void onPostPlayerRender(RenderPlayerEvent.Post event)
    {
        if(!rendering) return;

        if(event.getEntityPlayer() == renderEntity)
        {
            Minecraft.getMinecraft().getRenderManager().renderViewEntity = backupEntity;
            renderEntity = null;
        }
    }
}
