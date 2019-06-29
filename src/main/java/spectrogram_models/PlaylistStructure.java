package spectrogram_models;

public enum PlaylistStructure {

    netLocation,
    lastSelectedVariant;

    public String key() {return this.name();}
    public static boolean isControlKey(String key){
        try{
            return PlaylistStructure.valueOf(PlaylistStructure.class,key).key().equals(key);
        } catch (Exception e) {
            return false;
        }
    }
}
