package spectrogram;

import java.io.File;

public class PlaylistHandler {

    private File playlist = null;

    public boolean openPlayList(File playlist)
    {
        this.playlist = playlist;

        return isPlaylistValid();
    }

    public boolean isPlaylistValid()
    {
        return (null != playlist)&&(playlist.exists()); /* Not much validation yet, eh */
    }

    public String getPlayListPath() throws InvalidPlaylistException {
        if(isPlaylistValid())
        {
            return playlist.getPath();
        }
        else
        {
            throw new InvalidPlaylistException();
        }
    }

}
