package spectrogram_models;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.io.InvalidObjectException;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.io.File;
import java.util.stream.Collectors;

public enum PlaylistStructure {

    netLocation,
    lastSelectedVariant;


    public enum Validity {
        undefined, noexist, notAFile, invalidFormat, unknownFormat, emptyList, valid, encoded
    }

    public static String SONG_JSONID_id = "id";
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
                isNotControlKey(object.getKey())
                &&isValidVariant(object.getValue())
            )ret++;
        }
        return ret;
    }

    public static boolean isValidVariant(JsonElement variantElement){
        return (variantElement.isJsonNull() /* it is an empty variant */
                ||((!variantElement.isJsonPrimitive()) /* it's a controlKey */
                    && variantElement.getAsJsonObject().has("0")
                    && variantElement.getAsJsonObject().get("0").isJsonObject()
                    && isValidSong(variantElement.getAsJsonObject().get("0").getAsJsonObject())) /* or the first song from the variant is valid*/
        );
    }

    public static boolean isValidSong(JsonElement songElement){
        if(songElement.isJsonObject()){
            return isValidSong(songElement.getAsJsonObject());
        }else return false; /* JsonElement is not a jsonObject*/
    }

    public static boolean isValidSong(JsonObject SongObject){
        return SongObject.has(SONG_JSONID_id) && SongObject.has(SONG_JSONID_location);
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

    public static File getSongAsFile(JsonElement songElement) throws InvalidObjectException {
        if(isValidSong(songElement)){
            return getSongAsFile(songElement.getAsJsonObject());
        }else throw new InvalidObjectException("Unable to get song path, Song is not a JSON object!");
    }

    public static File getSongAsFile(JsonObject song) throws InvalidObjectException {
        if(isValidSong(song)){
            return new File(getSongPath(song));
        }else throw new InvalidObjectException("Unable to get song path, Song is not a JSON object!");
    }

    public static String getSongPath(JsonObject song) throws InvalidObjectException {
        if(isValidSong(song)){
            return song.getAsJsonPrimitive(SONG_JSONID_location).getAsString().trim().replaceAll("\"","");
        }else throw new InvalidObjectException("Unable to get song path, Song is not a JSON object!");
    }

    public static int getSongIndex(JsonElement song, JsonObject variantObj) throws InvalidObjectException {
        if(isValidSong(song)){
            return getSongIndex(song.getAsJsonObject(),variantObj);
        }else throw new InvalidObjectException("Unable to get song path, Song is not a JSON object!");
    }

    public static int getSongIndex(JsonObject song, JsonObject variantObj) throws InvalidObjectException {
        if(isValidSong(song)&&variantObj.isJsonObject()){
            int index = 0;
            List<Map.Entry<String, JsonElement>> songs = getSorted(variantObj);
            for(Map.Entry<String, JsonElement> songEntry : songs) {
                if(getSongID(song) == getSongID(songEntry.getValue())){
                    break;
                }
                index++;
            }
            return index;
        }else throw new InvalidObjectException("Unable to get song path, Song is not a JSON object!");
    }

    public static List<Map.Entry<String, JsonElement>>  getSorted(JsonObject variantObject){
        return variantObject.entrySet() /* TODO: Assert for integer keys */
                .stream().sorted(Comparator.comparingInt(e -> Integer.parseInt(e.getKey())))
                .collect(Collectors.toList()); /* TODO: Check for duplicates and continuity */
    }

    public static int getSongID(JsonElement song) throws  InvalidObjectException {
        if(isValidSong(song)){
            return getSongID(song.getAsJsonObject());
        }else throw new InvalidObjectException("Unable to determine ID, as it is not a valid song!");
    }

    public static int getSongID(JsonObject song) throws InvalidObjectException {
        if(isValidSong(song)){
            return song.getAsJsonPrimitive(SONG_JSONID_id).getAsInt();
        }else throw new InvalidObjectException("Unable to determine ID, as it is not a valid song!");
    }

    public static JsonObject getSongAtIndex(JsonObject variantObject, int index) throws InvalidObjectException {
        JsonObject ret = null;
        int found = 0;
        for(Map.Entry<String, JsonElement> song: variantObject.entrySet()){
            if(getSongIndex(song.getValue(), variantObject) == index){
                ret = song.getValue().getAsJsonObject(); found++;
            } /* This is not it */
        }
        if(1 == found){
            return ret; /* Successfully found Song at index */
        }else throw new InvalidObjectException("Found the " + index + ". song " + found + " times!");
    }

    public static JsonObject addNewSongObjectInto(String path, JsonObject variantObject) throws InvalidObjectException { /* TODO: Move the structural part into models */
        Random random = new Random();
        int id = random.nextInt();
        boolean unique = false;

        JsonObject song = new JsonObject();
        while(!unique){ /* Iterate through the currently added songs */
            unique = true; /* Until a unique ID is found */
            id  = random.nextInt();
            for(Map.Entry<String, JsonElement>  songEntry : variantObject.entrySet()){
                if(!songEntry.getValue().isJsonObject()) throw new InvalidObjectException("Playlist variant contains an invalid song!");
                if(id == songEntry.getValue().getAsJsonObject().getAsJsonPrimitive(PlaylistStructure.SONG_JSONID_id).getAsInt()){
                    unique = false;
                }
            }
        }
        song.addProperty(PlaylistStructure.SONG_JSONID_id , id);
        song.addProperty(PlaylistStructure.SONG_JSONID_location, path);
        return song;
    }
}