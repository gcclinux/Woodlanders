package wagemaker.uk.network;

import java.io.Serializable;

/**
 * Represents the state of a planted apple tree in the multiplayer world.
 * Used for synchronizing planted apple trees across clients and for world persistence.
 */
public class PlantedAppleTreeState implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private String plantedAppleTreeId;
    private float x;
    private float y;
    private float growthTimer;
    
    public PlantedAppleTreeState() {
    }
    
    public PlantedAppleTreeState(String plantedAppleTreeId, float x, float y, float growthTimer) {
        this.plantedAppleTreeId = plantedAppleTreeId;
        this.x = x;
        this.y = y;
        this.growthTimer = growthTimer;
    }
    
    public String getPlantedAppleTreeId() {
        return plantedAppleTreeId;
    }
    
    public void setPlantedAppleTreeId(String plantedAppleTreeId) {
        this.plantedAppleTreeId = plantedAppleTreeId;
    }
    
    public float getX() {
        return x;
    }
    
    public void setX(float x) {
        this.x = x;
    }
    
    public float getY() {
        return y;
    }
    
    public void setY(float y) {
        this.y = y;
    }
    
    public float getGrowthTimer() {
        return growthTimer;
    }
    
    public void setGrowthTimer(float growthTimer) {
        this.growthTimer = growthTimer;
    }
}
