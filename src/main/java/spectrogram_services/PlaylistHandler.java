package spectrogram_services;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import spectrogram_models.PlaylistStructure;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
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
            System.out.println(playlistObj.toString());
            return selectVariant(variant); /* includes a `flush()` */
        }else return false;
    }


    public boolean selectVariant(String variant, boolean writeOut){
        boolean ret = selectVariant(variant);
        if(writeOut)flush();
        return ret;
    }

    public boolean selectVariant(String variant)
    {
        if(
        (Validity.emptyList.ordinal() <= isPlaylistValid().ordinal()) /* Playlist state is OK */
        &&(!PlaylistStructure.isControlKey(variant))&&(playlistObj.has(variant)) /* variant exists */
        ){
            playlistObj.addProperty(PlaylistStructure.lastSelectedVariant.key(), variant);
            return true;
        }else return false;
    }

    public JsonObject getVariant(String variant) throws InterruptedException {
        if(
                (Validity.emptyList.ordinal() <= isPlaylistValid().ordinal()) /* Playlist state is OK */
                &&(!PlaylistStructure.isControlKey(variant))&&(playlistObj.has(variant)) /* variant exists */
                &&(selectVariant(variant)) /* Able to select */
        ){
            return playlistObj.getAsJsonObject(variant);
        }else throw new InterruptedException("Playlist not Valid or Variant name is incorrect!");
    }

    public ArrayList<String> getPlaylistVariants() throws IllegalStateException {
        if(Validity.emptyList.ordinal() <= isPlaylistValid().ordinal()) {
            ArrayList<String> variants = new ArrayList();

            for(Map.Entry<String, JsonElement> item : playlistObj.entrySet()){
                if(
                    (!PlaylistStructure.isControlKey(item.getKey()))
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
            System.out.println("Writing out: " + playlistObj.toString());
            try (FileWriter file = new FileWriter(playlistFile)) {
                file.write(playlistObj.toString());
                file.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } /* else Object state is not valid */
    }

    private void initializePlaylist() throws IllegalStateException {
        if(
            (Validity.unknownFormat.ordinal() <= isPlaylistValid().ordinal())
            &&(Validity.valid.ordinal() >= isPlaylistValid().ordinal())
        )
        { /* playlist exists, but isn't valid */
            /* Initialize and set up JSON object */
            playlistObj = new JsonObject();

            addVariant("default"); /* Add a default Variant */
            flush(); /* Write JSON object out to the file */
        }
        else
        { /* Should not override a valid playlist */
            throw new IllegalStateException("Unable to initialize playlist because it's not valid!");
        }
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
                BufferedReader buf = new BufferedReader(new InputStreamReader(is));
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

    public void addSongToVariant(File song, String variant) throws FileNotFoundException {
        if(
                (Validity.emptyList.ordinal() <= isPlaylistValid().ordinal()) /* Playlist state is OK */
                &&(!PlaylistStructure.isControlKey(variant))&&(playlistObj.has(variant)) /* variant exists */
        ){
            JsonObject varObj = playlistObj.getAsJsonObject(variant);
            varObj.addProperty("" + varObj.size(), song.getAbsolutePath());
            flush();
        }else throw new IllegalStateException("Unable to add song! Playlist file is not valid or variant " + variant + " doesn't exist!");
    }

    public boolean openPlaylist(File playlist) throws IllegalStateException, JsonSyntaxException
    {

        this.playlistFile = playlist; /* Update the Playlist */
        this.playlistObj = null;

        if(!parsePlaylist())
        { /* unable to parse playlist --> Playlist is empty / new */
            try {
                initializePlaylist();
            } catch (IllegalStateException e) {
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
            }catch (NullPointerException e){ /* JSON Control Sting ot found in playlist File */
                return Validity.invalidFormat;
            }
        }else return Validity.invalidFormat;

        /* TODO: Encryption */

        System.out.println("Something is undefined boss!");
        return Validity.undefined;
    }

    public boolean removeVariant(String variant){
        if(Validity.emptyList.ordinal() < isPlaylistValid().ordinal()){
            if(
                (!PlaylistStructure.isControlKey(variant))/* The item is not a Control key */
                &&(null != playlistObj.remove(variant))/* The item exists in the playlist as a Variant */
            ){
                return true;
            } else /* The variant doesn't exist or a control key is being removed */ return false;
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
