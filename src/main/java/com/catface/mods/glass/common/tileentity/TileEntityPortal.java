package com.catface.mods.glass.common.tileentity;

import com.catface.mods.glass.common.CFGlass;
import com.catface.mods.glass.common.packet.PacketPortalSync;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagDouble;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.HashMap;
import java.util.List;

public class TileEntityPortal extends TileEntity implements ITickable {

    public Vec3d dimensions = new Vec3d(1.0,2.0,0.05);
    public Vec3d portalOffset = new Vec3d(0.0,0.0,0.0);
    public Vec3d portalRotation = new Vec3d(0.0,0.0,0.0);
    public Vec3d tpRotation = new Vec3d(0.0,0.0,0.0);
    public Vec3d tpLoc = new Vec3d(0.0,0.0,0.0);
    public boolean teleportsEntities = true;
    public float scale = 1.0f;
    public String name = null;

    public static HashMap<String,TileEntityPortal> tileEntityList = new HashMap<>();

    public TileEntityPortal(){

    }

    public TileEntityPortal(String name){
        setName(name);
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if(this.tpLoc.distanceTo(Vec3d.ZERO) == 0){
            this.tpLoc = new Vec3d(this.pos.getX(),this.pos.getY()+10,this.pos.getZ());
        }
        if(this.name == null){
            CFGlass.LOGGER.logger.info(tileEntityList.keySet());
            setName("portal"+(tileEntityList.size()+1));
        }

        CFGlass.eventHandlerClient.portalLocations.put(name,this.pos);
        tileEntityList.put(this.name,this);
    }

    public String generateName(){
        for(int i=0;i<=tileEntityList.size();i++){
            if(!tileEntityList.containsKey("portal"+i)){
                return "portal"+i;
            }
        }

        return "portal0";
    }

    public void setName(String name){
        CFGlass.LOGGER.logger.info("setting name to "+name+" at "+this.pos);
        if(tileEntityList.containsKey(name) && tileEntityList.get(name) != this){
            CFGlass.LOGGER.logger.info("removing "+name+" at "+tileEntityList.get(name));
            tileEntityList.remove(name);
        }

        this.name = name;
        tileEntityList.put(this.name,this);
    }

    @Override
    public void onChunkUnload() {
        super.onChunkUnload();
        tileEntityList.remove(name);
        CFGlass.eventHandlerClient.portalLocations.remove(name);
    }

    @Override
    public void update() {
        if(teleportsEntities) {
            checkCollisions();
        }

        if(this.world.isRemote){
            displayBox();
        }
    }

    public AxisAlignedBB getHitbox(){
        Vec3d d = dimensions.rotateYaw((float) portalRotation.x).rotatePitch((float) portalRotation.y);
        return new AxisAlignedBB(this.pos.getX()+0.5-d.x/2,this.pos.getY(),this.pos.getZ()+0.5-d.z/2,this.pos.getX()+0.5+d.x/2,this.pos.getY()+d.y,this.pos.getZ()+0.5+d.z/2).offset(portalOffset);
    }

    public void displayBox(){
        AxisAlignedBB bb = getHitbox();
        world.spawnParticle(EnumParticleTypes.REDSTONE,bb.minX,bb.minY,bb.minZ,0,0,0,0);
        world.spawnParticle(EnumParticleTypes.REDSTONE,bb.maxX,bb.minY,bb.minZ,0,0,0,0);
        world.spawnParticle(EnumParticleTypes.REDSTONE,bb.minX,bb.maxY,bb.minZ,0,0,0,0);
        world.spawnParticle(EnumParticleTypes.REDSTONE,bb.minX,bb.minY,bb.maxZ,0,0,0,0);

        world.spawnParticle(EnumParticleTypes.REDSTONE,bb.maxX,bb.maxY,bb.maxZ,0,0,0,0);
        world.spawnParticle(EnumParticleTypes.REDSTONE,bb.minX,bb.maxY,bb.maxZ,0,0,0,0);
        world.spawnParticle(EnumParticleTypes.REDSTONE,bb.maxX,bb.minY,bb.maxZ,0,0,0,0);
        world.spawnParticle(EnumParticleTypes.REDSTONE,bb.maxX,bb.maxY,bb.minZ,0,0,0,0);
    }

    public void checkCollisions(){
        List<Entity> entities = this.world.getEntitiesWithinAABB(Entity.class,getHitbox());
        for(Entity ent: entities){
            applyEntityCollision(ent);
        }
    }

    public void applyEntityCollision(Entity entity){
        entity.setPositionAndRotation(this.tpLoc.x,this.tpLoc.y,this.tpLoc.z, (float) this.tpRotation.x, (float) this.tpRotation.y);
    }

    public void processClick(EntityPlayer player){

    }

    public void updateFromPacket(PacketPortalSync packet){
        this.portalOffset = packet.loc;
        this.tpLoc = packet.tpLoc;
        this.dimensions = packet.size;
        this.portalRotation = packet.portalRot;
        this.tpRotation = packet.tpRot;
        this.teleportsEntities = packet.tpsEnts;
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound)
    {
        super.writeToNBT(compound);
        NBTTagList offList = newDoubleNBTList(portalOffset.x,portalOffset.y,portalOffset.z);
        compound.setTag("offset",offList);

        NBTTagList dblList = newDoubleNBTList(dimensions.x,dimensions.y,dimensions.z);
        compound.setTag("dimensions",dblList);

        NBTTagList tpList = newDoubleNBTList(tpLoc.x,tpLoc.y,tpLoc.z);
        compound.setTag("tpLoc",tpList);

        NBTTagList rotList = newDoubleNBTList(portalRotation.x,portalRotation.y,portalRotation.z);
        compound.setTag("portalRotation",rotList);

        NBTTagList tpRotList = newDoubleNBTList(tpRotation.x,tpRotation.y,tpRotation.z);
        compound.setTag("tpRotation",tpRotList);

        compound.setBoolean("tpEnt",teleportsEntities);
        compound.setFloat("scale",scale);
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

    @Override
    public NBTTagCompound getUpdateTag()
    {
        return this.writeToNBT(new NBTTagCompound());
    }



    @Override
    public void readFromNBT(NBTTagCompound compound)
    {
        super.readFromNBT(compound);
        if(compound.hasKey("offset")){
            NBTTagList tags = compound.getTagList("offset",6);
            this.portalOffset = new Vec3d(tags.getDoubleAt(0),tags.getDoubleAt(1),tags.getDoubleAt(2));
        }

        if(compound.hasKey("dimensions")){
            NBTTagList tags = compound.getTagList("dimensions",6);
            this.dimensions = new Vec3d(tags.getDoubleAt(0),tags.getDoubleAt(1),tags.getDoubleAt(2));
        }

        if(compound.hasKey("tpLoc")){
            NBTTagList tags = compound.getTagList("tpLoc",6);
            this.tpLoc = new Vec3d(tags.getDoubleAt(0),tags.getDoubleAt(1),tags.getDoubleAt(2));
        }

        if(compound.hasKey("portalRotation")){
            NBTTagList tags = compound.getTagList("portalRotation",6);
            this.portalRotation = new Vec3d(tags.getDoubleAt(0),tags.getDoubleAt(1),tags.getDoubleAt(2));
        }

        if(compound.hasKey("tpRotation")){
            NBTTagList tags = compound.getTagList("tpRotation",6);
            this.tpRotation = new Vec3d(tags.getDoubleAt(0),tags.getDoubleAt(1),tags.getDoubleAt(2));
        }

        if(compound.hasKey("tpEnt")){
            this.teleportsEntities = compound.getBoolean("tpEnt");
        }

        if(compound.hasKey("scale")){
            this.scale = compound.getFloat("scale");
        }

        if(compound.hasKey("name")){
            String name = compound.getString("name");
            if(!name.equals(this.name)){
                this.setName(name);
            }
        }

    }

    @Override
    public SPacketUpdateTileEntity getUpdatePacket()
    {
        return new SPacketUpdateTileEntity(getPos(), 0, getUpdateTag());
    }

    @Override
    public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt)
    {
        CFGlass.LOGGER.logger.info("recieved data packet for "+this.name);
        onChunkUnload();
        readFromNBT(pkt.getNbtCompound());
        onLoad();
    }

    @Override
    public void handleUpdateTag(NBTTagCompound tag)
    {
        CFGlass.LOGGER.logger.info("handle update tag for "+this.name);
        onChunkUnload();
        readFromNBT(tag);
        onLoad();
    }

    @Override
    public AxisAlignedBB getRenderBoundingBox() {
        return super.getRenderBoundingBox().grow(dimensions.x,dimensions.y,dimensions.x);
    }

    @Override
    public String toString() {
        return "TileEntityPortal{" +
                "name='" + name + '\'' +
                ", dimensions=" + dimensions +
                ", portalOffset=" + portalOffset +
                ", portalRotation=" + portalRotation +
                ", tpRotation=" + tpRotation +
                ", tpLoc=" + tpLoc +
                ", teleportsEntities=" + teleportsEntities +
                ", scale=" + scale +
                ", pos=" + pos +
                '}';
    }

    public static EnumFacing getFace(EnumFacing startFace, Vec3d rotation){
        return EnumFacing.getFacingFromVector((float) rotation.x, (float) rotation.y, (float) rotation.z);
    }
}
