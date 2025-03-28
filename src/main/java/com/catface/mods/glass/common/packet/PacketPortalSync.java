package com.catface.mods.glass.common.packet;

import com.catface.mods.glass.common.CFGlass;
import com.catface.mods.glass.common.entity.PortalEntity;
import com.catface.mods.glass.common.tileentity.TileEntityPortal;
import com.google.common.base.Predicate;
import io.netty.buffer.ByteBuf;
import me.ichun.mods.ichunutil.common.core.network.AbstractPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.List;
import java.util.UUID;

public class PacketPortalSync extends AbstractPacket {

    public String name;
    public BlockPos pos;
    public Vec3d loc;
    public Vec3d tpLoc;
    public Vec3d size;
    public Vec3d portalRot;
    public Vec3d tpRot;
    public float scale;
    public boolean tpsEnts;

    public PacketPortalSync(){

    }


    public PacketPortalSync(TileEntityPortal portal){
        name = portal.name;
        pos = portal.getPos();
        loc = portal.portalOffset;
        tpLoc = portal.tpLoc;
        size = portal.dimensions;
        portalRot = portal.portalRotation;
        tpRot = portal.tpRotation;
        tpsEnts = portal.teleportsEntities;
        scale = portal.scale;
    }

    @Override
    public void writeTo(ByteBuf buf) {
        ByteBufUtils.writeUTF8String(buf,name);
        buf.writeInt(pos.getX());
        buf.writeInt(pos.getY());
        buf.writeInt(pos.getZ());

        buf.writeDouble(loc.x);
        buf.writeDouble(loc.y);
        buf.writeDouble(loc.z);

        buf.writeDouble(tpLoc.x);
        buf.writeDouble(tpLoc.y);
        buf.writeDouble(tpLoc.z);

        buf.writeDouble(size.x);
        buf.writeDouble(size.y);
        buf.writeDouble(size.z);

        buf.writeDouble(portalRot.x);
        buf.writeDouble(portalRot.y);
        buf.writeDouble(portalRot.z);

        buf.writeDouble(tpRot.x);
        buf.writeDouble(tpRot.y);
        buf.writeDouble(tpRot.z);

        buf.writeBoolean(tpsEnts);
        buf.writeFloat(scale);
    }

    @Override
    public void readFrom(ByteBuf buf) {
        name = ByteBufUtils.readUTF8String(buf);
        pos = new BlockPos(buf.readInt(),buf.readInt(),buf.readInt());
        loc = new Vec3d(buf.readDouble(),buf.readDouble(),buf.readDouble());
        tpLoc = new Vec3d(buf.readDouble(),buf.readDouble(),buf.readDouble());
        size = new Vec3d(buf.readDouble(),buf.readDouble(),buf.readDouble());
        portalRot = new Vec3d(buf.readDouble(),buf.readDouble(),buf.readDouble());
        tpRot = new Vec3d(buf.readDouble(),buf.readDouble(),buf.readDouble());
        tpsEnts = buf.readBoolean();
        scale = buf.readFloat();
    }

    @Override
    public void execute(Side side, EntityPlayer entityPlayer) {
        if(side.isClient()){
            syncPortal(entityPlayer.world);
        }
    }

    @SideOnly(Side.CLIENT)
    public void syncPortal(World world){
        Minecraft.getMinecraft().addScheduledTask(()->{
            CFGlass.LOGGER.logger.info("recieving packet "+toString());
            CFGlass.eventHandlerClient.syncList.add(this);

        });
    }

    @Override
    public Side receivingSide() {
        return Side.CLIENT;
    }

    @Override
    public String toString() {
        return "PacketPortalSync{" +
                "name='" + name + '\'' +
                ", loc=" + loc +
                ", tpLoc=" + tpLoc +
                ", size=" + size +
                ", portalRot=" + portalRot +
                ", tpRot=" + tpRot +
                ", tpsEnts=" + tpsEnts +
                '}';
    }
}
