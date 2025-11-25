package wagemaker.uk.weather;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import wagemaker.uk.client.PlayerConfig;

/**
 * Manages the fall-and-standup animation sequence for players.
 * Handles animation state progression, timing, and sprite frame extraction.
 */
public class FallAnimationSystem {
    
    /**
     * Animation states for the fall sequence.
     */
    public enum FallAnimationState {
        NONE,           // Not in fall sequence
        FALL,           // Frame 1: (256, 1280)
        STANDUP_1,      // Frame 2: (192, 1280)
        STANDUP_2,      // Frame 3: (128, 1280)
        STANDUP_3,      // Frame 4: (64, 1280)
        STANDUP_4,      // Frame 5: (0, 1280)
        COMPLETE        // Sequence finished
    }
    
    /**
     * Data class representing a single animation frame.
     */
    public static class FallAnimationFrame {
        private final int spriteX;
        private final int spriteY;
        private final float duration;
        
        public FallAnimationFrame(int spriteX, int spriteY, float duration) {
            this.spriteX = spriteX;
            this.spriteY = spriteY;
            this.duration = duration;
        }
        
        public int getSpriteX() {
            return spriteX;
        }
        
        public int getSpriteY() {
            return spriteY;
        }
        
        public float getDuration() {
            return duration;
        }
    }
    
    // Animation timing constants
    private static final float FRAME_DURATION = 0.2f; // 0.2 seconds per frame (1.1 seconds total)
    private static final int SPRITE_Y = 1280; // Y coordinate for all fall/standup frames
    private static final int FRAME_SIZE = 64; // 64x64 pixel frames
    private static final float MAX_SEQUENCE_DURATION = 5.0f; // 5 second timeout
    
    // Animation state tracking
    private FallAnimationState currentState;
    private float animationTimer;
    private float totalSequenceTime;
    private FallAnimationFrame[] frameSequence;
    
    /**
     * Creates a new fall animation system.
     */
    public FallAnimationSystem() {
        this.currentState = FallAnimationState.NONE;
        this.animationTimer = 0.0f;
        this.totalSequenceTime = 0.0f;
        initializeFrameSequence();
    }
    
    /**
     * Initializes the frame sequence with correct sprite coordinates.
     */
    private void initializeFrameSequence() {
        frameSequence = new FallAnimationFrame[5];
        
        // Frame 1: Fall (256, 1280)
        frameSequence[0] = new FallAnimationFrame(256, SPRITE_Y, FRAME_DURATION);
        
        // Frame 2: Standup1 (192, 1280)
        frameSequence[1] = new FallAnimationFrame(192, SPRITE_Y, FRAME_DURATION);
        
        // Frame 3: Standup2 (128, 1280)
        frameSequence[2] = new FallAnimationFrame(128, SPRITE_Y, FRAME_DURATION);
        
        // Frame 4: Standup3 (64, 1280)
        frameSequence[3] = new FallAnimationFrame(64, SPRITE_Y, FRAME_DURATION);
        
        // Frame 5: Standup4 (0, 1280)
        frameSequence[4] = new FallAnimationFrame(0, SPRITE_Y, FRAME_DURATION);
    }
    
    /**
     * Gets the current animation state.
     * 
     * @return The current animation state
     */
    public FallAnimationState getCurrentState() {
        return currentState;
    }
    
    /**
     * Gets the frame sequence.
     * 
     * @return The array of animation frames
     */
    public FallAnimationFrame[] getFrameSequence() {
        return frameSequence;
    }
    
    /**
     * Starts the fall animation sequence.
     * Resets the animation timer and sets the state to FALL.
     */
    public void startFallSequence() {
        currentState = FallAnimationState.FALL;
        animationTimer = 0.0f;
        totalSequenceTime = 0.0f;
    }
    
    /**
     * Updates the animation state based on elapsed time.
     * Advances frames every 0.8 seconds and preserves remainder time for smooth transitions.
     * Implements 5-second timeout protection to prevent stuck player.
     * 
     * @param deltaTime Time elapsed since last update in seconds
     */
    public void update(float deltaTime) {
        if (currentState == FallAnimationState.NONE || currentState == FallAnimationState.COMPLETE) {
            return; // No animation active
        }
        
        animationTimer += deltaTime;
        totalSequenceTime += deltaTime;
        
        // Check for timeout (5 seconds maximum)
        if (totalSequenceTime >= MAX_SEQUENCE_DURATION) {
            System.err.println("Warning: Fall animation sequence exceeded maximum duration (" + 
                             MAX_SEQUENCE_DURATION + "s), forcing completion");
            currentState = FallAnimationState.COMPLETE;
            return;
        }
        
        // Check if it's time to advance to the next frame
        if (animationTimer >= FRAME_DURATION) {
            // Preserve remainder time for smooth transitions
            animationTimer -= FRAME_DURATION;
            
            // Advance to next state
            switch (currentState) {
                case FALL:
                    currentState = FallAnimationState.STANDUP_1;
                    break;
                case STANDUP_1:
                    currentState = FallAnimationState.STANDUP_2;
                    break;
                case STANDUP_2:
                    currentState = FallAnimationState.STANDUP_3;
                    break;
                case STANDUP_3:
                    currentState = FallAnimationState.STANDUP_4;
                    break;
                case STANDUP_4:
                    currentState = FallAnimationState.COMPLETE;
                    break;
                default:
                    break;
            }
        }
    }
    
    /**
     * Gets the current fall animation frame from the player's sprite sheet.
     * Extracts a 64x64 TextureRegion based on the current animation state.
     * Uses the sprite sheet from PlayerConfig.
     * 
     * @return The current fall/standup frame, or null if not in fall sequence
     */
    public TextureRegion getCurrentFallFrame() {
        if (currentState == FallAnimationState.NONE || currentState == FallAnimationState.COMPLETE) {
            return null; // No fall frame to display
        }
        
        try {
            // Load PlayerConfig to get selected character sprite sheet
            PlayerConfig config = PlayerConfig.load();
            String characterFilename = config.getSelectedCharacter();
            
            // Use default if config is empty or invalid
            if (characterFilename == null || characterFilename.isEmpty()) {
                characterFilename = "boy_navy_start.png";
            }
            
            // Load the sprite sheet
            Texture spriteSheet = new Texture("sprites/player/" + characterFilename);
            
            // Get the frame coordinates based on current state
            FallAnimationFrame frame = getCurrentFrame();
            if (frame == null) {
                return null;
            }
            
            // Extract 64x64 TextureRegion from sprite sheet
            // LibGDX uses top-left origin, coordinates are (x, y) from top-left
            TextureRegion region = new TextureRegion(spriteSheet, 
                                                     frame.getSpriteX(), 
                                                     frame.getSpriteY(), 
                                                     FRAME_SIZE, 
                                                     FRAME_SIZE);
            
            return region;
            
        } catch (Exception e) {
            // Handle sprite sheet loading errors
            System.err.println("Error loading fall animation frame: " + e.getMessage());
            
            // Fall back to default sprite sheet
            try {
                Texture defaultSheet = new Texture("sprites/player/boy_navy_start.png");
                FallAnimationFrame frame = getCurrentFrame();
                if (frame != null) {
                    return new TextureRegion(defaultSheet, 
                                           frame.getSpriteX(), 
                                           frame.getSpriteY(), 
                                           FRAME_SIZE, 
                                           FRAME_SIZE);
                }
            } catch (Exception fallbackError) {
                System.err.println("Error loading fallback sprite sheet: " + fallbackError.getMessage());
            }
            
            return null;
        }
    }
    
    /**
     * Gets the current frame data based on animation state.
     * 
     * @return The current FallAnimationFrame, or null if not in fall sequence
     */
    private FallAnimationFrame getCurrentFrame() {
        switch (currentState) {
            case FALL:
                return frameSequence[0];
            case STANDUP_1:
                return frameSequence[1];
            case STANDUP_2:
                return frameSequence[2];
            case STANDUP_3:
                return frameSequence[3];
            case STANDUP_4:
                return frameSequence[4];
            default:
                return null;
        }
    }
    
    /**
     * Checks if the fall sequence is currently active.
     * 
     * @return True if the fall sequence is active (not NONE or COMPLETE)
     */
    public boolean isFallSequenceActive() {
        return currentState != FallAnimationState.NONE && 
               currentState != FallAnimationState.COMPLETE;
    }
    
    /**
     * Checks if the fall sequence has completed.
     * 
     * @return True if the fall sequence is in COMPLETE state
     */
    public boolean isFallSequenceComplete() {
        return currentState == FallAnimationState.COMPLETE;
    }
    
    /**
     * Resets the animation system to its initial state.
     * Clears the current state and resets the animation timer.
     */
    public void reset() {
        currentState = FallAnimationState.NONE;
        animationTimer = 0.0f;
        totalSequenceTime = 0.0f;
    }
}
