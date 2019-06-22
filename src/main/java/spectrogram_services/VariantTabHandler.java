package spectrogram_services;

import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.stage.FileChooser;
import spectrogram_models.Global;
import spectrogram_services.PlaylistHandler;
import spectrogram_services.WavConverter;

import java.io.File;
import java.io.FileNotFoundException;

public class VariantTabHandler extends Tab {

    private String variant = "";
    private PlaylistHandler plHandler = null;
    private Accordion mainAccordion = null;

    public VariantTabHandler(PlaylistHandler plHandler, String variant){
        setText(variant);
        this.variant = variant;
        setClosable(true);
        if(null != plHandler){
            this.plHandler = plHandler;
            setOnCloseRequest(removeVariantRequest);

            /* Add an Accordion and a titledPane to add new Music */
            mainAccordion = new Accordion();
            TitledPane addMusicTitledPane = new TitledPane();
            mainAccordion.getPanes().add(addMusicTitledPane);

            /* Add button for adding music */
            Button addMusicBtn = new Button();
            addMusicBtn.setId(variant + "addMusicBtn");
            addMusicBtn.setText("+");
            addMusicTitledPane.setGraphic(addMusicBtn);
            addMusicBtn.setOnAction(actionEvent -> addSong());

            /* Set the content of the Tab */
            this.setContent(mainAccordion);

        }else throw new UnsupportedOperationException("Standalone VariantHandler not supported!");
    }

    private void addSong(){
        FileChooser flc = new FileChooser();
        flc.setTitle("Add song to variant " + variant);
        flc.setSelectedExtensionFilter(new FileChooser.ExtensionFilter("Musica mp3", "*.mp3"));
        File resultFile = flc.showOpenDialog(Global.getStage());

        if((null != resultFile)&&(resultFile.exists())){
            plHandler.addSongToVariant(resultFile, variant);

            /* Add TitledPane for it */
            TitledPane aSong = new TitledPane();
            aSong.setText(resultFile.getName());

            ImageView imgV;

            /* Add Graphic for song */
            try {
                imgV = new ImageView(WavConverter.imageFromMp3(resultFile));
                AnchorPane lofasz = new AnchorPane();
                lofasz.getChildren().add(imgV);
                aSong.setContent(lofasz);
                mainAccordion.getPanes().add(0, aSong);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }

        }

    }

    private EventHandler<Event> removeVariantRequest = (event) -> {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Delete " + variant + " ?", ButtonType.YES, ButtonType.NO, ButtonType.CANCEL);
        alert.showAndWait();
        if (alert.getResult() != ButtonType.YES) {
            event.consume();
        }else if(!plHandler.removeVariant(variant))
        {
            (new Alert(Alert.AlertType.ERROR, "Unable to remove Variant!")).showAndWait();
            event.consume();
        }
    };

}
