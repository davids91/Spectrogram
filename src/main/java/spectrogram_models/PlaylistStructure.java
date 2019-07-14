package spectrogram_models;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.json.simple.JSONObject;
import spectrogram_services.PlaylistHandler;

import java.util.Map;
import java.util.Random;

public enum PlaylistStructure {

    netLocation,
    lastSelectedVariant;


    public enum Validity {
        undefined, noexist, notAFile, invalidFormat, unknownFormat, emptyList, valid, encoded
    }

    public static String SONG_JSONID_id = "id";
    public static String SONG_JSONID_index = "index";
    public static String SONG_JSONID_location = "location";

    public String key() {return this.name();}
    public static boolean isNotControlKey(String key){
        try{
            return !PlaylistStructure.valueOf(PlaylistStructure.class, key).key().equals(key);
        } catch (Exception e) {
            return true;
        }
    }

    public static int lastUsedVariantSize(JsonObject playlistObject){
        return playlistObject.getAsJsonObject(playlistObject.getAsJsonPrimitive(lastSelectedVariant.key()).getAsString()).size();
    }

    public static int getValidVariants(JsonObject playlistObject){
        int ret = 0;
        for(Map.Entry<String, JsonElement> object : playlistObject.entrySet()){
            if(
                isNotControlKey(object.getKey()) /* TODO: Loading fails on an empty playlist */
                &&isValidVariant(object.getValue())
            )ret++;
        }
        return ret;
    }

    public static boolean isValidVariant(JsonElement variantElement){ /* TODO: make more robust */
        return (variantElement.isJsonNull() /* it is an empty variant */
                ||((!variantElement.isJsonPrimitive()) /* it's a controlKey */
                    && isValidSong(variantElement.getAsJsonObject().get("0").getAsJsonObject())) /* or the first song from the variant is valid*/
        );
    }

    public static boolean isValidSong(JsonObject SongObject){
        return SongObject.has(SONG_JSONID_id) && SongObject.has(SONG_JSONID_index) && SongObject.has(SONG_JSONID_location);
    }

    public static boolean isValidVariant(String variant, JsonObject playlistObject){
        return (isNotControlKey(variant))&&(playlistObject.has(variant));
    }

    public static String getLastSelectedVariant(JsonObject playlistObject) /* TODO: show success */ {
            return playlistObject.getAsJsonPrimitive(PlaylistStructure.lastSelectedVariant.key()).toString()
                    .replace("\"","");
    }

    public static void addVariant(String variant, JsonObject playlist){
        playlist.add(variant, new JsonObject());
    }

    public static String getSongPath(JsonObject song) throws NoSuchFieldException {
        if(song.isJsonObject() && song.has(SONG_JSONID_location)){
            return song.getAsJsonPrimitive(SONG_JSONID_location).getAsString().trim().replaceAll("\"","");
        }else throw new NoSuchFieldException("Unable to get song path, Song is not a JSON object!");
    }

    public static JsonObject getSongObjectInto(String path, JsonObject variantObject) throws NoSuchFieldException {
        Random random = new Random();
        int id = random.nextInt();
        boolean unique = false;

        JsonObject song = new JsonObject();
        while(!unique){ /* Iterate through the currently added songs */
            unique = true; /* Until a unique ID is found */
            id  = random.nextInt();
            for(Map.Entry<String, JsonElement>  songEntry : variantObject.entrySet()){
                if(!songEntry.getValue().isJsonObject()) throw new NoSuchFieldException("Song in playlist is not a JSON object!");
                if(id == songEntry.getValue().getAsJsonObject().getAsJsonPrimitive(SONG_JSONID_id).getAsInt()){
                    unique = false;
                }
            }
        }
        song.addProperty(SONG_JSONID_id , id);
        song.addProperty(SONG_JSONID_index , variantObject.size());
        song.addProperty(SONG_JSONID_location, path);
        return song;
    }
}
