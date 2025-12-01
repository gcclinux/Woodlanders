package wagemaker.uk.birds;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.viewport.Viewport;

import java.util.Random;

/**
 * Central manager class that coordinates spawning, updating, and rendering of bird formations.
 */
public class BirdFormationManager {
    private BirdFormation activeFormation;
    private float spawnTimer;
    private float nextSpawnInterval;
    private Random random;
    private OrthographicCamera camera;
    private Viewport viewport;
    private SpawnBoundary lastSpawnBoundary;
    private Texture birdTexture1;
    private Texture birdTexture2;
    private Sound birdSound;
    private long birdSoundId = -1;
    private boolean isFadingOut = false;
    private float currentVolume = 1.0f;
    private float fadeOutTimer = 0f;
    
    private static final float MIN_SPAWN_INTERVAL = 60f; // 1 minute
    private static final float MAX_SPAWN_INTERVAL = 180f; // 3 minutes
    private static final float BIRD_SPEED = 100f; // pixels per second
    private static final float FADE_OUT_DURATION = 1.0f; // 1 second fade out

    public BirdFormationManager(OrthographicCamera camera, Viewport viewport) {
        this.camera = camera;
        this.viewport = viewport;
        this.random = new Random();
        this.activeFormation = null;
        this.lastSpawnBoundary = null;
    }

    /**
     * Constructor for testing that accepts a pre-loaded texture
     */
    public BirdFormationManager(OrthographicCamera camera, Viewport viewport, Texture texture) {
        this.camera = camera;
        this.viewport = viewport;
        this.random = new Random();
        this.activeFormation = null;
        this.lastSpawnBoundary = null;
        this.birdTexture1 = texture;
        this.birdTexture2 = texture; // Use same texture for both frames in tests
        
        // Initialize spawn timer with first random interval
        nextSpawnInterval = generateRandomInterval();
        spawnTimer = nextSpawnInterval;
    }

    public void initialize() {
        // Load bird textures for animation
        try {
            birdTexture1 = new Texture(Gdx.files.internal("sprites/bird.png"));
            birdTexture2 = new Texture(Gdx.files.internal("sprites/bird2.png"));
            System.out.println("[BIRDS] Bird system initialized successfully with 2 animation frames");
        } catch (Exception e) {
            System.err.println("[BIRDS] Failed to load bird textures: " + e.getMessage());
            birdTexture1 = null;
            birdTexture2 = null;
            return;
        }
        
        // Load bird sound
        try {
            birdSound = Gdx.audio.newSound(Gdx.files.internal("sound/birds.wav"));
            System.out.println("[BIRDS] Bird sound loaded successfully");
        } catch (Exception e) {
            System.err.println("[BIRDS] Failed to load bird sound: " + e.getMessage());
            birdSound = null;
        }
        
        // Initialize spawn timer with first random interval
        nextSpawnInterval = generateRandomInterval();
        spawnTimer = nextSpawnInterval;
        
        System.out.println("[BIRDS] First spawn in " + String.format("%.1f", nextSpawnInterval) + " seconds (spawn interval: " + MIN_SPAWN_INTERVAL + "-" + MAX_SPAWN_INTERVAL + "s)");
    }

    public void update(float deltaTime, float playerX, float playerY) {
        if (birdTexture1 == null || birdTexture2 == null) {
            return; // Bird system disabled if textures failed to load
        }
        
        // Update fade-out if in progress
        if (isFadingOut && birdSound != null && birdSoundId != -1) {
            fadeOutTimer += deltaTime;
            float fadeProgress = fadeOutTimer / FADE_OUT_DURATION;
            
            if (fadeProgress >= 1.0f) {
                // Fade complete, stop sound
                try {
                    birdSound.stop(birdSoundId);
                    System.out.println("[BIRDS] Bird sound stopped (fade complete)");
                } catch (Exception e) {
                    System.err.println("[BIRDS] Error stopping bird sound: " + e.getMessage());
                } finally {
                    birdSoundId = -1;
                    isFadingOut = false;
                    currentVolume = 1.0f;
                    fadeOutTimer = 0f;
                }
            } else {
                // Update volume during fade
                currentVolume = 1.0f - fadeProgress;
                try {
                    birdSound.setVolume(birdSoundId, currentVolume);
                } catch (Exception e) {
                    System.err.println("[BIRDS] Error setting bird sound volume: " + e.getMessage());
                }
            }
        }
        
        // Update active formation
        if (activeFormation != null) {
            activeFormation.update(deltaTime);
            
            // Get camera position for boundary checking
            float cameraX = camera.position.x - viewport.getWorldWidth() / 2;
            float cameraY = camera.position.y - viewport.getWorldHeight() / 2;
            
            // Check if formation has reached target
            if (activeFormation.hasReachedTarget(viewport.getWorldWidth(), viewport.getWorldHeight(), cameraX, cameraY)) {
                despawnFormation();
            }
        }
        
        // Update spawn timer
        if (activeFormation == null && !isFadingOut) {
            spawnTimer -= deltaTime;
            
            if (spawnTimer <= 0) {
                spawnFormation();
            }
        }
    }

    public void render(SpriteBatch batch) {
        if (activeFormation != null && birdTexture1 != null && birdTexture2 != null) {
            activeFormation.render(batch);
        }
    }

    public void dispose() {
        // Stop sound if playing
        if (birdSound != null && birdSoundId != -1) {
            try {
                birdSound.stop(birdSoundId);
            } catch (Exception e) {
                System.err.println("[BIRDS] Error stopping bird sound during dispose: " + e.getMessage());
            } finally {
                birdSoundId = -1;
            }
        }
        
        // Dispose sound resource
        if (birdSound != null) {
            try {
                birdSound.dispose();
            } catch (Exception e) {
                System.err.println("[BIRDS] Error disposing bird sound: " + e.getMessage());
            } finally {
                birdSound = null;
            }
        }
        
        if (activeFormation != null) {
            activeFormation.dispose();
        }
        if (birdTexture1 != null) {
            birdTexture1.dispose();
        }
        if (birdTexture2 != null) {
            birdTexture2.dispose();
        }
    }

    private void spawnFormation() {
        SpawnPoint spawnPoint = selectRandomSpawnPoint();
        Vector2 velocity = spawnPoint.direction.cpy().scl(BIRD_SPEED);
        
        activeFormation = new BirdFormation(spawnPoint, velocity, birdTexture1, birdTexture2);
        lastSpawnBoundary = spawnPoint.boundary;
        
        // Start bird sound
        if (birdSound != null && birdSoundId == -1) {
            try {
                birdSoundId = birdSound.loop();
                birdSound.setVolume(birdSoundId, 1.0f);
                currentVolume = 1.0f;
                isFadingOut = false;
                fadeOutTimer = 0f;
                System.out.println("[BIRDS] Bird sound started");
            } catch (Exception e) {
                System.err.println("[BIRDS] Error starting bird sound: " + e.getMessage());
                birdSoundId = -1; // Ensure sound ID remains in stopped state
            }
        }
        
        // Log spawn event
        float cameraX = camera.position.x;
        float cameraY = camera.position.y;
        System.out.println("[BIRDS] Formation spawned at " + spawnPoint.boundary + 
                         " boundary (world: x=" + String.format("%.1f", spawnPoint.x) + 
                         ", y=" + String.format("%.1f", spawnPoint.y) + 
                         ") [camera at x=" + String.format("%.1f", cameraX) + 
                         ", y=" + String.format("%.1f", cameraY) + 
                         "] flying " + getDirectionName(spawnPoint.direction));
        
        // Reset timer for next spawn
        nextSpawnInterval = generateRandomInterval();
        spawnTimer = nextSpawnInterval;
        
        System.out.println("[BIRDS] Next spawn in " + String.format("%.1f", nextSpawnInterval) + " seconds");
    }

    private void despawnFormation() {
        // Start fade-out for bird sound
        if (birdSound != null && birdSoundId != -1 && !isFadingOut) {
            isFadingOut = true;
            fadeOutTimer = 0f;
            System.out.println("[BIRDS] Bird sound fading out");
        }
        
        if (activeFormation != null) {
            activeFormation.dispose();
            activeFormation = null;
            System.out.println("[BIRDS] Formation despawned (reached target boundary)");
        }
        
        // Reset timer for next spawn
        nextSpawnInterval = generateRandomInterval();
        spawnTimer = nextSpawnInterval;
        
        System.out.println("[BIRDS] Next spawn in " + String.format("%.1f", nextSpawnInterval) + " seconds");
    }

    private float generateRandomInterval() {
        return MIN_SPAWN_INTERVAL + random.nextFloat() * (MAX_SPAWN_INTERVAL - MIN_SPAWN_INTERVAL);
    }

    private SpawnPoint selectRandomSpawnPoint() {
        float viewWidth = viewport.getWorldWidth();
        float viewHeight = viewport.getWorldHeight();
        
        // Get camera position to spawn birds relative to current view
        float cameraX = camera.position.x - viewWidth / 2;
        float cameraY = camera.position.y - viewHeight / 2;
        
        // Select random boundary
        SpawnBoundary[] boundaries = SpawnBoundary.values();
        SpawnBoundary boundary = boundaries[random.nextInt(boundaries.length)];
        
        // Ensure variation from last spawn
        if (lastSpawnBoundary != null && boundaries.length > 1) {
            int attempts = 0;
            while (boundary == lastSpawnBoundary && attempts < 10) {
                boundary = boundaries[random.nextInt(boundaries.length)];
                attempts++;
            }
        }
        
        float x, y;
        Vector2 direction;
        
        switch (boundary) {
            case TOP:
                x = cameraX + random.nextFloat() * viewWidth;
                y = cameraY + viewHeight;
                direction = new Vector2(0, -1); // Fly downward
                break;
            case BOTTOM:
                x = cameraX + random.nextFloat() * viewWidth;
                y = cameraY;
                direction = new Vector2(0, 1); // Fly upward
                break;
            case LEFT:
                x = cameraX;
                y = cameraY + random.nextFloat() * viewHeight;
                direction = new Vector2(1, 0); // Fly right
                break;
            case RIGHT:
                x = cameraX + viewWidth;
                y = cameraY + random.nextFloat() * viewHeight;
                direction = new Vector2(-1, 0); // Fly left
                break;
            default:
                x = cameraX;
                y = cameraY;
                direction = new Vector2(1, 0);
        }
        
        return new SpawnPoint(boundary, x, y, direction);
    }

    public BirdFormation getActiveFormation() {
        return activeFormation;
    }

    public float getSpawnTimer() {
        return spawnTimer;
    }

    public float getNextSpawnInterval() {
        return nextSpawnInterval;
    }
    
    private String getDirectionName(Vector2 direction) {
        if (direction.y > 0.5f) return "upward";
        if (direction.y < -0.5f) return "downward";
        if (direction.x > 0.5f) return "right";
        if (direction.x < -0.5f) return "left";
        return "unknown";
    }
}
