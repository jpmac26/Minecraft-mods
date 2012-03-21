package net.minecraft.src;

import net.minecraft.src.nbxlite.chunkproviders.*;

public abstract class WorldProvider
{
    public World worldObj;
    public WorldType terrainType;
    public WorldChunkManager worldChunkMgr;

    /**
     * States whether the Hell world provider is used(true) or if the normal world provider is used(false)
     */
    public boolean isHellWorld;
    public boolean hasNoSky;
    public float lightBrightnessTable[];

    /** 0 for normal world -1 for hell */
    public int worldType;
    private float colorsSunriseSunset[];

    public WorldProvider()
    {
        isHellWorld = false;
        hasNoSky = false;
        lightBrightnessTable = new float[16];
        worldType = 0;
        colorsSunriseSunset = new float[4];
    }

    public final void registerWorld(World par1World)
    {
        worldObj = par1World;
        terrainType = par1World.getWorldInfo().getTerrainType();
        registerWorldChunkManager();
        generateLightBrightnessTable();
    }

    /**
     * Creates the light to brightness table
     */
    protected void generateLightBrightnessTable()
    {
        float f = 0.0F;
        if(mod_noBiomesX.ClassicLight){
            f = 0.05F;
        }

        for (int i = 0; i <= 15; i++)
        {
            float f1 = 1.0F - (float)i / 15F;
            lightBrightnessTable[i] = ((1.0F - f1) / (f1 * 3F + 1.0F)) * (1.0F - f) + f;
        }
    }

    protected void registerWorldChunkManager()
    {
        if (worldObj.getWorldInfo().getTerrainType() == WorldType.field_48458_c)
        {
            worldChunkMgr = new WorldChunkManagerHell(BiomeGenBase.plains, 0.5F, 0.5F);
        }
        else
        {
            worldChunkMgr = new WorldChunkManager(worldObj);
        }
    }

    /**
     * Returns the chunk provider back for the world provider
     */
    public IChunkProvider getChunkProvider()
    {
        if (terrainType == WorldType.field_48458_c)
        {
            return new ChunkProviderFlat(worldObj, worldObj.getSeed(), worldObj.getWorldInfo().isMapFeaturesEnabled());
        }
        else
        {
            return new ChunkProviderGenerate2(worldObj, worldObj.getSeed(), worldObj.getWorldInfo().isMapFeaturesEnabled());
        }
    }

    /**
     * Will check if the x, z position specified is alright to be set as the map spawn point
     */
    public boolean canCoordinateBeSpawn(int par1, int par2)
    {
        if (mod_noBiomesX.Generator==0 && mod_noBiomesX.MapFeatures>=2){
            return true;
        }
        int i = worldObj.getFirstUncoveredBlock(par1, par2);
        if (mod_noBiomesX.Generator==2 || mod_noBiomesX.MapTheme==1){
            return i == Block.grass.blockID;
        }
        return i == Block.sand.blockID;
    }

    public float calculateCelestialAngle(long par1, float par3)
    {
        int i = (int)(par1 % 24000L);
        float f = ((float)i + par3) / 24000F - 0.25F;

        if (f < 0.0F)
        {
            f++;
        }

        if (f > 1.0F)
        {
            f--;
        }

        float f1 = f;
        f = 1.0F - (float)((Math.cos((double)f * Math.PI) + 1.0D) / 2D);
        f = f1 + (f - f1) / 3F;
        return f;
    }

    public boolean func_48567_d()
    {
        return true;
    }

    public boolean canRespawnHere()
    {
        return true;
    }

    public static WorldProvider getProviderForDimension(int par0)
    {
        if (par0 == -1)
        {
            return new WorldProviderHell();
        }

        if (par0 == 0)
        {
            return new WorldProviderSurface();
        }

        if (par0 == 1)
        {
            return new WorldProviderEnd();
        }
        else
        {
            return null;
        }
    }

    /**
     * Gets the hard-coded portal location to use when entering this dimension
     */
    public ChunkCoordinates getEntrancePortalLocation()
    {
        return null;
    }

    public int getAverageGroundLevel()
    {
        return terrainType != WorldType.field_48458_c ? 64 : 4;
    }

//FORGE COMPATIBILITY
    public String getSaveFolder(){
        return "FIXME";
    }
    public String getWelcomeMessage(){
        return "FIXME";
    }
    public String getDepartMessage(){
        return "FIXME";
    }
    public double getMovementFactor(){
        return 1.0D;
    }
}
