package com.catface.mods.glass.client.core;

import com.catface.mods.glass.common.CFGlass;
import com.catface.mods.glass.common.block.MirrorPlacement;
import com.catface.mods.glass.common.block.PortalPlacement;
import com.catface.mods.glass.common.block.TerminalPlacement;
import com.catface.mods.glass.common.packet.PacketPortalSync;
import com.catface.mods.glass.common.tileentity.TileEntityGlassBase;
import com.catface.mods.glass.common.tileentity.TileEntityGlassMaster;
import com.catface.mods.glass.common.tileentity.TileEntityGlassTerminal;
import com.catface.mods.glass.common.tileentity.TileEntityPortal;
import com.catface.mods.glass.common.tileentity.mirror.TileEntityMirrorBase;
import com.catface.mods.glass.common.tileentity.mirror.TileEntityMirrorMaster;
import me.ichun.mods.ichunutil.client.model.item.ModelEmpty;
import me.ichun.mods.ichunutil.common.module.worldportals.common.WorldPortals;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent;

import java.util.*;

public class EventHandlerClient
{
    public BlockPos clickedPos = BlockPos.ORIGIN;
    public HashMap<String, BlockPos> terminalLocations = new HashMap<>();
    public HashMap<String, BlockPos> mirrorLocations = new HashMap<>();
    public HashMap<String, BlockPos> portalLocations = new HashMap<>();
    public HashMap<String, HashSet<TileEntityGlassBase>> activeGLASS = new HashMap<>();
    public HashMap<String, HashSet<TileEntityMirrorBase>> activeMIRROR = new HashMap<>();
    public HashSet<String> drawnChannels = new HashSet<>();
    public HashMap<String, TerminalPlacement> terminalPlacements = new HashMap<>();
    public HashMap<String, MirrorPlacement> mirrorPlacements = new HashMap<>();
    public HashMap<String, PortalPlacement> portalPlacements = new HashMap<>();
    public HashMap<String, Integer> terminalPlacementCreationTimeout = new HashMap<>();
    public HashMap<String, Integer> mirrorPlacementCreationTimeout = new HashMap<>();
    public HashMap<String, Integer> portalPlacementCreationTimeout = new HashMap<>();
    public ArrayList<PacketPortalSync> syncList = new ArrayList<>();

    @SubscribeEvent
    public void onRenderTick(TickEvent.RenderTickEvent event)
    {
        if(event.phase == TickEvent.Phase.START)
        {
            drawnChannels.clear();
        }
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event)
    {
        if(event.phase == TickEvent.Phase.END)
        {
            Minecraft mc = Minecraft.getMinecraft();
            if(mc.world != null)
            {
                if(!clickedPos.equals(BlockPos.ORIGIN))
                {
                    TileEntity te = mc.world.getTileEntity(clickedPos);
                    if(te instanceof TileEntityGlassBase && ((TileEntityGlassBase)te).active && mc.playerController.getIsHittingBlock())
                    {

                        TileEntityGlassBase base = (TileEntityGlassBase)te;
                        if(base.fadeoutTime < TileEntityGlassBase.FADEOUT_TIME - TileEntityGlassBase.PROPAGATE_TIME)
                        {
                            base.fadeoutTime = TileEntityGlassBase.FADEOUT_TIME;
                            base.fadePropagate = TileEntityGlassBase.PROPAGATE_TIME;
                            base.fadeDistance = 2;
                            base.fadePropagate();
                        }
                    } else if(te instanceof TileEntityMirrorBase && ((TileEntityMirrorBase)te).active && mc.playerController.getIsHittingBlock())
                    {

                        TileEntityMirrorBase base = (TileEntityMirrorBase)te;
                        if(base.fadeoutTime < TileEntityMirrorBase.FADEOUT_TIME - TileEntityMirrorBase.PROPAGATE_TIME)
                        {
                            base.fadeoutTime = TileEntityMirrorBase.FADEOUT_TIME;
                            base.fadePropagate = TileEntityMirrorBase.PROPAGATE_TIME;
                            base.fadeDistance = 2;
                            base.fadePropagate();
                        }
                    }
                    else
                    {
                        clickedPos = BlockPos.ORIGIN;
                    }
                }
                Iterator<Map.Entry<String, Integer>> ite = terminalPlacementCreationTimeout.entrySet().iterator();
                while(ite.hasNext())
                {
                    Map.Entry<String, Integer> e = ite.next();
                    e.setValue(e.getValue() - 1);
                    if(e.getValue() < 0)
                    {
                        ite.remove();
                    }
                }

                Iterator<Map.Entry<String, Integer>> ite1 = mirrorPlacementCreationTimeout.entrySet().iterator();
                while(ite1.hasNext())
                {
                    Map.Entry<String, Integer> e = ite1.next();
                    e.setValue(e.getValue() - 1);
                    if(e.getValue() < 0)
                    {
                        ite1.remove();
                    }
                }

                Iterator<Map.Entry<String, Integer>> ite2 = portalPlacementCreationTimeout.entrySet().iterator();
                while(ite2.hasNext())
                {
                    Map.Entry<String, Integer> e = ite2.next();
                    e.setValue(e.getValue() - 1);
                    if(e.getValue() < 0)
                    {
                        ite2.remove();
                    }
                }

                for(PacketPortalSync sync: syncList){
                    if(TileEntityPortal.tileEntityList.containsKey(sync.name)){
                        TileEntityPortal portal = TileEntityPortal.tileEntityList.get(sync.name);
                        portal.updateFromPacket(sync);
                        if(portalPlacements.containsKey(portal.name)){
                            portalPlacements.get(portal.name).updatePlacement();
                        }
                    }
                }

                syncList.clear();
            }
        }
    }

    @SubscribeEvent
    public void onModelRegistry(ModelRegistryEvent event)
    {
        ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(CFGlass.blockGlass), 0, new ModelResourceLocation("cfglass:block_glass", "inventory"));
        ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(CFGlass.blockGlass), 1, new ModelResourceLocation("cfglass:block_glass_projector", "inventory"));
        ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(CFGlass.blockGlass), 2, new ModelResourceLocation("cfglass:block_glass", "inventory"));

        ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(CFGlass.blockMirror), 0, new ModelResourceLocation("cfglass:block_mirror", "inventory"));
        ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(CFGlass.blockMirror), 1, new ModelResourceLocation("cfglass:block_mirror_master", "inventory"));

        ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(CFGlass.blockPortal), 0, new ModelResourceLocation("cfglass:block_portal", "inventory"));

        ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(CFGlass.blockGlassTerminal), 0, new ModelResourceLocation("cfglass:block_glass_terminal", "inventory"));
    }

    @SubscribeEvent
    public void onModelBake(ModelBakeEvent event)
    {
        event.getModelRegistry().putObject(new ModelResourceLocation("cfglass:block_glass_terminal", "normal"), new ModelEmpty(Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite("minecraft:blocks/obsidian")));
    }

    @SubscribeEvent
    public void onClientDisconnect(FMLNetworkEvent.ClientDisconnectionFromServerEvent event)
    {
        Minecraft.getMinecraft().addScheduledTask(this::disconnectFromServer);
    }

    @SubscribeEvent
    public void onWorldUnload(WorldEvent.Unload event)
    {
        if(event.getWorld().isRemote)
        {
            Minecraft mc = Minecraft.getMinecraft();
            if(mc.world != null)
            {
                Iterator<Map.Entry<String, BlockPos>> ite = terminalLocations.entrySet().iterator();
                while(ite.hasNext())
                {
                    Map.Entry<String, BlockPos> e = ite.next();
                    TileEntity te = mc.world.getTileEntity(e.getValue());
                    if(!(te instanceof TileEntityGlassTerminal))
                    {
                        ite.remove();
                    }
                }

                Iterator<Map.Entry<String, BlockPos>> iteL = mirrorLocations.entrySet().iterator();
                while(iteL.hasNext())
                {
                    Map.Entry<String, BlockPos> e = iteL.next();
                    TileEntity te = mc.world.getTileEntity(e.getValue());
                    if(!(te instanceof TileEntityMirrorMaster))
                    {
                        iteL.remove();
                    }
                }

                Iterator<Map.Entry<String, BlockPos>> iteP = portalLocations.entrySet().iterator();
                while(iteP.hasNext())
                {
                    Map.Entry<String, BlockPos> e = iteP.next();
                    TileEntity te = mc.world.getTileEntity(e.getValue());
                    if(!(te instanceof TileEntityMirrorMaster))
                    {
                        iteP.remove();
                    }
                }

                Iterator<Map.Entry<String, HashSet<TileEntityGlassBase>>> ite1 = activeGLASS.entrySet().iterator();
                while(ite1.hasNext())
                {
                    Map.Entry<String, HashSet<TileEntityGlassBase>> e = ite1.next();
                    e.getValue().removeIf(base -> base.getWorld() != mc.world || !base.active);
                    if(e.getValue().isEmpty())
                    {
                        ite1.remove();
                    }
                }


                Iterator<Map.Entry<String, HashSet<TileEntityMirrorBase>>> iteMIRROR = activeMIRROR.entrySet().iterator();
                while(iteMIRROR.hasNext())
                {
                    Map.Entry<String, HashSet<TileEntityMirrorBase>> e = iteMIRROR.next();
                    e.getValue().removeIf(base -> base.getWorld() != mc.world || !base.active);
                    if(e.getValue().isEmpty())
                    {
                        iteMIRROR.remove();
                    }
                }
            }
            else
            {
                terminalLocations.clear();
                activeGLASS.clear();
                terminalPlacements.forEach((k, v) -> WorldPortals.eventHandlerClient.renderGlobalProxy.releaseViewFrustum(v.getPair()));
                terminalPlacements.clear();
                terminalPlacementCreationTimeout.clear();

                mirrorLocations.clear();
                activeMIRROR.clear();
                mirrorPlacements.forEach((k, v) -> WorldPortals.eventHandlerClient.renderGlobalProxy.releaseViewFrustum(v.getPair()));
                mirrorPlacements.clear();
                mirrorPlacementCreationTimeout.clear();

                portalLocations.clear();
                portalPlacements.forEach((k, v) -> WorldPortals.eventHandlerClient.renderGlobalProxy.releaseViewFrustum(v.getPair()));
                portalPlacements.clear();
                portalPlacementCreationTimeout.clear();
            }
        }
    }

    public TerminalPlacement getTerminalPlacement(String channel) //this is called in render, only from active bases.
    {
        if(!terminalPlacementCreationTimeout.containsKey(channel))
        {
            if(terminalPlacements.containsKey(channel))
            {
                return terminalPlacements.get(channel);
            }
            if(terminalLocations.containsKey(channel))
            {
                Minecraft mc = Minecraft.getMinecraft();
                TileEntity te = mc.world.getTileEntity(terminalLocations.get(channel));
                if(te instanceof TileEntityGlassTerminal)
                {
                    TileEntityGlassTerminal terminal = (TileEntityGlassTerminal)te;

                    //we have the terminal. now find the projector
                    TileEntityGlassMaster master = null;
                    HashSet<TileEntityGlassBase> activeGlasses = activeGLASS.get(channel);
                    for(TileEntityGlassBase base : activeGlasses)
                    {
                        if(base.active && base.distance == 1 && base.channel.equalsIgnoreCase(channel) && base instanceof TileEntityGlassMaster)
                        {
                            master = (TileEntityGlassMaster)base;
                            break;
                        }
                    }

                    if(master != null) //we have the projector. Now generate the terminal placement
                    {
                        TerminalPlacement placement = new TerminalPlacement(mc.world, master, terminal, getActiveGlass(channel));
                        terminalPlacements.put(channel, placement);
                        return placement;
                    }
                }
                else
                {
                    terminalLocations.remove(channel);
                }
            }

            terminalPlacementCreationTimeout.put(channel, 13); //13 tick wait before trying again.
        }
        return null;
    }

    public MirrorPlacement getMirrorPlacement(String channel) //this is called in render, only from active bases.
    {
        if(!mirrorPlacementCreationTimeout.containsKey(channel))
        {
            if(mirrorPlacements.containsKey(channel))
            {
                return mirrorPlacements.get(channel);
            }
            if(mirrorLocations.containsKey(channel)) {
                Minecraft mc = Minecraft.getMinecraft();
                TileEntity te = mc.world.getTileEntity(mirrorLocations.get(channel));
                if (te instanceof TileEntityMirrorMaster) {
                    TileEntityMirrorMaster mirror = (TileEntityMirrorMaster) te;

                    //we have the terminal. now find the projector
                    TileEntityMirrorMaster master = null;
                    HashSet<TileEntityMirrorBase> activeMirrors = activeMIRROR.get(channel);
                    for (TileEntityMirrorBase base : activeMirrors) {
                        if (base.active && base.distance == 1 && base.channel.equalsIgnoreCase(channel) && base instanceof TileEntityMirrorMaster) {
                            master = (TileEntityMirrorMaster) base;
                            break;
                        }
                    }

                    if (master != null) //we have the projector. Now generate the terminal placement
                    {
                        MirrorPlacement placement = new MirrorPlacement(mc.world, master, master, getActiveMirror(channel));
                        mirrorPlacements.put(channel, placement);
                        return placement;
                    }
                } else {
                    mirrorLocations.remove(channel);
                }
            }

            mirrorPlacementCreationTimeout.put(channel, 13); //13 tick wait before trying again.
        }
        return null;
    }

    public PortalPlacement getPortalPlacement(String name) //this is called in render, only from active bases.
    {
        if(!portalPlacementCreationTimeout.containsKey(name))
        {
            if(portalPlacements.containsKey(name))
            {
                return portalPlacements.get(name);
            }
            if(portalLocations.containsKey(name)) {
                Minecraft mc = Minecraft.getMinecraft();
                TileEntity te = mc.world.getTileEntity(portalLocations.get(name));
                if (te instanceof TileEntityPortal) {
                    TileEntityPortal portal = (TileEntityPortal) te;
                    PortalPlacement placement = new PortalPlacement(portal);
                    EventHandlerClient.this.portalPlacements.put(name, placement);
                    CFGlass.LOGGER.logger.info("creating placement client");
                    return placement;


                } else {
                    portalLocations.remove(name);
                }
            }

            portalPlacementCreationTimeout.put(name, 13); //13 tick wait before trying again.
        }
        return null;
    }

    public HashSet<TileEntityGlassBase> getActiveGlass(String channel)
    {
        if(!channel.isEmpty() && activeGLASS.containsKey(channel))
        {
            return activeGLASS.get(channel);
        }
        return new HashSet<>();
    }

    public void addActiveGlass(TileEntityGlassBase base, String channel)
    {
        Minecraft.getMinecraft().addScheduledTask(() -> addGlass(base, channel));
    }

    private void addGlass(TileEntityGlassBase base, String channel)
    {
        HashSet<TileEntityGlassBase> bases = activeGLASS.computeIfAbsent(channel, v -> new HashSet<>());
        bases.add(base);

        if(terminalPlacements.containsKey(channel))
        {
            terminalPlacements.get(channel).addActiveGlass(base);
        }
    }

    public void removeActiveGlass(TileEntityGlassBase base, String channel)
    {
        Minecraft.getMinecraft().addScheduledTask(() -> removeGlass(base, channel));
    }

    private void removeGlass(TileEntityGlassBase base, String channel)
    {
        HashSet<TileEntityGlassBase> bases = activeGLASS.get(channel);
        if(bases != null)
        {
            bases.remove(base);
            if(bases.isEmpty())
            {
                activeGLASS.remove(channel);
                if(terminalPlacements.containsKey(channel))
                {
                    WorldPortals.eventHandlerClient.renderGlobalProxy.releaseViewFrustum(terminalPlacements.get(channel).getPair());
                }
                terminalPlacements.remove(channel);
                terminalPlacementCreationTimeout.remove(channel);
            }
            else if(terminalPlacements.containsKey(channel))
            {
                terminalPlacements.get(channel).removeActiveGlass(base);
            }
        }
    }

    public HashSet<TileEntityMirrorBase> getActiveMirror(String channel)
    {
        if(!channel.isEmpty() && activeMIRROR.containsKey(channel))
        {
            return activeMIRROR.get(channel);
        }
        return new HashSet<>();
    }


    public void addActiveMirror(TileEntityMirrorBase base, String channel)
    {
        Minecraft.getMinecraft().addScheduledTask(() -> addMirror(base, channel));
    }

    private void addMirror(TileEntityMirrorBase base, String channel)
    {
        HashSet<TileEntityMirrorBase> bases = activeMIRROR.computeIfAbsent(channel, v -> new HashSet<>());
        bases.add(base);

        if(mirrorPlacements.containsKey(channel))
        {
            mirrorPlacements.get(channel).addActiveMirror(base);
        }
    }

    public void removeActiveMirror(TileEntityMirrorBase base, String channel)
    {
        Minecraft.getMinecraft().addScheduledTask(() -> removeMirror(base, channel));
    }

    private void removeMirror(TileEntityMirrorBase base, String channel)
    {
        HashSet<TileEntityMirrorBase> bases = activeMIRROR.get(channel);
        if(bases != null)
        {
            bases.remove(base);
            if(bases.isEmpty())
            {
                activeMIRROR.remove(channel);
                if(mirrorPlacements.containsKey(channel))
                {
                    WorldPortals.eventHandlerClient.renderGlobalProxy.releaseViewFrustum(mirrorPlacements.get(channel).getPair());
                }
                mirrorPlacements.remove(channel);
                mirrorPlacementCreationTimeout.remove(channel);
            }
            else if(mirrorPlacements.containsKey(channel))
            {
                mirrorPlacements.get(channel).removeActiveMirror(base);
            }
        }
    }


    public void disconnectFromServer()
    {
        terminalLocations.clear();
        activeGLASS.clear();
        terminalPlacements.forEach((k, v) -> WorldPortals.eventHandlerClient.renderGlobalProxy.releaseViewFrustum(v.getPair()));
        terminalPlacements.clear();
        terminalPlacementCreationTimeout.clear();

        mirrorLocations.clear();
        activeMIRROR.clear();
        mirrorPlacements.forEach((k, v) -> WorldPortals.eventHandlerClient.renderGlobalProxy.releaseViewFrustum(v.getPair()));
        mirrorPlacements.clear();
        mirrorPlacementCreationTimeout.clear();

        portalLocations.clear();
        portalPlacements.forEach((k, v) -> WorldPortals.eventHandlerClient.renderGlobalProxy.releaseViewFrustum(v.getPair()));
        portalPlacements.clear();
        portalPlacementCreationTimeout.clear();
    }
}
