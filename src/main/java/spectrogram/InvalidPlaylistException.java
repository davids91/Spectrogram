package spectrogram;

public class InvalidPlaylistException extends Exception {
    public InvalidPlaylistException() {
        super();
    }
    public InvalidPlaylistException(String message) {
        super(message);
    }
}
