package wagemaker.uk.weather;

/**
 * Result of a puddle collision check.
 * Contains information about whether a collision occurred and which puddle was involved.
 */
public class PuddleCollisionResult {
    private final boolean hasCollision;
    private final WaterPuddle puddle;
    
    /**
     * Creates a new collision result.
     * 
     * @param hasCollision True if a collision was detected
     * @param puddle The puddle involved in the collision, or null if no collision
     */
    public PuddleCollisionResult(boolean hasCollision, WaterPuddle puddle) {
        this.hasCollision = hasCollision;
        this.puddle = puddle;
    }
    
    /**
     * Checks if a collision occurred.
     * 
     * @return True if a collision was detected
     */
    public boolean hasCollision() {
        return hasCollision;
    }
    
    /**
     * Gets the puddle involved in the collision.
     * 
     * @return The puddle, or null if no collision occurred
     */
    public WaterPuddle getPuddle() {
        return puddle;
    }
}
