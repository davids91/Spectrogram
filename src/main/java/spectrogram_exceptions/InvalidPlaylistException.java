package spectrogram_exceptions;

public class InvalidPlaylistException extends Exception {
    public InvalidPlaylistException() {
        super();
    }
    public InvalidPlaylistException(String message) {
        super(message);
    }
}
