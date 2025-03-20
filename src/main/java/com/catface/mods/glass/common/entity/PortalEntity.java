package com.catface.mods.glass.common.entity;

import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagIntArray;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class PortalEntity extends Entity {

    public Vec3d dimensions = new Vec3d(1.0,2.0,0.05);
    public Vec3d portalRotation = new Vec3d(0.0,0.0,0.0);
    public Vec3d tpRotation = new Vec3d(0.0,0.0,0.0);
    public Vec3d tpLoc = new Vec3d(0.0,0.0,0.0);
    public boolean teleportsEntities = false;
    public float scale = 1.0f;

    public PortalEntity(World worldIn) {
        super(worldIn);
        this.noClip = true;
        this.setNoGravity(true);
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
        if(this.teleportsEntities && this.getCollisionBoundingBox().intersects(entityIn.posX,entityIn.posY,entityIn.posZ,entityIn.posX,entityIn.posY+entityIn.height/2,entityIn.posZ)){
            entityIn.setPositionAndRotation(tpLoc.x,tpLoc.y,tpLoc.z, (float) tpRotation.x, (float) tpRotation.y);
        }
    }

    @Override
    protected void entityInit() {

    }

    @Override
    protected void readEntityFromNBT(NBTTagCompound compound) {
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
    }

    @Override
    protected void writeEntityToNBT(NBTTagCompound compound) {
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
    }
}
