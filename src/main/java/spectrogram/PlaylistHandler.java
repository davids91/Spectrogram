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
        }
        else
        { /* Should not override a valid playlist */
            throw new PlaylistOverrideException();
        }
    }

    public boolean openPlayList(File playlist)
    {
        if(Validity.emptyFile.ordinal() >= isPlaylistValid().ordinal())
        {
            this.playlistFile = playlist;
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

        String fileText = "";
        if(0 < playlistFile.length())
        { /* The file has a length! let's read it in! */
            InputStream is = null;
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
        }
        else
        {
            return Validity.emptyFile;
        }

        JsonParser parser = new JsonParser();
        try {
            playlistObj = (JsonObject)parser.parse(fileText);
        } catch (JsonSyntaxException e) {
            return  Validity.invalidJSON;
        }
        /* TODO: Tell when it's valid!  */

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
