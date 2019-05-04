package spectrogram;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

import java.io.*;

public class PlaylistHandler {

    private File playlistFile = null;
    private JsonObject playlistObj = null;

    public enum Validity {
        noexist, notAFile, emptyFile, invalidJSON, emptyList, valid, encoded
    }

    public void initializePlaylist() throws PlaylistOverrideException {
        if(
            (Validity.emptyFile.ordinal() <= isPlaylistValid().ordinal())
            &&(Validity.valid.ordinal() >= isPlaylistValid().ordinal())
        )
        { /* playlist exists, but isn't valid */
            /* Initialize and set up JSON object */
            playlistObj = new JsonObject();
            playlistObj.addProperty("name", playlistFile.getName());

            /* Write JSON object out to the file */
            try (FileWriter file = new FileWriter(playlistFile)) {
                file.write(playlistObj.toString());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        else
        { /* Should not override a valid playlist */
            throw new PlaylistOverrideException();
        }
    }

    private void parsePlaylist() throws InvalidPlaylistException, JsonSyntaxException {
        if(Validity.emptyFile.ordinal() < isPlaylistValid().ordinal())
        {
            InputStream is = null;
            String fileText = "";
            try {
                is = new FileInputStream(playlistFile);
                BufferedReader buf = new BufferedReader(new InputStreamReader(is));
                String line = buf.readLine();
                StringBuilder sb = new StringBuilder();
                while(line != null) { sb.append(line).append("\n"); line = buf.readLine(); }
                fileText = sb.toString();
            } catch (IOException e) {
                e.printStackTrace();
            }
            JsonParser parser = new JsonParser();
            playlistObj = (JsonObject)parser.parse(fileText);
        }
        else
        {
            throw new InvalidPlaylistException();
        }
    }

    public boolean openPlayList(File playlist) throws InvalidPlaylistException , JsonSyntaxException
    {
        if(Validity.emptyFile.ordinal() >= isPlaylistValid().ordinal())
        { /* No playlist is open */
            this.playlistFile = playlist;
            parsePlaylist();
            return (Validity.valid == isPlaylistValid());
        }
        else
        { /* A playlist is already open */
            return false;
        }
    }

    public Validity isPlaylistValid()
    {
        if((null == playlistFile)||(!playlistFile.exists()))
        { /* if the playlist file is non-existent */
            return Validity.noexist;
        }

        if(playlistFile.isFile())
        {
            return Validity.notAFile;
        }

        if(0 == playlistFile.length())
        { /* The file doesn't have any content in it */
            return Validity.emptyFile;
        }

        if(true != playlistObj.isJsonObject())
        {
            return  Validity.invalidJSON;
        }

        /* TODO: Tell when the Playlist is empty!  */

        return Validity.noexist;
    }

    public String getPlayListPath() throws InvalidPlaylistException {
        if(Validity.valid == isPlaylistValid())
        {
            return playlistFile.getPath();
        }
        else
        {
            throw new InvalidPlaylistException();
        }
    }
    public String getPlayListName() throws InvalidPlaylistException {
        if(Validity.valid == isPlaylistValid())
        {
            return playlistFile.getName();
        }
        else
        {
            throw new InvalidPlaylistException();
        }
    }
}
