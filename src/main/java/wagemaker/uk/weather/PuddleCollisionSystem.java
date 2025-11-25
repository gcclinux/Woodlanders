package wagemaker.uk.weather;

import java.util.HashSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Manages collision detection between player and puddles.
 * Tracks triggered states to prevent repeated falls in the same puddle.
 */
public class PuddleCollisionSystem {
    
    private static final float FALL_ZONE_RADIUS = 12.0f;
    private static final int MAX_TRIGGERED_PUDDLES = 20; // Maximum size limit for triggered set
    
    private final Set<String> triggeredPuddleIds;
    private final Map<String, Boolean> playerInZone;
    private int lastPuddleCount = 0;
    
    /**
     * Creates a new puddle collision system.
     */
    public PuddleCollisionSystem() {
        this.triggeredPuddleIds = new HashSet<>();
        this.playerInZone = new HashMap<>();
    }
    
    /**
     * Checks if the player is within the fall zone of any active puddle.
     * Skips puddles that have already been triggered.
     * 
     * @param playerX Player's X position in world coordinates
     * @param playerY Player's Y position in world coordinates
     * @param activePuddles List of currently active puddles
     * @return Collision result indicating if a collision occurred and which puddle
     */
    public PuddleCollisionResult checkCollision(float playerX, float playerY, 
                                                List<WaterPuddle> activePuddles) {
        // Validate input
        if (activePuddles == null || activePuddles.isEmpty()) {
            return new PuddleCollisionResult(false, null);
        }
        
        // Validate player position
        if (Float.isNaN(playerX) || Float.isNaN(playerY) || 
            Float.isInfinite(playerX) || Float.isInfinite(playerY)) {
            System.err.println("Warning: Invalid player position in collision detection");
            return new PuddleCollisionResult(false, null);
        }
        
        // Check if puddle count decreased significantly - clear triggered states if so
        // Only clear if count decreased (puddles despawned), not if it increased
        int currentPuddleCount = activePuddles.size();
        if (lastPuddleCount > 0 && currentPuddleCount < lastPuddleCount) {
            clearAllTriggeredStates();
        }
        lastPuddleCount = currentPuddleCount;
        
        // Check each puddle for collision
        for (WaterPuddle puddle : activePuddles) {
            if (!puddle.isActive()) {
                continue;
            }
            
            // Skip if already triggered
            if (isTriggered(puddle)) {
                continue;
            }
            
            // Calculate distance and check collision
            float distance = calculateDistance(playerX, playerY, puddle);
            if (distance <= FALL_ZONE_RADIUS) {
                return new PuddleCollisionResult(true, puddle);
            }
        }
        
        return new PuddleCollisionResult(false, null);
    }
    
    /**
     * Marks a puddle as triggered for the player.
     * Prevents the same puddle from triggering multiple falls.
     * Implements size limit to prevent unbounded growth.
     * 
     * @param puddle The puddle to mark as triggered
     */
    public void markPuddleTriggered(WaterPuddle puddle) {
        if (puddle != null && puddle.getId() != null) {
            // Check if we've exceeded the maximum size limit
            if (triggeredPuddleIds.size() >= MAX_TRIGGERED_PUDDLES) {
                // Remove oldest entry (first element in the set)
                String oldestId = triggeredPuddleIds.iterator().next();
                triggeredPuddleIds.remove(oldestId);
                playerInZone.remove(oldestId);
                System.err.println("Warning: Triggered puddle set exceeded maximum size, removed oldest entry");
            }
            
            triggeredPuddleIds.add(puddle.getId());
            playerInZone.put(puddle.getId(), true);
        }
    }
    
    /**
     * Updates triggered states based on player position.
     * Resets triggered state when player exits a puddle's fall zone.
     * 
     * @param playerX Player's X position in world coordinates
     * @param playerY Player's Y position in world coordinates
     * @param activePuddles List of currently active puddles
     */
    public void updateTriggeredStates(float playerX, float playerY, 
                                      List<WaterPuddle> activePuddles) {
        if (activePuddles == null) {
            return;
        }
        
        // Check each triggered puddle
        for (WaterPuddle puddle : activePuddles) {
            if (!puddle.isActive()) {
                continue;
            }
            
            String puddleId = puddle.getId();
            if (puddleId == null || !triggeredPuddleIds.contains(puddleId)) {
                continue;
            }
            
            // Calculate distance
            float distance = calculateDistance(playerX, playerY, puddle);
            boolean currentlyInZone = distance <= FALL_ZONE_RADIUS;
            boolean wasInZone = playerInZone.getOrDefault(puddleId, false);
            
            // Update zone tracking
            playerInZone.put(puddleId, currentlyInZone);
            
            // Reset triggered state if player has exited the zone
            if (wasInZone && !currentlyInZone) {
                triggeredPuddleIds.remove(puddleId);
                playerInZone.remove(puddleId);
            }
        }
    }
    
    /**
     * Clears all triggered states and zone tracking.
     * Called when puddles despawn or the puddle system resets.
     */
    public void clearAllTriggeredStates() {
        triggeredPuddleIds.clear();
        playerInZone.clear();
    }
    
    /**
     * Checks if a puddle has been triggered.
     * 
     * @param puddle The puddle to check
     * @return True if the puddle has been triggered
     */
    private boolean isTriggered(WaterPuddle puddle) {
        return puddle != null && 
               puddle.getId() != null && 
               triggeredPuddleIds.contains(puddle.getId());
    }
    
    /**
     * Calculates the Euclidean distance from player center to puddle center.
     * 
     * @param playerX Player's X position in world coordinates
     * @param playerY Player's Y position in world coordinates
     * @param puddle The puddle to calculate distance to
     * @return Distance in pixels
     */
    private float calculateDistance(float playerX, float playerY, WaterPuddle puddle) {
        // Calculate puddle center
        float puddleCenterX = puddle.getX() + puddle.getWidth() / 2.0f;
        float puddleCenterY = puddle.getY() + puddle.getHeight() / 2.0f;
        
        // Calculate Euclidean distance
        float dx = playerX - puddleCenterX;
        float dy = playerY - puddleCenterY;
        
        return (float) Math.sqrt(dx * dx + dy * dy);
    }
}
