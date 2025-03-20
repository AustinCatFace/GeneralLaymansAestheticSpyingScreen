package com.catface.mods.glass.common;

import com.catface.mods.glass.common.command.CommandPortal;
import com.catface.mods.glass.common.core.EventHandlerServer;
import com.catface.mods.glass.common.core.ProxyCommon;
import com.catface.mods.glass.client.core.EventHandlerClient;
import me.ichun.mods.ichunutil.common.core.Logger;
import me.ichun.mods.ichunutil.common.core.network.PacketChannel;
import me.ichun.mods.ichunutil.common.iChunUtil;
import net.minecraft.block.Block;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;

@Mod(modid = CFGlass.MOD_ID, name = CFGlass.MOD_NAME,
        version = CFGlass.VERSION,
        guiFactory = iChunUtil.GUI_CONFIG_FACTORY,
        dependencies = "required-after:ichunutil@[" + iChunUtil.VERSION_MAJOR + ".1.0," + (iChunUtil.VERSION_MAJOR + 1) + ".0.0)",
        acceptableRemoteVersions = "[" + iChunUtil.VERSION_MAJOR + ".0.0," + iChunUtil.VERSION_MAJOR + ".1.0)",
        acceptedMinecraftVersions = iChunUtil.MC_VERSION_RANGE
)
public class CFGlass
{
    public static final String MOD_NAME = "CF Glass";
    public static final String MOD_ID = "cfglass";
    public static final String VERSION = iChunUtil.VERSION_MAJOR + ".0.0";

    @Mod.Instance
    public static CFGlass instance;

    @SidedProxy(clientSide = "com.catface.mods.glass.client.core.ProxyClient", serverSide = "com.catface.mods.glass.common.core.ProxyCommon")
    public static ProxyCommon proxy;

    public static final Logger LOGGER = Logger.createLogger(MOD_NAME);

    public static PacketChannel channel;

    public static EventHandlerClient eventHandlerClient;
    public static EventHandlerServer eventHandlerServer;

    public static Block blockGlass;
    public static Block blockGlassTerminal;
    public static Block blockMirror;

    public static SoundEvent soundAmb;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event)
    {
        proxy.preInit();

        //UpdateChecker.registerMod(new UpdateChecker.ModVersionInfo(MOD_NAME, iChunUtil.VERSION_OF_MC, VERSION, false));
    }

    @Mod.EventHandler
    public void serverStart(FMLServerStartingEvent event){
        event.registerServerCommand(new CommandPortal());
    }
}
