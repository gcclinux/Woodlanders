package wagemaker.uk.network;

public class AppleTreePlantMessage extends NetworkMessage {
    private static final long serialVersionUID = 1L;
    
    private String playerId;
    private String plantedAppleTreeId;
    private float x;
    private float y;
    
    public AppleTreePlantMessage() {
        super();
    }
    
    public AppleTreePlantMessage(String playerId, String plantedAppleTreeId, float x, float y) {
        super(playerId);
        this.playerId = playerId;
        this.plantedAppleTreeId = plantedAppleTreeId;
        this.x = x;
        this.y = y;
    }
    
    @Override
    public MessageType getType() {
        return MessageType.APPLE_TREE_PLANT;
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
