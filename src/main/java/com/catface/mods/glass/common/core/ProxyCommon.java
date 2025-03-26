package com.catface.mods.glass.common.core;

import com.catface.mods.glass.common.entity.PortalEntity;
import com.catface.mods.glass.common.packet.PacketPortalSync;
import com.catface.mods.glass.common.packet.PacketSetChannel;
import com.catface.mods.glass.common.packet.PacketSetProjector;
import com.catface.mods.glass.common.packet.PacketWirelessOrder;
import com.catface.mods.glass.common.tileentity.*;
import com.catface.mods.glass.common.tileentity.mirror.TileEntityMirrorBase;
import com.catface.mods.glass.common.CFGlass;
import com.catface.mods.glass.common.tileentity.mirror.TileEntityMirrorMaster;
import me.ichun.mods.ichunutil.common.core.network.PacketChannel;
import me.ichun.mods.ichunutil.common.module.worldportals.common.WorldPortals;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.registry.EntityRegistry;
import net.minecraftforge.fml.common.registry.GameRegistry;

public class ProxyCommon
{
    public void preInit()
    {
        WorldPortals.init();

        GameRegistry.registerTileEntity(TileEntityGlassMaster.class, "GLASS_TEMaster");
        GameRegistry.registerTileEntity(TileEntityGlassBase.class, "GLASS_TEBase");
        GameRegistry.registerTileEntity(TileEntityGlassWireless.class, "GLASS_TEWireless");
        GameRegistry.registerTileEntity(TileEntityGlassTerminal.class, "GLASS_TETerminal");
        GameRegistry.registerTileEntity(TileEntityMirrorBase.class,"GLASS_MIRRORBase");
        GameRegistry.registerTileEntity(TileEntityMirrorMaster.class,"GLASS_MIRRORMaster");
        GameRegistry.registerTileEntity(TileEntityPortal.class,"GLASS_PORTAL");

        CFGlass.eventHandlerServer = new EventHandlerServer();
        MinecraftForge.EVENT_BUS.register(CFGlass.eventHandlerServer);

        CFGlass.channel = new PacketChannel("GLASS", PacketSetChannel.class, PacketSetProjector.class, PacketWirelessOrder.class, PacketPortalSync.class);
        EntityRegistry.registerModEntity(new ResourceLocation(CFGlass.MOD_ID,"portal_entity"), PortalEntity.class,"portal_entity",0,CFGlass.instance,256,1,true);
    }
}
