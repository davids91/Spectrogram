package spectrogram;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

import java.io.*;

public class PlaylistHandler {

    private File playlistFile = null;
    private JsonObject playlistObj = null;

    public enum Validity {
        noexist, notAFile, invalidFormat, unknownFormat, emptyFile, emptyList, valid, encoded
    }

    public void initializePlaylist() throws PlaylistOverrideException {
        if(
            (Validity.unknownFormat.ordinal() <= isPlaylistValid().ordinal())
            &&(Validity.valid.ordinal() >= isPlaylistValid().ordinal())
        )
        { /* playlist exists, but isn't valid */
            /* Initialize and set up JSON object */
            playlistObj = new JsonObject();
            
            /* Write JSON object out to the file * There is nothing to write yet.
            System.out.println("Writing out: " + playlistObj.toString());
            try (FileWriter file = new FileWriter(playlistFile)) {
                file.write(playlistObj.toString());
                file.flush();
            } catch (IOException e) {
                e.printStackTrace();
            } /**/
        }
        else
        { /* Should not override a valid playlist */
            throw new PlaylistOverrideException();
        }
    }

    private void parsePlaylist() throws InvalidPlaylistException {
        if(Validity.unknownFormat.ordinal() <= isPlaylistValid().ordinal())
        {
            try {
                InputStream is = new FileInputStream(playlistFile);
                BufferedReader buf = new BufferedReader(new InputStreamReader(is));
                String line = buf.readLine();
                StringBuilder sb = new StringBuilder();
                while(line != null) { sb.append(line).append("\n"); line = buf.readLine(); }
                JsonParser parser = new JsonParser();
                playlistObj = (JsonObject)(parser.parse(sb.toString()));
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JsonSyntaxException | ClassCastException e) {
                playlistObj = null; /* JSON interpretation error, or classCast from JSONNull to Json object */
            }
        }
        else
        {
            throw new InvalidPlaylistException();
        }
    }

    public boolean openPlayList(File playlist)throws InvalidPlaylistException, JsonSyntaxException
    {
        if(Validity.emptyFile.ordinal() >= isPlaylistValid().ordinal())
        { /* No valid playlist is actually open */
            this.playlistFile = playlist; /* Update the Playlist */
            if(Validity.unknownFormat == isPlaylistValid())
            { /* If the playlist is an existing, not empty file at least */
                parsePlaylist();
            }
            if(Validity.unknownFormat.ordinal() <= isPlaylistValid().ordinal())
            { /* Still invalid Format --> Playlist is empty / new */
                try {
                    initializePlaylist();
                } catch (PlaylistOverrideException e) {
                    e.printStackTrace();
                }
            }
            else
            { /* The playlist doesn't exist or it's not a file */
                throw new InvalidPlaylistException("Playlist to be opened doesn't exist or is not a file.");
            }
            return (Validity.valid == isPlaylistValid());
        }
        else
        { /* A playlist is already open */
            return false;
        }
    }

    public Validity isPlaylistValid()
    {
        /* if the playlist file is non-existent */
        if((null == playlistFile)||(!playlistFile.exists()))
            return Validity.noexist;

        if(!playlistFile.isFile())
            return Validity.notAFile;

        if((null == playlistObj)||(!playlistObj.isJsonObject()))
            return Validity.unknownFormat;

        /* The file doesn't have any content in it */
        if(0 == playlistFile.length())
            return Validity.emptyFile;

        /* TODO: Tell when the Playlist is empty!  */

        return Validity.noexist;
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
            throw new InvalidPlaylistException();
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
