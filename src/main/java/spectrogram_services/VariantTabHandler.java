package spectrogram_services;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
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
import java.util.Map;

public class VariantTabHandler extends Tab {

    private String variant = "";
    private JsonObject varObj = null;
    private PlaylistHandler plHandler = null;
    private Accordion mainAccordion = null;

    public VariantTabHandler(PlaylistHandler plHandler, String variant){
        setText(variant);
        this.variant = variant;
        setClosable(true);
        if(null != plHandler){
            this.plHandler = plHandler;
            setOnCloseRequest(removeVariantRequest);

            /* Add an Accordion and a titledPane for songs and to add new Music */
            mainAccordion = new Accordion();
            TitledPane addMusicTitledPane = new TitledPane();

            try {
                varObj = plHandler.getVariant(variant);

                /* Load in the songs from the variants */
                TitledPane songPane = null;
                for(Map.Entry song: varObj.entrySet()){
                    songPane = new TitledPane();
                    songPane.setText(song.getValue().toString());
                    mainAccordion.getPanes().add(songPane);
                    /* TODO: Load in song image */
                }

            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            /* Add button for adding music */
            mainAccordion.getPanes().add(addMusicTitledPane);
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
