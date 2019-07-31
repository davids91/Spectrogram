package spectrogram_services;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import spectrogram_models.PlaylistStructure;

import java.io.*;
import java.util.ArrayList;
import java.util.Map;

public class PlaylistHandler {

    private File playlistFile = null;
    private JsonObject playlistObject = null;

    public boolean addVariant(String variant){
        if(PlaylistStructure.Validity.emptyList.ordinal() <= isPlaylistValid().ordinal()){
            PlaylistStructure.addVariant(variant,playlistObject);
            flush();
            return true;
        }else return false;
    }

    public boolean selectVariant(String variant, boolean writeOut){
        boolean ret = selectVariant(variant);
        if(writeOut)flush();
        return ret;
    }

    public String getLastSelectedVariant() throws IllegalStateException{
        if(PlaylistStructure.Validity.emptyList.ordinal() <= isPlaylistValid().ordinal()) /* Playlist state is OK */
        {
            return PlaylistStructure.getLastSelectedVariant(playlistObject);
        }else throw new IllegalStateException("Unable to determine last selected Variant, playlist not valid!");
    }

    private boolean selectVariant(String variant)
    {
        if(
        (PlaylistStructure.Validity.emptyList.ordinal() <= isPlaylistValid().ordinal()) /* Playlist state is OK */
        &&(PlaylistStructure.isValidVariant(variant,playlistObject)) /* variant exists */
        ){
            playlistObject.addProperty(PlaylistStructure.lastSelectedVariant.key(), variant);
            return true;
        }else return false;
    }

    JsonObject getVariant(String variant) throws InterruptedException {
        if(
            (PlaylistStructure.Validity.emptyList.ordinal() <= isPlaylistValid().ordinal()) /* Playlist state is OK */
            &&(PlaylistStructure.isValidVariant(variant,playlistObject)) /* variant exists */
        ){
            return playlistObject.getAsJsonObject(variant);
        }else throw new InterruptedException("Playlist not Valid or Variant name is incorrect!");
    }

    public ArrayList<String> getPlaylistVariants() throws IllegalStateException {
        if(PlaylistStructure.Validity.emptyList.ordinal() <= isPlaylistValid().ordinal()) {
            ArrayList<String> variants = new ArrayList<>();

            for(Map.Entry<String, JsonElement> item : playlistObject.entrySet()){
                if(
                    (PlaylistStructure.isNotControlKey(item.getKey()))
                    &&(item.getValue().isJsonObject())
                ){ /* Not a control key And it's a JSON Object*/
                    variants.add(item.getKey()); /* All JSONObjects except the Control values count as Variants */
                }/* else might be a Control key, might be a simple property */
            }
            return variants;
        }else throw new IllegalStateException("Unable to read back playlist variants, because playlist is not valid!");
    }

    private void flush()
    {
        if(PlaylistStructure.Validity.emptyList.ordinal() <= isPlaylistValid().ordinal()) {
            try (FileWriter file = new FileWriter(playlistFile)) {
                file.write(playlistObject.toString());
                file.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } /* else Object state is not valid */
    }

    private void initializePlaylist() throws IllegalStateException, IOException {
        if(playlistFile.exists()||playlistFile.createNewFile()){
            playlistObject = new JsonObject();
            addVariant("default"); /* Add a default Variant */
            flush(); /* Write JSON object out to the file */
        }else throw new IllegalStateException("Unable to create playlist file!");
    }

    private boolean parsePlaylist()
    {
        if(
            (PlaylistStructure.Validity.notAFile.ordinal() < isPlaylistValid().ordinal()) /* State permits parsing */
            ||(0 < playlistFile.length()) /* There is anything to parse */
        )
        {
            try (
                InputStream is = new FileInputStream(playlistFile);
                BufferedReader buf = new BufferedReader(new InputStreamReader(is))
            ){
                String line = buf.readLine();
                StringBuilder sb = new StringBuilder();
                while(line != null) { sb.append(line).append("\n"); line = buf.readLine(); }
                is.close();
                buf.close();
                JsonParser parser = new JsonParser();
                playlistObject = (JsonObject)(parser.parse(sb.toString()));
                return true;
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            } catch (JsonSyntaxException | ClassCastException e) {
                playlistObject = null; /* JSON interpretation error, or classCast from JSONNull to Json object */
                return false;
            }
        }
        else
        {
            return false;
        }
    }

    /**
     * Adds a Song to the playlist
     * @param song the File containing the Song
     * @param variant the string of which variant it needs to be added to
     * @return the new Song as a JsonObject
     */
    JsonObject addSongToVariant(File song, String variant) throws InvalidObjectException {
        if(
            (PlaylistStructure.Validity.emptyList.ordinal() <= isPlaylistValid().ordinal()) /* Playlist state is OK */
            &&(PlaylistStructure.isValidVariant(variant,playlistObject)) /* variant exists */
        ){
            JsonObject variantObject = playlistObject.getAsJsonObject(variant);
            JsonObject songObject = PlaylistStructure.addNewSongObjectInto(song.getPath(),variantObject);
            variantObject.add("" + variantObject.size(), songObject);
            flush();
            return songObject;
        }else throw new InvalidObjectException("Unable to add song! Playlist file is not valid or variant " + variant + " doesn't exist!");
    }

    public boolean createNewPlaylist(File newPlaylist){
        this.playlistFile = newPlaylist; /* Update the Playlist */
        this.playlistObject = null;
        try {
            initializePlaylist();
            return true;
        } catch (IllegalStateException | IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean openPlaylist(File playlist) throws IllegalStateException, JsonSyntaxException
    {
        this.playlistFile = playlist; /* Update the Playlist */
        this.playlistObject = null;

        if(!parsePlaylist())
        { /* unable to parse playlist --> Playlist is empty / new */
            System.out.println("Unable to parse playlist!");
            try {
                initializePlaylist();
            } catch (IllegalStateException | IOException e) {
                e.printStackTrace();
                throw new IllegalStateException("Unable to Initialize playlist!");
            }
        }
        return (PlaylistStructure.Validity.invalidFormat.ordinal() < isPlaylistValid().ordinal());
    }

    public PlaylistStructure.Validity isPlaylistValid()
    {
        /* if the playlist file is non-existent */
        if((null == playlistFile)||(!playlistFile.exists()))
            return PlaylistStructure.Validity.noexist;

        if(!playlistFile.isFile())
            return PlaylistStructure.Validity.notAFile;

        if((null == playlistObject)||(!playlistObject.isJsonObject()))
            return PlaylistStructure.Validity.unknownFormat; /* The playlist is an existing file, not sure about its content */

        if(0 == playlistFile.length())
            return PlaylistStructure.Validity.emptyList; /* The file doesn't have any content in it */

        if((null != playlistObject)&&(playlistObject.isJsonObject()))
        {
            try {
                int validVariants = PlaylistStructure.getValidVariants(playlistObject);
                if (1 < validVariants) return PlaylistStructure.Validity.valid; /* TODO: Encrypted state */
                else if( (0 < validVariants) /* There is at least one variant */
                    && (PlaylistStructure.lastUsedVariantSize(playlistObject) == 0) /* of size 0 */
                ) return PlaylistStructure.Validity.emptyList;
                else return PlaylistStructure.Validity.invalidFormat;
            }catch (NullPointerException e){ /* JSON Control Sting not found in playlist File */
                e.printStackTrace();
                return PlaylistStructure.Validity.invalidFormat;
            }
        }else return PlaylistStructure.Validity.invalidFormat;
    }

    boolean removeVariant(String variant){
        if(PlaylistStructure.Validity.emptyList.ordinal() < isPlaylistValid().ordinal()){ /* The item exists in the playlist as a Variant */
            return (PlaylistStructure.isValidVariant(variant,playlistObject)) /* variant exists */
                    && (null != playlistObject.remove(variant)); /* The variant exists */
        } /* else the playlist doesn't have any variants to remove */ return false;
    }

    public void closePlaylist()
    {
        playlistObject = null;
        playlistFile = null;
    }

    public String getPlayListPath() throws IllegalStateException {
        if(PlaylistStructure.Validity.emptyList.ordinal() <= isPlaylistValid().ordinal())
        {
            return playlistFile.getPath();
        }else{
            throw new IllegalStateException("Unable to determine playlist path!");
        }
    }

    public String getPlayListName() throws IllegalStateException {
        if(PlaylistStructure.Validity.emptyList.ordinal() <= isPlaylistValid().ordinal())
        {
            return playlistFile.getName();
        }else{
            throw new IllegalStateException("Unable to determine playlist name!");
        }
    }
}
