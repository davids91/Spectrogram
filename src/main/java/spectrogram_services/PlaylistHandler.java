package spectrogram_services;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import spectrogram_models.InvalidPlaylistException;
import spectrogram_models.PlaylistOverrideException;
import spectrogram_models.PlaylistStructure;

import java.io.*;
import java.util.ArrayList;
import java.util.Map;

public class PlaylistHandler {

    private File playlistFile = null;
    private JsonObject playlistObj = null;

    public enum Validity {
        undefined, noexist, notAFile, invalidFormat, unknownFormat, emptyFile, emptyList, valid, encoded
    }

    public boolean addVariant(String variant){
        if(Validity.emptyList.ordinal() <= isPlaylistValid().ordinal()){
            playlistObj.add(variant, new JsonObject());
            System.out.println(playlistObj.toString());
            return selectVariant(variant); /* includes a `flush()` */
        }else return false;
    }

    public boolean selectVariant(String variant)
    {
        if(
        (Validity.emptyList.ordinal() <= isPlaylistValid().ordinal()) /* Playlist state is OK */
        &&(!PlaylistStructure.isControlKey(variant))&&(playlistObj.has(variant)) /* variant exists */
        ){
            playlistObj.addProperty(PlaylistStructure.lastSelectedVariant.key(), variant);
            flush();
            return true;
        }else return false;
    }

    public ArrayList<String> getPlaylistVariants() throws InvalidPlaylistException {
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
        }else throw new InvalidPlaylistException("Unable to read back playlist variants");
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

    private void initializePlaylist() throws PlaylistOverrideException {
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
            throw new PlaylistOverrideException();
        }
    }

    private boolean parsePlaylist()
    {
        if(
            (Validity.notAFile.ordinal() < isPlaylistValid().ordinal()) /* State permits parsing */
            ||(0 < playlistFile.length()) /* There is anything to parse */
        )
        {
            System.out.println("Parsing " + playlistFile.getPath());
            try {
                InputStream is = new FileInputStream(playlistFile);
                BufferedReader buf = new BufferedReader(new InputStreamReader(is));
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

    public boolean openPlaylist(File playlist) throws InvalidPlaylistException, JsonSyntaxException
    {

        this.playlistFile = playlist; /* Update the Playlist */
        this.playlistObj = null;

        if(!parsePlaylist())
        { /* unable to parse playlist --> Playlist is empty / new */
            try {
                initializePlaylist();
            } catch (PlaylistOverrideException e) {
                e.printStackTrace();
                throw new InvalidPlaylistException("Unable to Initialize playlist!");
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
            return Validity.emptyFile; /* The file doesn't have any content in it */

        if((null != playlistObj)&&(playlistObj.isJsonObject()))
        {
            if(2 < playlistObj.size()) return Validity.valid;
            else if((playlistObj.size() == 2) /* Only 2 objects are present inside the JSON */
                &&(playlistObj.getAsJsonObject( /* inside the playList */
                    /* The last used Variant */
                    playlistObj.getAsJsonPrimitive(PlaylistStructure.lastSelectedVariant.key()).getAsString()
                ).size() == 0) /* Has a size of 0 */
            )return Validity.emptyList;
        }else return Validity.invalidFormat;

        /* TODO: Encryption */

        System.out.println("Something is undefined boss!");
        return Validity.undefined;
    }

    public void closePlaylist()
    {
        playlistObj = null;
        playlistFile = null;
    }

    public String getPlayListPath() throws InvalidPlaylistException {
        if(Validity.emptyFile.ordinal() <= isPlaylistValid().ordinal())
        {
            return playlistFile.getPath();
        }
        else
        {
            throw new InvalidPlaylistException("Unable to determine playlist path");
        }
    }

    public String getPlayListName() throws InvalidPlaylistException {
        if(Validity.emptyFile.ordinal() <= isPlaylistValid().ordinal())
        {
            return playlistFile.getName();
        }
        else
        {
            throw new InvalidPlaylistException();
        }
    }
}
