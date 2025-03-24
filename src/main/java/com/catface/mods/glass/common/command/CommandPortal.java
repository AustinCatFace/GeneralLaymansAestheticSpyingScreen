package com.catface.mods.glass.common.command;

import com.catface.mods.glass.common.CFGlass;
import com.catface.mods.glass.common.entity.PortalEntity;
import com.catface.mods.glass.common.packet.PacketPortalSync;
import com.google.common.base.Predicate;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

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
        return "/portal create <name> <x> <y> <z> <tpX> <tpY> <tpZ> <width> <height> <portalYaw> <portalPitch> <tpYaw> <tpPitch> <tpsEntities?>";
    }

    public String getRemoveUsage(ICommandSender sender){
        return "/portal remove <name>";
    }

    public String getEditUsage(ICommandSender sender){
        return "/portal edit <name> " +
                "(POS <x> <y> <z>) ||" +
                "(TP <tpX> <tpY> <tpZ>) || " +
                "(SIZE <length> <width>) || " +
                "(ROT <portalYaw> <portalPitch>) || " +
                "(TPROT <tpYaw> <tpPitch>) || " +
                "(TPENTS <true/false>)";
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        if(args.length >= 2){
            String func = args[0];
            String name = args[1];
            List<PortalEntity> portals = sender.getEntityWorld().getEntities(PortalEntity.class, new Predicate<PortalEntity>() {
                @Override
                public boolean apply(@Nullable PortalEntity input) {
                    return input.getCustomNameTag().equals(args[1]);
                }
            });

            switch(func){
                case "create":
                    if(portals.size() >= 1){
                        throw new WrongUsageException("there's already a portal with name "+name);
                    }
                    createPortal(server,sender,args);
                    break;
                case "remove":

                    if(portals.size() < 1){
                        throw new WrongUsageException("no portals found with name "+name);
                    }
                    for(PortalEntity portal: portals){
                        portal.setDead();
                    }
                    break;
                case "edit":
                    if(portals.size() < 1){
                        throw new WrongUsageException("no portals found with name "+name);
                    }
                    editPortal(server, sender, args,portals.get(0));
                    break;
                default:
                    break;
            }
        } else {
            throw new WrongUsageException(getUsage(sender));
        }
    }

    public void createPortal(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {

        double[] valueArray = new double[]{     sender.getPositionVector().x, // portal loc (0-2)
                                                sender.getPositionVector().y,
                                                sender.getPositionVector().z,
                                                sender.getPositionVector().x, // tp loc (3-5)
                                                sender.getPositionVector().y+10,
                                                sender.getPositionVector().z,
                                                1.0, // width
                                                2.0, // height
                                                0.0, // portalYaw
                                                0.0, // portalPitch
                                                0.0, // tpYaw
                                                0.0};// tpPitch
        String name = args[1];
        for(int i=2;i<args.length-1;i++){
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


        PortalEntity portal = new PortalEntity(sender.getEntityWorld());
        portal.setCustomNameTag(name);
        syncPortalToArray(portal,valueArray,tpsEnts);
        sender.getEntityWorld().spawnEntity(portal);
        CFGlass.LOGGER.logger.info("creating portal "+portal.toString());
    }

    public void editPortal(MinecraftServer server, ICommandSender sender, String[] args,PortalEntity entity) throws CommandException {
        double[] valueArray = new double[]{     entity.getPositionVector().x, // portal loc (0-2)
                                                entity.getPositionVector().y,
                                                entity.getPositionVector().z,
                                                entity.getPositionVector().x, // tp loc (3-5)
                                                entity.getPositionVector().y+10,
                                                entity.getPositionVector().z,
                                                entity.dimensions.x, // width
                                                entity.dimensions.y, // height
                                                entity.portalRotation.x, // portalYaw
                                                entity.portalRotation.y, // portalPitch
                                                entity.tpRotation.x, // tpYaw
                                                entity.tpRotation.y};// tpPitch

        boolean tpEnts = entity.teleportsEntities;
        for(int i=0;i<args.length;i++){
            String s = args[i];
            switch(s){
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
                    if(args.length>i+2){
                        try{
                            double x = parseDouble(args[i+1]);
                            double y = parseDouble(args[i+2]);
                            valueArray[6] = x;
                            valueArray[7] = y;
                        } catch (Exception e){
                            throw new WrongUsageException(getEditUsage(sender));
                        }
                    } else {
                        throw new WrongUsageException(getEditUsage(sender));
                    }
                    break;
                case "ROT":
                    if(args.length>i+2){
                        try{
                            double x = parseDouble(args[i+1]);
                            double y = parseDouble(args[i+2]);
                            valueArray[8] = x;
                            valueArray[9] = y;
                        } catch (Exception e){
                            throw new WrongUsageException(getEditUsage(sender));
                        }
                    } else {
                        throw new WrongUsageException(getEditUsage(sender));
                    }
                    break;
                case "TPROT":
                    if(args.length>i+2){
                        try{
                            double x = parseDouble(args[i+1]);
                            double y = parseDouble(args[i+2]);
                            valueArray[10] = x;
                            valueArray[11] = y;
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
            }
        }
        CFGlass.LOGGER.logger.info("editing Portal "+entity.toString());
        syncPortalToArray(entity,valueArray,tpEnts);
        CFGlass.LOGGER.logger.info("post edit "+entity.toString());
        CFGlass.channel.sendToAll(new PacketPortalSync(entity));
    }


    public void syncPortalToArray(PortalEntity entity, double[] values,boolean tpsEnts){
        entity.setPosition(values[0],values[1],values[2]);
        entity.tpLoc = new Vec3d(values[3],values[4],values[5]);
        entity.dimensions = new Vec3d(values[6],values[7],0.05);
        entity.portalRotation = new Vec3d(values[8],values[9],0.0);
        entity.tpRotation = new Vec3d(values[10],values[11],0.0);
        entity.teleportsEntities = tpsEnts;
        entity.scale = 1.0f;

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

        } else if(l==2){

            tabs.add("<name>");

        } else {

            function = args[0];
            if(function.equals("create")){
                String[] fields = new String[]{"create", "<name>", "<x>", "<y>", "<z>", "<tpX>", "<tpY>", "<tpZ>", "<width>", "<height>", "<portalYaw>", "<portalPitch>", "<tpYaw>", "<tpPitch>", "<tpsEntities?>"};
                tabs.add(fields[l-1]);
            } else if(function.equals("edit")){
                HashMap<String,String[]> fields = new HashMap<>();
                fields.put("POS",new String[]{"<x>","<y>","<z>"});
                fields.put("TP",new String[]{"<tpX>","<tpY>","<tpZ>"});
                fields.put("SIZE",new String[]{"<length>","<width>"});
                fields.put("ROT",new String[]{"<portalYaw>","<portalPitch>"});
                fields.put("TPROT",new String[]{"<tpYaw>","<tpPitch>"});
                fields.put("TPENTS",new String[]{"<true/false>"});

                for(int i=1;i<=4;i++){
                    if(l-i>=0){
                        String positionArgument = args[l-i];
                        if(fields.containsKey(positionArgument)){
                            tabs.clear();
                            String[] values = fields.get(positionArgument);
                            tabs.addAll(Arrays.asList(values));
                        }
                    }

                }
                if(!tabs.isEmpty()){
                    tabs.addAll(fields.keySet());
                }
            }
        }

        return tabs;
    }
}
