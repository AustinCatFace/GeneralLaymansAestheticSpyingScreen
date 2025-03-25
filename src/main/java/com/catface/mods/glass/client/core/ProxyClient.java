package com.catface.mods.glass.client.core;

import com.catface.mods.glass.client.entity.EntityPortalView;
import com.catface.mods.glass.client.entity.RenderPortalEntity;
import com.catface.mods.glass.client.render.TileEntityGlassRenderer;
import com.catface.mods.glass.client.render.TileEntityGlassTerminalRenderer;
import com.catface.mods.glass.client.render.TileEntityMirrorRenderer;
import com.catface.mods.glass.common.CFGlass;
import com.catface.mods.glass.common.core.ProxyCommon;
import com.catface.mods.glass.common.entity.PortalEntity;
import com.catface.mods.glass.common.tileentity.TileEntityGlassBase;
import com.catface.mods.glass.common.tileentity.TileEntityGlassTerminal;
import com.catface.mods.glass.common.tileentity.mirror.TileEntityMirrorBase;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.registry.EntityRegistry;

public class ProxyClient extends ProxyCommon
{
    @Override
    public void preInit()
    {
        super.preInit();

        CFGlass.eventHandlerClient = new EventHandlerClient();
        MinecraftForge.EVENT_BUS.register(CFGlass.eventHandlerClient);
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityGlassBase.class, new TileEntityGlassRenderer());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityGlassTerminal.class, new TileEntityGlassTerminalRenderer());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityMirrorBase.class, new TileEntityMirrorRenderer());
        RenderingRegistry.registerEntityRenderingHandler(PortalEntity.class, RenderPortalEntity::new);
        EntityRegistry.registerModEntity(new ResourceLocation("cfblocks:portal_view_entity"), EntityPortalView.class, "mirror_entity", 1, CFGlass.instance, 80, 1, false);
    }
}
