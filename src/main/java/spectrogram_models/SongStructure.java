package spectrogram_models;

import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TitledPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;

import java.io.File;
import java.io.IOException;

public class SongStructure {

    public static Image getImageFromFile(File file) throws IOException {
        return Global.getCache().getCachedSpectrogram(file);
    }

    public static HBox getSongControls(PlayListAccordion mainAccordion){
        HBox controlBox = new HBox();

        Button prevBtn = new Button();
        prevBtn.setText("<<");
        prevBtn.setOnAction((actionEvent)->mainAccordion.setExpandedPane(
            mainAccordion.getPanes().get(mainAccordion.getPrevSelectableIndex())
        ));

        Button playBtn = new Button();
        playBtn.setText(">");
        /* TODO: play Button Functionality */

        Button nextBtn = new Button();
        nextBtn.setText(">>");
        nextBtn.setOnAction((actionEvent)->mainAccordion.setExpandedPane(
                mainAccordion.getPanes().get(mainAccordion.getNextSelectableIndex())
        ));

        controlBox.getChildren().add(prevBtn);
        controlBox.getChildren().add(playBtn);
        controlBox.getChildren().add(nextBtn);

        return controlBox;
    }

    public static TitledPane getSongTitledPane(File songFile, PlayListAccordion mainAccordion) throws IOException {
        TitledPane songPane = new TitledPane();

        /* Content */
        songPane.setText(songFile.getName());
        songPane.setGraphic(getSongControls(mainAccordion));
        ScrollPane alignPane = new ScrollPane();
        Image theImg = SongStructure.getImageFromFile(songFile);
        ImageView theImgV = new ImageView(theImg);

        alignPane.setContent(theImgV);
        songPane.setContent(alignPane);

        /* Alignment */
        theImgV.fitHeightProperty().bind(songPane.heightProperty());
        alignPane.setMinHeight(800); /* TODO: Set a static size, display image correctly */
        alignPane.setFitToWidth(true);
        alignPane.setFitToHeight(true);

        alignPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);


        return songPane;
    }
}
