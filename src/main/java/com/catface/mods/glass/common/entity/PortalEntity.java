package com.catface.mods.glass.common.entity;

import com.catface.mods.glass.common.CFGlass;
import com.catface.mods.glass.common.packet.PacketPortalSync;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagIntArray;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EntitySelectors;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.List;

public class PortalEntity extends Entity {

    public Vec3d dimensions = new Vec3d(1.0,2.0,0.05);
    public Vec3d portalRotation = new Vec3d(0.0,0.0,0.0);
    public Vec3d tpRotation = new Vec3d(0.0,0.0,0.0);
    public Vec3d tpLoc = new Vec3d(0.0,0.0,0.0);
    public boolean teleportsEntities = true;
    public float scale = 1.0f;

    public PortalEntity(World worldIn) {
        super(worldIn);
        this.noClip = true;
        this.setNoGravity(true);
    }

    @Override
    public void onEntityUpdate() {
        super.onEntityUpdate();
        this.collideWithNearbyEntities();
    }

    @Override
    public void onAddedToWorld() {
        super.onAddedToWorld();
//        if(!this.world.isRemote){
//            CFGlass.channel.sendToAll(new PacketPortalSync(this));
//        }
    }

    protected void collideWithNearbyEntities()
    {
        List<Entity> list = this.world.getEntitiesInAABBexcluding(this, this.getEntityBoundingBox(), EntitySelectors.getTeamCollisionPredicate(this));

        if (!list.isEmpty())
        {

            for (int l = 0; l < list.size(); ++l)
            {
                Entity entity = list.get(l);
                if(!(entity instanceof PortalEntity)){
                    this.applyEntityCollision(entity);
                }
            }
        }
    }

    @Override
    protected void doBlockCollisions() {

    }

    @Override
    public void setPosition(double x, double y, double z) {
        this.posX = x;
        this.posY = y;
        this.posZ = z;
        if (this.isAddedToWorld() && !this.world.isRemote) this.world.updateEntityWithOptionalForce(this, false); // Forge - Process chunk registration after moving.
        if(this.dimensions != null) {
            this.setEntityBoundingBox(new AxisAlignedBB(x - (double) dimensions.x / 2, y, z - (double) dimensions.z / 2, x + (double) dimensions.x / 2, y + (double) dimensions.y, z + (double) dimensions.z / 2));
        }
    }


    @Override
    protected void setSize(float width, float height) {

        if (this.isAddedToWorld() && !this.world.isRemote) this.world.updateEntityWithOptionalForce(this, false); // Forge - Process chunk registration after moving.

        this.setEntityBoundingBox(new AxisAlignedBB(this.posX - (double)dimensions.x/2, this.posY, this.posZ - (double)dimensions.z/2, this.posX + (double)dimensions.x/2, this.posY + (double)dimensions.y, this.posZ + (double)dimensions.z/2));
    }

    @Override
    public void applyEntityCollision(Entity entityIn) {
        if(this.teleportsEntities){
            entityIn.setPositionAndRotation(tpLoc.x,tpLoc.y,tpLoc.z, (float) tpRotation.x, (float) tpRotation.y);
        }
    }

    @Override
    protected void entityInit() {

    }



    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
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
        return super.writeToNBT(compound);
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);
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
        CFGlass.LOGGER.logger.info("reading NBT "+compound.toString());
    }

    @Override
    protected void readEntityFromNBT(NBTTagCompound compound) {

    }

    @Override
    protected void writeEntityToNBT(NBTTagCompound compound) {

    }

    @Override
    public String toString() {
        return "PortalEntity{" +
                "name="+getCustomNameTag()+
                ", dimensions=" + dimensions +
                ", portalRotation=" + portalRotation +
                ", tpRotation=" + tpRotation +
                ", tpLoc=" + tpLoc +
                ", teleportsEntities=" + teleportsEntities +
                ", scale=" + scale +
                ", posX=" + posX +
                ", posY=" + posY +
                ", posZ=" + posZ +
                '}';
    }
}
