package spectrogram;

import javafx.stage.Stage;
import spectrogram_services.PlaylistHandler;
import spectrogram_services.WavConverter;

/* TODO: Reform Global logic */
public class Global {
    public static PlaylistHandler plHandler = new PlaylistHandler();
    public static WavConverter wConv = null;
    public static Stage primaryStage;
}
