package com.catface.mods.glass.common.command;

import com.catface.mods.glass.common.CFGlass;
import com.catface.mods.glass.common.block.BlockPortal;
import com.catface.mods.glass.common.entity.PortalEntity;
import com.catface.mods.glass.common.packet.PacketPortalSync;
import com.catface.mods.glass.common.tileentity.TileEntityBoxPortal;
import com.catface.mods.glass.common.tileentity.TileEntityPortal;
import com.google.common.base.Predicate;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagDouble;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.*;

public class CommandPortal extends CommandBase {
    @Override
    public String getName() {
        return "portal";
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return "/portal <create/remove/edit> <params...>";
    }

    public String getCreateUsage(ICommandSender sender){
        return "/portal create <name> <x> <y> <z> <tpX> <tpY> <tpZ> <width> <height> <depth> <portalYaw> <portalPitch> <portalRoll> <tpYaw> <tpPitch> <tpRoll> <scale> <tpsEntities?> ";
    }

    public String getRemoveUsage(ICommandSender sender){
        return "/portal remove <name>";
    }

    public String getEditUsage(ICommandSender sender){
        return "/portal edit <name> " +
                "(POS <x> <y> <z>) ||" +
                "(TP <tpX> <tpY> <tpZ>) || " +
                "(SIZE <width> <height> <depth>) || " +
                "(ROT <portalYaw> <portalPitch> <portalRoll>) || " +
                "(TPROT <tpYaw> <tpPitch> <tpRoll>) || " +
                "(TPENTS <true/false>) || "+
                "(SCALE <scale>)";
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        if(args.length >= 2){
            String func = args[0];
            String name = args[1];
            BlockPos pos = TileEntityPortal.parsePosFromName(name);

            if(pos == BlockPos.ORIGIN){
                throw  new WrongUsageException("parsed name "+name+" got origin");
            }
            TileEntity te = sender.getEntityWorld().getTileEntity(pos);

            switch(func){
                case "create":

                    createPortalTE(server,sender,pos,args);
                    break;
                case "remove":

                    if(te instanceof TileEntityPortal){
                        sender.getEntityWorld().setBlockState(pos, Blocks.AIR.getDefaultState(),3);
                    } else {
                        throw new WrongUsageException("no portals found at "+pos);
                    }

                    break;
                case "edit":

                    if(te instanceof TileEntityPortal){
                        editPortalTE(server,sender,args, (TileEntityPortal) te);
                    } else {
                        throw new WrongUsageException("no portals found at "+pos);
                    }

                    break;
                default:
                    break;
            }
        } else {
            throw new WrongUsageException(getUsage(sender));
        }
    }


    public void createPortalTE(MinecraftServer server, ICommandSender sender,BlockPos pos, String[] args) throws CommandException {

        double[] valueArray = new double[]{     0, // portal loc (0-2)
                                                0,
                                                0,
                                                sender.getPositionVector().x, // tp loc (3-5)
                                                sender.getPositionVector().y+10,
                                                sender.getPositionVector().z,
                                                1.0, // width
                                                2.0, // height
                                                0.05, // depth
                                                0.0, // portalYaw
                                                0.0, // portalPitch
                                                0.0, // portalRoll
                                                0.0, // tpYaw
                                                0.0, // tpPitch
                                                0.0, // tpRoll
                                                1.0};// scale
        String name = args[1];
        for(int i=4;i<args.length-1;i++){
            try{
                double o = parseDouble(args[i]);
                valueArray[i-2] = o;
            } catch (Exception e){
                throw new WrongUsageException(getCreateUsage(sender));
            }
        }

        boolean tpsEnts = false;
        if(args.length >2){
            tpsEnts = parseBoolean(args[args.length-1]);
        }


        World world = sender.getEntityWorld();
        TileEntityPortal portal;

        world.setBlockState(pos, CFGlass.blockPortal.getDefaultState(),3);
        portal = new TileEntityPortal(name);

        syncPortalToArray(portal,valueArray,tpsEnts);
        world.setTileEntity(pos,portal);
        CFGlass.LOGGER.logger.info("creating portal "+portal.toString());
    }

    public void editPortalTE(MinecraftServer server, ICommandSender sender, String[] args,TileEntityPortal te) throws CommandException {
        double[] valueArray = new double[]{     te.portalOffset.x, // portal loc (0-2)
                                                te.portalOffset.y,
                                                te.portalOffset.z,
                                                te.tpLoc.x, // tp loc (3-5)
                                                te.tpLoc.y,
                                                te.tpLoc.z,
                                                te.dimensions.x, // width
                                                te.dimensions.y, // height
                                                te.dimensions.z,
                                                te.portalRotation.x, // portalYaw
                                                te.portalRotation.y, // portalPitch
                                                te.portalRotation.z,
                                                te.tpRotation.x, // tpYaw
                                                te.tpRotation.y, // tpPitch
                                                te.tpRotation.z,
                                                te.scale};// scale

        boolean tpEnts = te.teleportsEntities;
        for(int i=0;i<args.length;i++){
            String s = args[i];
            switch(s.toUpperCase()){
                case "POS":
                    if(args.length>i+3){
                        try{
                            double x = parseDouble(args[i+1]);
                            double y = parseDouble(args[i+2]);
                            double z = parseDouble(args[i+3]);
                            valueArray[0] = x;
                            valueArray[1] = y;
                            valueArray[2] = z;
                        } catch (Exception e){
                            throw new WrongUsageException(getEditUsage(sender));
                        }
                    } else {
                        throw new WrongUsageException(getEditUsage(sender));
                    }
                    break;
                case "TP":
                    if(args.length>i+3){
                        try{
                            double x = parseDouble(args[i+1]);
                            double y = parseDouble(args[i+2]);
                            double z = parseDouble(args[i+3]);
                            valueArray[3] = x;
                            valueArray[4] = y;
                            valueArray[5] = z;
                        } catch (Exception e){
                            throw new WrongUsageException(getEditUsage(sender));
                        }
                    } else {
                        throw new WrongUsageException(getEditUsage(sender));
                    }
                    break;
                case "SIZE":
                    if(args.length>i+3){
                        try{
                            double x = parseDouble(args[i+1]);
                            double y = parseDouble(args[i+2]);
                            double z = parseDouble(args[i+3]);
                            valueArray[6] = x;
                            valueArray[7] = y;
                            valueArray[8] = z;
                        } catch (Exception e){
                            throw new WrongUsageException(getEditUsage(sender));
                        }
                    } else {
                        throw new WrongUsageException(getEditUsage(sender));
                    }
                    break;
                case "ROT":
                    if(args.length>i+3){
                        try{
                            double x = parseDouble(args[i+1]);
                            double y = parseDouble(args[i+2]);
                            double z = parseDouble(args[i+3]);
                            valueArray[9] = x;
                            valueArray[10] = y;
                            valueArray[11] = z;
                        } catch (Exception e){
                            throw new WrongUsageException(getEditUsage(sender));
                        }
                    } else {
                        throw new WrongUsageException(getEditUsage(sender));
                    }
                    break;
                case "TPROT":
                    if(args.length>i+3){
                        try{
                            double x = parseDouble(args[i+1]);
                            double y = parseDouble(args[i+2]);
                            double z = parseDouble(args[i+3]);
                            valueArray[12] = x;
                            valueArray[13] = y;
                            valueArray[14] = z;
                        } catch (Exception e){
                            throw new WrongUsageException(getEditUsage(sender));
                        }
                    } else {
                        throw new WrongUsageException(getEditUsage(sender));
                    }
                    break;
                case "TPENTS":
                    if(args.length>i+1){
                        try{
                            tpEnts = parseBoolean(args[i+1]);
                        } catch (Exception e){
                            throw new WrongUsageException(getEditUsage(sender));
                        }
                    } else {
                        throw new WrongUsageException(getEditUsage(sender));
                    }
                    break;
                case "SCALE":
                    if(args.length>i+1){
                        try{
                            valueArray[15] = parseDouble(args[i+1],0.0,16.0);
                        } catch (Exception e){
                            throw new WrongUsageException(getEditUsage(sender));
                        }
                    } else {
                        throw new WrongUsageException(getEditUsage(sender));
                    }
                    break;
            }
        }
        syncPortalToArray(te,valueArray,tpEnts);
        CFGlass.channel.sendToAll(new PacketPortalSync(te));
    }



    public void syncPortalToArray(TileEntityPortal entity, double[] values,boolean tpsEnts){
        entity.portalOffset = new Vec3d(values[0],values[1],values[2]);
        entity.tpLoc = new Vec3d(values[3],values[4],values[5]);
        entity.dimensions = new Vec3d(values[6],values[7],values[8]);
        entity.portalRotation = new Vec3d(values[9],values[10],values[11]);
        entity.tpRotation = new Vec3d(values[12],values[13],values[14]);
        entity.teleportsEntities = tpsEnts;
        entity.scale = (float) values[15];
        CFGlass.LOGGER.logger.info("Command Portal -> syncing portal "+entity.toString());
    }

    @Override
    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos targetPos) {
        List<String> tabs = new ArrayList<>();
        int l = args.length;
        String function;

        if(l == 1){

            tabs.add("create");
            tabs.add("edit");
            tabs.add("remove");

        } else if(l>=2 && l<=4){
            int i = l-2;
            String[] poss = new String[]{"<x>", "<y>", "<z>"};
            tabs.add(poss[i]);
        } else {

            function = args[0];
            if(function.equals("create")){
                String[] fields = new String[]{"create", "<x>", "<y>", "<z>", "<offsetX>", "<offsetY>", "<offsetZ>", "<tpX>", "<tpY>", "<tpZ>", "<width>", "<height>", "<depth>", "<portalYaw>", "<portalPitch>", "<portalRoll>", "<tpYaw>", "<tpPitch>", "<tpRoll>", "<scale>", "<tpsEntities?>"};
                tabs.add(fields[l-1]);
            } else if(function.equals("edit")){
                HashMap<String,String[]> fields = new HashMap<>();
                fields.put("POS",new String[]{"<x>","<y>","<z>"});
                fields.put("TP",new String[]{"<tpX>","<tpY>","<tpZ>"});
                fields.put("SIZE",new String[]{"<width>","<height>","<depth>"});
                fields.put("ROT",new String[]{"<portalYaw>","<portalPitch>", "<portalRoll>"});
                fields.put("TPROT",new String[]{"<tpYaw>","<tpPitch>","<tpRoll>"});
                fields.put("TPENTS",new String[]{"<true/false>"});
                fields.put("SCALE",new String[]{"<scale>"});

                for(int i=0;i<=l;i++){
                    if(l-i>=0){
                        String positionArgument = args[l-i];
                        if(fields.containsKey(positionArgument)){
                            tabs.clear();
                            String[] values = fields.get(positionArgument);
                            tabs.addAll(Arrays.asList(values));
                            break;
                        }
                    }

                }
                if(tabs.isEmpty()){
                    tabs.addAll(fields.keySet());
                }
            }
        }

        return tabs;
    }

    public NBTTagCompound generateCompoundFromArgs(MinecraftServer server, ICommandSender sender,String name, double[] values, boolean tpsEnts){
        NBTTagCompound compound = new NBTTagCompound();

        NBTTagList offList = newDoubleNBTList(values[0],values[1],values[2]);
        compound.setTag("offset",offList);

        NBTTagList dblList = newDoubleNBTList(values[6],values[7],0.05);
        compound.setTag("dimensions",dblList);

        NBTTagList tpList = newDoubleNBTList(values[3],values[4],values[5]);
        compound.setTag("tpLoc",tpList);

        NBTTagList rotList = newDoubleNBTList(values[8],values[9],0.0);
        compound.setTag("portalRotation",rotList);

        NBTTagList tpRotList = newDoubleNBTList(values[10],values[11],0.0);
        compound.setTag("tpRotation",tpRotList);

        compound.setBoolean("tpEnt",tpsEnts);
        compound.setFloat("scale",1.0f);
        compound.setString("name",name);
        return compound;
    }

    protected NBTTagList newDoubleNBTList(double... numbers)
    {
        NBTTagList nbttaglist = new NBTTagList();

        for (double d0 : numbers)
        {
            nbttaglist.appendTag(new NBTTagDouble(d0));
        }

        return nbttaglist;
    }
}
