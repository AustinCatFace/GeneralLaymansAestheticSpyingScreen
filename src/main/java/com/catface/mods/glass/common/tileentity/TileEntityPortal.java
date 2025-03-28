package com.catface.mods.glass.common.tileentity;

import com.catface.mods.glass.common.CFGlass;
import com.catface.mods.glass.common.packet.PacketPortalSync;
import com.google.common.base.Predicate;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.multiplayer.ChunkProviderClient;
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
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.*;

public class TileEntityPortal extends TileEntity implements ITickable {

    public Vec3d dimensions;
    public Vec3d portalOffset;
    public Vec3d portalRotation;
    public Vec3d tpRotation;
    public Vec3d tpLoc;
    public boolean teleportsEntities;
    public float scale = 1.0f;
    public String name = null;


    public TileEntityPortal(){
        dimensions = new Vec3d(1.0,2.0,0.05);
        portalOffset = new Vec3d(0.0,0.0,0.0);
        portalRotation = new Vec3d(0.0,0.0,0.0);
        tpRotation = new Vec3d(0.0,0.0,0.0);
        tpLoc = new Vec3d(0.0,0.0,0.0);
        teleportsEntities = true;
        scale = 1.0f;
    }

    public static HashMap<String, BlockPos> portalList = new HashMap<>();

    public TileEntityPortal(String name){
        this();
        this.name = name;
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if(this.tpLoc.distanceTo(Vec3d.ZERO) == 0){
            this.tpLoc = new Vec3d(this.pos.getX(),this.pos.getY()+10,this.pos.getZ());
        }
        if(this.name == null){
            this.name = generateName();
        }

        if(this.world.isRemote) {
            CFGlass.eventHandlerClient.portalLocations.put(name, this.pos);
            //updatePortalChunk(true);
        }
    }

    @SideOnly(Side.CLIENT)
    public void updatePortalChunk(boolean load){
        BlockPos pos = new BlockPos(this.tpLoc);
        ChunkPos chunkPos = new ChunkPos(pos);
        ChunkProviderClient provider = (ChunkProviderClient) world.getChunkProvider();
        boolean f = provider.isChunkGeneratedAt(chunkPos.x, chunkPos.z);
        if(load && !f){
            provider.loadChunk(chunkPos.x,chunkPos.z);
        } else if(f && !load) {
            provider.unloadChunk(chunkPos.x, chunkPos.z);
        }
    }

    public String generateName(){
        BlockPos pos = this.getPos();
        if(pos == null){
            return "portal000";
        }
        return "portal"+"["+pos.getX()+","+pos.getY()+","+pos.getZ()+"]";
    }


    @Override
    public void onChunkUnload() {
        super.onChunkUnload();

        if(this.world.isRemote) {
            CFGlass.eventHandlerClient.portalLocations.remove(name);
            //updatePortalChunk(false);
        }
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
        float yaw = (float) (this.tpRotation.x+this.portalRotation.x);
        float pitch = (float) (this.tpRotation.y+this.portalRotation.y);
        entity.setPositionAndRotation(this.tpLoc.x,this.tpLoc.y,this.tpLoc.z, yaw , pitch );
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
        this.scale = packet.scale;
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
                this.name = name;
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

    public float getFacingYaw(){
        switch (EnumFacing.getFront(this.getBlockMetadata())){
            case EAST:
                return 270.0f;
            case WEST:
                return 90.0f;
            case SOUTH:
                return 180.0f;
            case NORTH:
            default:
                return 0.0f;
        }
    }

    public float getFacingPitch(){
        switch (EnumFacing.getFront(this.getBlockMetadata())){
            case UP:
                return 90.0f;
            case DOWN:
                return -90.0f;
            default:
                return 0.0f;
        }
    }

    public EnumFacing getFaceFromRotation(){
        if(Math.abs(tpRotation.y) > 45){
            return tpRotation.y>0 ? EnumFacing.UP : EnumFacing.DOWN;
        } else {
           double x = Math.abs(tpRotation.x%360);
           if(x <= 45 || x > 270){
               return EnumFacing.NORTH;
           } else if (x > 45 && x <=135){
               return EnumFacing.WEST;
           } else if (x > 135 && x <= 225){
               return EnumFacing.SOUTH;
           } else {
               return EnumFacing.EAST;
           }
        }
    }

    public static EnumFacing getFace(EnumFacing startFace, Vec3d rotation){
        return EnumFacing.getFacingFromVector((float) rotation.x, (float) rotation.y, (float) rotation.z);
    }

    public static BlockPos parsePosFromName(String name){
        BlockPos pos = BlockPos.ORIGIN;
        int startX = -1;
        int endX = -1;

        int startY = -1;
        int endY = -1;

        int startZ = -1;
        int endZ = -1;
        for(int i = 0;i<name.length();i++){
            char c = name.charAt(i);
            if(c=='['){
                startX = i+1;
            } else if (c == ']'){
                endZ = i;
            } else if (c == ','){
                if(endX < 0){
                    endX = i;
                    startY = i+1;
                } else if (endY < 0 ) {
                    endY = i;
                    startZ = i + 1;
                }

            }
        }

        try {
            int x = Integer.parseInt(name.substring(startX, endX));
            int y = Integer.parseInt(name.substring(startY, endY));
            int z = Integer.parseInt(name.substring(startZ, endZ));
            pos = new BlockPos(x,y,z);
        } catch (Exception e){

        }

        return pos;
    }
}
