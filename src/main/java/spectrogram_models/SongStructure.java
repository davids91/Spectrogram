package spectrogram_models;

import javafx.scene.control.TitledPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import spectrogram_services.WavConverter;

import java.io.File;
import java.io.FileNotFoundException;

public class SongStructure {

    public static Image getImageFromFile(File file) throws FileNotFoundException {
        return WavConverter.imageFromMp3(file);
    }

    public static TitledPane getSongTitledPane(File songFile) throws FileNotFoundException {
        TitledPane songPane = new TitledPane();
        songPane.setText(songFile.getName());

        AnchorPane lofasz = new AnchorPane();
        lofasz.getChildren().add(new ImageView(SongStructure.getImageFromFile(songFile)));
        songPane.setContent(lofasz);

        return songPane;
    }
}
