package spectrogram;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

import java.io.*;

public class PlaylistHandler {

    private File playlistFile = null;
    private JsonObject playlistObj = null;

    public enum Validity {
        undefined, noexist, notAFile, invalidFormat, unknownFormat, emptyFile, emptyList, valid, encoded
    }

    private void initializePlaylist() throws PlaylistOverrideException {
        if(
            (Validity.unknownFormat.ordinal() <= isPlaylistValid().ordinal())
            &&(Validity.valid.ordinal() >= isPlaylistValid().ordinal())
        )
        { /* playlist exists, but isn't valid */
            /* Initialize and set up JSON object */
            playlistObj = new JsonObject();
            playlistObj.addProperty("lastSVar","default");

            /* Add a default Variant */
            JsonObject newVariant = new JsonObject();

            playlistObj.add("default", newVariant);

            /* Write JSON object out to the file */
            System.out.println("Writing out: " + playlistObj.toString());
            try (FileWriter file = new FileWriter(playlistFile)) {
                file.write(playlistObj.toString());
                file.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
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

    boolean openPlaylist(File playlist) throws InvalidPlaylistException, JsonSyntaxException
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

    Validity isPlaylistValid()
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

        /*if((null == playlistObj)||(!playlistObj.isJsonObject()))
            return Validity.invalidFormat; /* Not sure how to set this one..  */

        if((null != playlistObj)&&(playlistObj.size() == 2) /* Only 2 objects are present inside the JSON */
            &&(playlistObj.getAsJsonObject( /* inside the playList */
                playlistObj.getAsJsonPrimitive("lastSVar").getAsString() /* The last used Variant */
            ).size() == 0) /* Has a size of 0 */
        )
            return Validity.emptyList;


        /* TODO: Tell when the Playlist is valid!  */

        return Validity.undefined;
    }

    void closePlaylist()
    {
        playlistObj = null;
        playlistFile = null;
    }

    String getPlayListPath() throws InvalidPlaylistException {
        if(Validity.emptyFile.ordinal() <= isPlaylistValid().ordinal())
        {
            return playlistFile.getPath();
        }
        else
        {
            throw new InvalidPlaylistException("Unable to determine playlist path");
        }
    }

    String getPlayListName() throws InvalidPlaylistException {
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
