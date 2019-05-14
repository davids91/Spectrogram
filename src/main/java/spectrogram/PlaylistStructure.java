package spectrogram;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public enum PlaylistStructure {

    NETLOCATION("netLocation"),
    LASTSELECTEDVARIANT("lastSVar");

    public static boolean isControlKey(String key){ return getKeys().contains(key); }
    public static List<String> getKeys(){
        ArrayList<String> ret = new ArrayList();
        for(PlaylistStructure key : PlaylistStructure.values()) ret.add(key.key);
        return ret;
    }

    private String key;

    PlaylistStructure(String key) {
        this.key = key;
    }

    public String key(){return key;}
}
