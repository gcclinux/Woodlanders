package wagemaker.uk.network;

/**
 * Message sent when a player falls into a puddle.
 * Contains the player ID and puddle ID for validation and synchronization.
 */
public class PlayerFallMessage extends NetworkMessage {
    private static final long serialVersionUID = 1L;
    
    private String playerId;
    private String puddleId;
    
    /**
     * Default constructor for serialization.
     */
    public PlayerFallMessage() {
        super();
    }
    
    /**
     * Creates a new PlayerFallMessage.
     * @param senderId The ID of the sender (client)
     * @param playerId The ID of the player who fell
     * @param puddleId The ID of the puddle that triggered the fall
     */
    public PlayerFallMessage(String senderId, String playerId, String puddleId) {
        super(senderId);
        this.playerId = playerId;
        this.puddleId = puddleId;
    }
    
    @Override
    public MessageType getType() {
        return MessageType.PLAYER_FALL;
    }
    
    /**
     * Gets the ID of the player who fell.
     * @return The player ID
     */
    public String getPlayerId() {
        return playerId;
    }
    
    /**
     * Gets the ID of the puddle that triggered the fall.
     * @return The puddle ID
     */
    public String getPuddleId() {
        return puddleId;
    }
}
