package spectrogram_services;

import com.google.gson.JsonObject;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import spectrogram_models.Global;
import spectrogram_models.SongPane;
import spectrogram_models.VariantTab;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Map;
import java.util.Random;

public class VariantTabHandler{

    private String variant;
    private JsonObject varObj = null;
    private VariantTab tab = null;
    private PlaylistHandler plHandler;
    private Accordion mainAccordion;

    /* TODO: Drag&&Drop rearranging the songs */
    /* TODO: Save Last Selected Variant */

    public VariantTabHandler(PlaylistHandler plHandler, String variant){
        tab = new VariantTab();
        tab.setId(variant + "Tab" +  new Random().nextInt());
        tab.setText(variant);
        this.variant = variant;
        tab.setClosable(true);
        if(null != plHandler){
            this.plHandler = plHandler;
            tab.setOnCloseRequest(removeVariantRequest);

            /* Add an Accordion and a titledPane for songs and to add new Music */
            mainAccordion = new Accordion();

            try {
                varObj = plHandler.getVariant(variant);

                /* Load in the songs from the variants */
                for(Map.Entry song: varObj.entrySet()){
                    mainAccordion.getPanes().add(
                        new SongPane(
                            new File(song.getValue().toString().trim().replaceAll("\"",""))
                        )
                    );
                }

            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            /* Add button for adding music */
            VariantTab.getAddSongBtn(
                VariantTab.createAddSongTitledPane(mainAccordion)
            ).setOnAction(actionEvent -> {
                try {
                    addSong();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            });

            /* Set the content of the Tab */
            tab.setContent(VariantTab.createPlaylistContent(mainAccordion));

        }else throw new UnsupportedOperationException("Standalone VariantHandler not supported!");
    }

    public VariantTab getTab(){
        return tab;
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

            /* Add Graphic for song */
            mainAccordion.getPanes().add(0, new SongPane(resultFile));
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
