package spectrogram_models;

import javafx.scene.control.TitledPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

public class SongStructure {

    public static Image getImageFromFile(File file) throws IOException {
        return Global.getCache().getCachedSpectrogram(file);
    }

    public static TitledPane getSongTitledPane(File songFile) throws IOException {
        TitledPane songPane = new TitledPane();
        songPane.setText(songFile.getName());

        AnchorPane lofasz = new AnchorPane();
        lofasz.getChildren().add(new ImageView(SongStructure.getImageFromFile(songFile)));
        songPane.setContent(lofasz);

        return songPane;
    }
}
