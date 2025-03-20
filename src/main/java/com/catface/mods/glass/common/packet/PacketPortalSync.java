package com.catface.mods.glass.common.packet;

import com.catface.mods.glass.common.entity.PortalEntity;
import com.google.common.base.Predicate;
import io.netty.buffer.ByteBuf;
import me.ichun.mods.ichunutil.common.core.network.AbstractPacket;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.relauncher.Side;

import javax.annotation.Nullable;
import java.util.List;
import java.util.UUID;

public class PacketPortalSync extends AbstractPacket {

    public String name;
    public Vec3d loc;
    public Vec3d tpLoc;
    public Vec3d size;
    public Vec3d portalRot;
    public Vec3d tpRot;
    public boolean tpsEnts;

    public PacketPortalSync(){

    }

    public PacketPortalSync(PortalEntity entity){
        name = entity.getCustomNameTag();
        loc = entity.getPositionVector();
        tpLoc = entity.tpLoc;
        size = entity.dimensions;
        portalRot = entity.portalRotation;
        tpRot = entity.tpRotation;
        tpsEnts = entity.teleportsEntities;
    }

    @Override
    public void writeTo(ByteBuf buf) {
        ByteBufUtils.writeUTF8String(buf,name);
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
    }

    @Override
    public void readFrom(ByteBuf buf) {
        name = ByteBufUtils.readUTF8String(buf);
        loc = new Vec3d(buf.readDouble(),buf.readDouble(),buf.readDouble());
        tpLoc = new Vec3d(buf.readDouble(),buf.readDouble(),buf.readDouble());
        size = new Vec3d(buf.readDouble(),buf.readDouble(),buf.readDouble());
        portalRot = new Vec3d(buf.readDouble(),buf.readDouble(),buf.readDouble());
        tpRot = new Vec3d(buf.readDouble(),buf.readDouble(),buf.readDouble());
        tpsEnts = buf.readBoolean();
    }

    @Override
    public void execute(Side side, EntityPlayer entityPlayer) {
        List<PortalEntity> portalList = entityPlayer.getEntityWorld().getEntities(PortalEntity.class, new Predicate<PortalEntity>() {
            @Override
            public boolean apply(@Nullable PortalEntity input) {
                return input.getCustomNameTag().equals(name);
            }
        });

        if(portalList.size() > 0){
            PortalEntity ent = portalList.get(0);
            ent.setPosition(loc.x,loc.y,loc.z);
            ent.tpLoc = this.tpLoc;
            ent.dimensions = this.size;
            ent.portalRotation = this.portalRot;
            ent.tpRotation = this.tpRot;
            ent.teleportsEntities = this.tpsEnts;
        }
    }

    @Override
    public Side receivingSide() {
        return Side.CLIENT;
    }
}
