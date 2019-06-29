package spectrogram_services;

import com.google.gson.JsonObject;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import spectrogram_models.Global;
import spectrogram_models.PlayListAccordion;
import spectrogram_models.SongStructure;
import spectrogram_models.VariantTabStructure;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Map;

public class VariantTabHandler extends Tab {

    private String variant;
    private JsonObject varObj = null;
    private PlaylistHandler plHandler;
    private PlayListAccordion mainAccordion;

    public VariantTabHandler(PlaylistHandler plHandler, String variant){
        setText(variant);
        this.variant = variant;
        setClosable(true);
        if(null != plHandler){
            this.plHandler = plHandler;
            setOnCloseRequest(removeVariantRequest);

            /* Add an Accordion and a titledPane for songs and to add new Music */
            mainAccordion = VariantTabStructure.createVariantAccordion();

            try {
                varObj = plHandler.getVariant(variant);

                /* Load in the songs from the variants */
                for(Map.Entry song: varObj.entrySet()){
                    mainAccordion.getPanes().add(
                        SongStructure.getSongTitledPane(
                            new File(song.getValue().toString().trim().replaceAll("\"","")),
                            mainAccordion
                        )
                    );
                }

            } catch (InterruptedException | IOException e) {
                e.printStackTrace();
            }

            /* Add button for adding music */
            VariantTabStructure.getAddSongBtn(
                VariantTabStructure.createAddSongTitledPane(mainAccordion)
            ).setOnAction(actionEvent -> {
                try {
                    addSong();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            });

            /* Set the content of the Tab */
            this.setContent(mainAccordion);

        }else throw new UnsupportedOperationException("Standalone VariantHandler not supported!");
    }

    /* TODO: Last used Folder */
    /* TODO: Add multiple songs at once */
    private void addSong() throws FileNotFoundException {
        FileChooser flc = new FileChooser();
        flc.setTitle("Add song to variant " + variant);
        flc.setSelectedExtensionFilter(new FileChooser.ExtensionFilter("Musica mp3", "*.mp3"));
        File resultFile = flc.showOpenDialog(Global.getStage());

        if((null != resultFile)&&(resultFile.exists())){
            plHandler.addSongToVariant(resultFile, variant);

            /* Add TitledPane for it */
            TitledPane aSong = null;
            try {
                aSong = SongStructure.getSongTitledPane(resultFile,mainAccordion);
            } catch (IOException e) {
                e.printStackTrace();
            }

            /* Add Graphic for song */
            mainAccordion.getPanes().add(0, aSong);

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
