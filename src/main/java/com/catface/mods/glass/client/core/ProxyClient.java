package com.catface.mods.glass.client.core;

import com.catface.mods.glass.client.entity.RenderPortalEntity;
import com.catface.mods.glass.client.render.TileEntityGlassRenderer;
import com.catface.mods.glass.client.render.TileEntityGlassTerminalRenderer;
import com.catface.mods.glass.client.render.TileEntityMirrorRenderer;
import com.catface.mods.glass.client.render.TileEntityPortalRenderer;
import com.catface.mods.glass.common.CFGlass;
import com.catface.mods.glass.common.core.ProxyCommon;
import com.catface.mods.glass.common.entity.PortalEntity;
import com.catface.mods.glass.common.tileentity.TileEntityGlassBase;
import com.catface.mods.glass.common.tileentity.TileEntityGlassTerminal;
import com.catface.mods.glass.common.tileentity.TileEntityPortal;
import com.catface.mods.glass.common.tileentity.mirror.TileEntityMirrorBase;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.client.registry.RenderingRegistry;

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
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityPortal.class,new TileEntityPortalRenderer());
        RenderingRegistry.registerEntityRenderingHandler(PortalEntity.class, RenderPortalEntity::new);
    }
}
