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
    private JsonObject playlistObj = null;

    public enum Validity {
        undefined, noexist, notAFile, invalidFormat, unknownFormat, emptyList, valid, encoded
    }

    public boolean addVariant(String variant){
        if(Validity.emptyList.ordinal() <= isPlaylistValid().ordinal()){
            playlistObj.add(variant, new JsonObject());
            playlistObj.addProperty(PlaylistStructure.lastSelectedVariant.key(), variant);
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
        if(Validity.emptyList.ordinal() <= isPlaylistValid().ordinal()) /* Playlist state is OK */
        {
            return playlistObj.getAsJsonPrimitive(PlaylistStructure.lastSelectedVariant.key()).toString()
                    .replace("\"","");
        }else throw new IllegalStateException("Unable to determine last selected Variant, playlist not valid!");
    }

    private boolean selectVariant(String variant)
    {
        if(
        (Validity.emptyList.ordinal() <= isPlaylistValid().ordinal()) /* Playlist state is OK */
        &&(PlaylistStructure.isNotControlKey(variant))&&(playlistObj.has(variant)) /* variant exists */
        ){
            playlistObj.addProperty(PlaylistStructure.lastSelectedVariant.key(), variant);
            return true;
        }else return false;
    }

    JsonObject getVariant(String variant) throws InterruptedException {
        if(
                (Validity.emptyList.ordinal() <= isPlaylistValid().ordinal()) /* Playlist state is OK */
                &&(PlaylistStructure.isNotControlKey(variant))&&(playlistObj.has(variant)) /* variant exists */
        ){
            return playlistObj.getAsJsonObject(variant);
        }else throw new InterruptedException("Playlist not Valid or Variant name is incorrect!");
    }

    public ArrayList<String> getPlaylistVariants() throws IllegalStateException {
        if(Validity.emptyList.ordinal() <= isPlaylistValid().ordinal()) {
            ArrayList<String> variants = new ArrayList<>();

            for(Map.Entry<String, JsonElement> item : playlistObj.entrySet()){
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
        if(Validity.emptyList.ordinal() <= isPlaylistValid().ordinal()) {
            try (FileWriter file = new FileWriter(playlistFile)) {
                file.write(playlistObj.toString());
                file.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } /* else Object state is not valid */
    }

    private void initializePlaylist() throws IllegalStateException, IOException {
        /* Initialize and set up JSON object */
        if(!playlistFile.exists())playlistFile.createNewFile();
        playlistObj = new JsonObject();
        addVariant("default"); /* Add a default Variant */
        flush(); /* Write JSON object out to the file */
    }

    private boolean parsePlaylist()
    {
        if(
            (Validity.notAFile.ordinal() < isPlaylistValid().ordinal()) /* State permits parsing */
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
                playlistObj = (JsonObject)(parser.parse(sb.toString()));
                return true;
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            } catch (JsonSyntaxException | ClassCastException e) {
                playlistObj = null; /* JSON interpretation error, or classCast from JSONNull to Json object */
                return false;
            }
        }
        else
        {
            return false;
        }
    }

    void addSongToVariant(File song, String variant) {
        if(
            (Validity.emptyList.ordinal() <= isPlaylistValid().ordinal()) /* Playlist state is OK */
            &&(PlaylistStructure.isNotControlKey(variant))&&(playlistObj.has(variant)) /* variant exists */
        ){
            try {
                JsonObject variantObject = playlistObj.getAsJsonObject(variant);
                variantObject.add("" + variantObject.size(), PlaylistStructure.getSongObjectInto(song.getPath(),variantObject));
                flush();
            } catch (NoSuchFieldException e) {
                e.printStackTrace();
            }
        }else throw new IllegalStateException("Unable to add song! Playlist file is not valid or variant " + variant + " doesn't exist!");
    }

    public boolean createNewPlaylist(File newPlaylist){
        this.playlistFile = newPlaylist; /* Update the Playlist */
        this.playlistObj = null;
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
        this.playlistObj = null;

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
        return (Validity.invalidFormat.ordinal() < isPlaylistValid().ordinal());
    }

    public Validity isPlaylistValid()
    {
        /* if the playlist file is non-existent */
        if((null == playlistFile)||(!playlistFile.exists()))
            return Validity.noexist;

        if(!playlistFile.isFile())
            return Validity.notAFile;

        if((null == playlistObj)||(!playlistObj.isJsonObject()))
            return Validity.unknownFormat; /* The playlist is an existing file, not sure about its content */

        if(0 == playlistFile.length())
            return Validity.emptyList; /* The file doesn't have any content in it */

        if((null != playlistObj)&&(playlistObj.isJsonObject()))
        {
            try {
                if (2 < playlistObj.size()) return Validity.valid;
                else if ((playlistObj.size() == 2) /* Only 2 objects are present inside the JSON */
                        && (playlistObj.getAsJsonObject( /* inside the playList */
                        /* The last used Variant */
                        playlistObj.getAsJsonPrimitive(PlaylistStructure.lastSelectedVariant.key()).getAsString()
                    ).size() == 0) /* Has a size of 0 */
                ) return Validity.emptyList;
                else if(0 < playlistObj.getAsJsonObject( /* inside the playList */
                        /* The last used Variant */
                        playlistObj.getAsJsonPrimitive(PlaylistStructure.lastSelectedVariant.key()).getAsString()
                ).size()) return Validity.valid;
            }catch (NullPointerException e){ /* JSON Control Sting not found in playlist File */
                return Validity.invalidFormat;
            }
        }else return Validity.invalidFormat;

        /* TODO: Encryption */

        System.out.println("Something is undefined boss!");
        return Validity.undefined;
    }

    boolean removeVariant(String variant){
        if(Validity.emptyList.ordinal() < isPlaylistValid().ordinal()){ /* The item exists in the playlist as a Variant */
            return (PlaylistStructure.isNotControlKey(variant))/* The item is not a Control key */
                    && (null != playlistObj.remove(variant)); /* The variant exists */
        } /* else the playlist doesn't have any variants to remove */ return false;
    }

    public void closePlaylist()
    {
        playlistObj = null;
        playlistFile = null;
    }

    public String getPlayListPath() throws IllegalStateException {
        if(Validity.emptyList.ordinal() <= isPlaylistValid().ordinal())
        {
            return playlistFile.getPath();
        }else{
            throw new IllegalStateException("Unable to determine playlist path!");
        }
    }

    public String getPlayListName() throws IllegalStateException {
        if(Validity.emptyList.ordinal() <= isPlaylistValid().ordinal())
        {
            return playlistFile.getName();
        }else{
            throw new IllegalStateException("Unable to determine playlist name!");
        }
    }
}
