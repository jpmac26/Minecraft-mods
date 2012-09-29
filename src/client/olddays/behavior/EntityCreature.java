package net.minecraft.src;

import java.util.Random;

public abstract class EntityCreature extends EntityLiving
{
    public static boolean nopanic = false;
    public static boolean fastzombies = false;
    public static boolean jump = false;

    private PathEntity pathToEntity;

    /** The Entity this EntityCreature is set to attack. */
    protected Entity entityToAttack;

    /**
     * returns true if a creature has attacked recently only used for creepers and skeletons
     */
    protected boolean hasAttacked;

    /** Used to make a creature speed up and wander away when hit. */
    protected int fleeingTick;

    public EntityCreature(World par1World)
    {
        super(par1World);
        hasAttacked = false;
        fleeingTick = 0;
    }

    /**
     * Disables a mob's ability to move on its own while true.
     */
    protected boolean isMovementCeased()
    {
        return false;
    }

    protected void updateEntityActionState()
    {
        worldObj.theProfiler.startSection("ai");

        if (fleeingTick > 0)
        {
            if (nopanic){
                fleeingTick = 0;
            }else{
                fleeingTick--;
            }
        }

        hasAttacked = isMovementCeased();
        float f = 16F;

        if (entityToAttack == null)
        {
            entityToAttack = findPlayerToAttack();

            if (entityToAttack != null)
            {
                pathToEntity = worldObj.getPathEntityToEntity(this, entityToAttack, f, true, false, false, true);
            }
        }
        else if (entityToAttack.isEntityAlive())
        {
            float f1 = entityToAttack.getDistanceToEntity(this);

            if (canEntityBeSeen(entityToAttack))
            {
                attackEntity(entityToAttack, f1);
            }
            else
            {
                attackBlockedEntity(entityToAttack, f1);
            }
        }
        else
        {
            entityToAttack = null;
        }

        worldObj.theProfiler.endSection();

        if (!hasAttacked && entityToAttack != null && (pathToEntity == null || rand.nextInt(20) == 0))
        {
            pathToEntity = worldObj.getPathEntityToEntity(this, entityToAttack, f, true, false, false, true);
        }
        else if (!hasAttacked && (pathToEntity == null && rand.nextInt(180) == 0 || rand.nextInt(120) == 0 || (fleeingTick > 0 && !nopanic)) && entityAge < 100)
        {
            updateWanderPath();
        }

        int i = MathHelper.floor_double(boundingBox.minY + 0.5D);
        boolean flag = isInWater();
        boolean flag1 = handleLavaMovement();
        rotationPitch = 0.0F;

        if (pathToEntity == null || rand.nextInt(100) == 0)
        {
            super.updateEntityActionState();
            pathToEntity = null;
            return;
        }

        worldObj.theProfiler.startSection("followpath");
        Vec3 vec3 = pathToEntity.getPosition(this);

        for (double d = width * 2.0F; vec3 != null && vec3.squareDistanceTo(posX, vec3.yCoord, posZ) < d * d;)
        {
            pathToEntity.incrementPathIndex();

            if (pathToEntity.isFinished())
            {
                vec3 = null;
                pathToEntity = null;
            }
            else
            {
                vec3 = pathToEntity.getPosition(this);
            }
        }

        isJumping = false;

        if (vec3 != null)
        {
            double d1 = vec3.xCoord - posX;
            double d2 = vec3.zCoord - posZ;
            double d3 = vec3.yCoord - (double)i;
            float f2 = (float)((Math.atan2(d2, d1) * 180D) / Math.PI) - 90F;
            float f3 = MathHelper.wrapAngleTo180_float(f2 - rotationYaw);
            moveForward = moveSpeed;

            if (f3 > 30F)
            {
                f3 = 30F;
            }

            if (f3 < -30F)
            {
                f3 = -30F;
            }

            rotationYaw += f3;

            if (hasAttacked && entityToAttack != null)
            {
                double d4 = entityToAttack.posX - posX;
                double d5 = entityToAttack.posZ - posZ;
                float f5 = rotationYaw;
                rotationYaw = (float)((Math.atan2(d5, d4) * 180D) / Math.PI) - 90F;
                float f4 = (((f5 - rotationYaw) + 90F) * (float)Math.PI) / 180F;
                moveStrafing = -MathHelper.sin(f4) * moveForward * 1.0F;
                moveForward = MathHelper.cos(f4) * moveForward * 1.0F;
            }

            if (d3 > 0.0D)
            {
                isJumping = true;
            }
        }

        if (entityToAttack != null)
        {
            faceEntity(entityToAttack, 30F, 30F);
        }

        if (isCollidedHorizontally && (!hasPath() || jump))
        {
            isJumping = true;
        }

        if (rand.nextFloat() < 0.8F && (flag || flag1))
        {
            isJumping = true;
        }

        worldObj.theProfiler.endSection();
    }

    /**
     * Time remaining during which the Animal is sped up and flees.
     */
    protected void updateWanderPath()
    {
        worldObj.theProfiler.startSection("stroll");
        boolean flag = false;
        int i = -1;
        int j = -1;
        int k = -1;
        float f = -99999F;

        for (int l = 0; l < 10; l++)
        {
            int i1 = MathHelper.floor_double((posX + (double)rand.nextInt(13)) - 6D);
            int j1 = MathHelper.floor_double((posY + (double)rand.nextInt(7)) - 3D);
            int k1 = MathHelper.floor_double((posZ + (double)rand.nextInt(13)) - 6D);
            float f1 = getBlockPathWeight(i1, j1, k1);

            if (f1 > f)
            {
                f = f1;
                i = i1;
                j = j1;
                k = k1;
                flag = true;
            }
        }

        if (flag)
        {
            pathToEntity = worldObj.getEntityPathToXYZ(this, i, j, k, 10F, true, false, false, true);
        }

        worldObj.theProfiler.endSection();
    }

    /**
     * Basic mob attack. Default to touch of death in EntityCreature. Overridden by each mob to define their attack.
     */
    protected void attackEntity(Entity entity, float f)
    {
    }

    /**
     * Used when an entity is close enough to attack but cannot be seen (Creeper de-fuse)
     */
    protected void attackBlockedEntity(Entity entity, float f)
    {
    }

    /**
     * Takes a coordinate in and returns a weight to determine how likely this creature will try to path to the block.
     * Args: x, y, z
     */
    public float getBlockPathWeight(int par1, int par2, int par3)
    {
        return 0.0F;
    }

    /**
     * Finds the closest player within 16 blocks to attack, or null if this Entity isn't interested in attacking
     * (Animals, Spiders at day, peaceful PigZombies).
     */
    protected Entity findPlayerToAttack()
    {
        return null;
    }

    /**
     * Checks if the entity's current position is a valid location to spawn this entity.
     */
    public boolean getCanSpawnHere()
    {
        int i = MathHelper.floor_double(posX);
        int j = MathHelper.floor_double(boundingBox.minY);
        int k = MathHelper.floor_double(posZ);
        return super.getCanSpawnHere() && getBlockPathWeight(i, j, k) >= 0.0F;
    }

    /**
     * Returns true if entity has a path to follow
     */
    public boolean hasPath()
    {
        return pathToEntity != null;
    }

    /**
     * sets the Entities walk path in EntityCreature
     */
    public void setPathToEntity(PathEntity par1PathEntity)
    {
        pathToEntity = par1PathEntity;
    }

    /**
     * Returns current entities target
     */
    public Entity getEntityToAttack()
    {
        return entityToAttack;
    }

    /**
     * Sets the entity which is to be attacked.
     */
    public void setTarget(Entity par1Entity)
    {
        entityToAttack = par1Entity;
    }

    /**
     * This method return a value to be applyed directly to entity speed, this factor is less than 1 when a slowdown
     * potion effect is applyed, more than 1 when a haste potion effect is applyed and 2 for fleeing entities.
     */
    protected float getSpeedModifier()
    {
        if (isAIEnabled() && !newai)
        {
            if (this instanceof EntityZombie && !(this instanceof EntityPigZombie)){
                if (fastzombies){
                    return 4.34782608696F;
                }
                return 2.17391304348F;
            }
            if (this instanceof EntityWolf){
                return 3.6F;
            }
            if (this instanceof EntitySkeleton){
                return 2.8F;
            }
        }
        if (isAIEnabled() && newai())
        {
            if (this instanceof EntityZombie && !(this instanceof EntityPigZombie) && fastzombies){
                return 2.0F;
            }
            return 1.0F;
        }
        float f = super.getSpeedModifier();

        if (fleeingTick > 0 && !nopanic)
        {
            f *= 2.0F;
        }

        return f;
    }
}
