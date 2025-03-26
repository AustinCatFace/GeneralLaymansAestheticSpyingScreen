package com.catface.mods.glass.common.core;

import com.catface.mods.glass.common.block.BlockGlass;
import com.catface.mods.glass.common.block.BlockGlassTerminal;
import com.catface.mods.glass.common.block.BlockMirror;
import com.catface.mods.glass.common.block.BlockPortal;
import com.catface.mods.glass.common.item.ItemGlass;
import com.catface.mods.glass.common.CFGlass;
import com.catface.mods.glass.common.item.ItemMirror;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class EventHandlerServer
{
    @SubscribeEvent
    public void onRegisterBlock(RegistryEvent.Register<Block> event)
    {
        CFGlass.blockGlass = (new BlockGlass(Material.GLASS, false)).setRegistryName(CFGlass.MOD_ID, "block_glass").setUnlocalizedName("glass.block.glass").setHardness(0.8F);
        CFGlass.blockGlassTerminal = (new BlockGlassTerminal()).setRegistryName(CFGlass.MOD_ID, "block_glass_terminal").setUnlocalizedName("glass.block.glass_terminal").setHardness(50.0F).setResistance(2000.0F);
        CFGlass.blockMirror = (new BlockMirror(Material.GLASS, false)).setRegistryName(CFGlass.MOD_ID, "block_mirror").setUnlocalizedName("glass.block.mirror").setHardness(0.8F);
        CFGlass.blockPortal = (new BlockPortal(Material.GLASS)).setRegistryName(CFGlass.MOD_ID, "block_portal").setUnlocalizedName("glass.block.portal").setHardness(0.8F);
        event.getRegistry().register(CFGlass.blockGlass);
        event.getRegistry().register(CFGlass.blockGlassTerminal);
        event.getRegistry().register(CFGlass.blockMirror);
        event.getRegistry().register(CFGlass.blockPortal);
    }

    @SubscribeEvent
    public void onRegisterItem(RegistryEvent.Register<Item> event)
    {
        event.getRegistry().register(new ItemGlass(CFGlass.blockGlass).setRegistryName(CFGlass.blockGlass.getRegistryName()));
        event.getRegistry().register(new ItemBlock(CFGlass.blockGlassTerminal).setRegistryName(CFGlass.blockGlassTerminal.getRegistryName()));
        event.getRegistry().register(new ItemMirror(CFGlass.blockMirror).setRegistryName(CFGlass.blockMirror.getRegistryName()));
        event.getRegistry().register(new ItemMirror(CFGlass.blockPortal).setRegistryName(CFGlass.blockPortal.getRegistryName()));
    }

    @SubscribeEvent
    public void onRegisterSound(RegistryEvent.Register<SoundEvent> event)
    {
        CFGlass.soundAmb = new SoundEvent(new ResourceLocation("cfglass", "amb")).setRegistryName(new ResourceLocation("cfglass", "amb"));

        event.getRegistry().register(CFGlass.soundAmb);
    }
}
