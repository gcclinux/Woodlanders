package wagemaker.uk.network;

public class AppleTreeTransformMessage extends NetworkMessage {
    private static final long serialVersionUID = 1L;
    
    private String playerId;
    private String plantedAppleTreeId;
    private String appleTreeId;
    private float x;
    private float y;
    
    public AppleTreeTransformMessage() {
        super();
    }
    
    public AppleTreeTransformMessage(String playerId, String plantedAppleTreeId, String appleTreeId, float x, float y) {
        super(playerId);
        this.playerId = playerId;
        this.plantedAppleTreeId = plantedAppleTreeId;
        this.appleTreeId = appleTreeId;
        this.x = x;
        this.y = y;
    }
    
    @Override
    public MessageType getType() {
        return MessageType.APPLE_TREE_TRANSFORM;
    }
    
    public String getPlayerId() {
        return playerId;
    }
    
    public void setPlayerId(String playerId) {
        this.playerId = playerId;
    }
    
    public String getPlantedAppleTreeId() {
        return plantedAppleTreeId;
    }
    
    public void setPlantedAppleTreeId(String plantedAppleTreeId) {
        this.plantedAppleTreeId = plantedAppleTreeId;
    }
    
    public String getAppleTreeId() {
        return appleTreeId;
    }
    
    public void setAppleTreeId(String appleTreeId) {
        this.appleTreeId = appleTreeId;
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
}
