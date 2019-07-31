package spectrogram_services;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import spectrogram_models.Global;
import spectrogram_models.PlaylistStructure;
import spectrogram_models.SongPane;
import spectrogram_models.VariantTab;

import java.io.File;
import java.io.InvalidObjectException;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

public class VariantTabHandler{

    private String variant;
    private JsonObject variantObject = null;
    private VariantTab tab;
    private PlaylistHandler plHandler;
    private Accordion mainAccordion;

    public VariantTabHandler(PlaylistHandler plHandler, String variant){
        tab = new VariantTab();
        tab.setId(variant + "Tab" +  new Random().nextInt());
        tab.setText(variant);
        this.variant = variant;
        tab.setClosable(true);
        if(null != plHandler){
            this.plHandler = plHandler;
            tab.setOnCloseRequest(removeVariantRequest);
            mainAccordion = new Accordion(); /* Add an Accordion and a titledPane for songs and to add new Music */
            try {
                variantObject = plHandler.getVariant(variant); /* Load in the songs from the variants */
                loadSongs();
            } catch (InterruptedException | InvalidObjectException e) {
                e.printStackTrace();
            }

            /* Add button for adding music */
            VariantTab.getAddSongBtn(
                VariantTab.createAddSongTitledPane(mainAccordion)
            ).setOnAction(actionEvent -> addSong());

            /* Set the content of the Tab */
            tab.setContent(VariantTab.createPlaylistContent(mainAccordion));

        }else throw new UnsupportedOperationException("Standalone VariantHandler not supported!");
    }

    public void putAAfterB(SongPane a, SongPane b){
        System.out.println("Putting " + a.getText() + " after " + b.getText());

        try {
            JsonElement AObj = a.getObject();
            JsonElement BObj = b.getObject();
            int indexA = PlaylistStructure.getSongIndex(AObj,variantObject);
            int indexB = PlaylistStructure.getSongIndex(BObj,variantObject);
            if(indexA < indexB){ /* Song is moving up */
                for (int i = indexA + 1; i <= indexB; i++){
                    addSongIntoVarAtIndexI(
                        PlaylistStructure.getSongAtIndex(variantObject,i),i - 1
                    );
                }
            }else { /* Song is moving down */
                for (int i = indexA - 1; i >= indexB; i--){
                    addSongIntoVarAtIndexI(
                        PlaylistStructure.getSongAtIndex(variantObject,i),i + 1
                    );
                }
            }
            addSongIntoVarAtIndexI(AObj,indexB);
            loadSongs();
        } catch (InvalidObjectException e) {
            e.printStackTrace();
        }
    }

    private void addSongIntoVarAtIndexI(JsonElement song, int index) throws InvalidObjectException {
        if(PlaylistStructure.isValidSong(song)){
            if(variantObject.has(Integer.toString(index)))
                variantObject.remove(Integer.toString(index));
            variantObject.add(Integer.toString(index),song);
        }else throw new InvalidObjectException("Unable to add song to Playlist as it is invalid!");
    }

    private void loadSongs() throws InvalidObjectException { /* TODO: Key to be used, song index to be eliminated */
        mainAccordion.getPanes().clear();
        List<Map.Entry<String, JsonElement>> songs = PlaylistStructure.getSorted(variantObject);
        for(Map.Entry<String, JsonElement> song: songs){
            mainAccordion.getPanes().add(new SongPane(song.getValue(),this));
        }
    }

    public VariantTab getTab(){
        return tab;
    }
    /* TODO: Drag song into new Variant */
    /* TODO: open Last used Folder */
    private void addSong(){ /* TODO: remove song */
        FileChooser flc = new FileChooser();
        flc.setTitle("Add song to variant " + variant);
        flc.setSelectedExtensionFilter(new FileChooser.ExtensionFilter("Musica mp3", "*.mp3"));
        List<File> resultFiles = flc.showOpenMultipleDialog(Global.getStage());

        for(File resultFile : resultFiles){
            if((null != resultFile)&&(resultFile.exists())){
                try {
                    mainAccordion.getPanes().add(0,
                        new SongPane(plHandler.addSongToVariant(resultFile, variant),this)
                    );
                } catch (InvalidObjectException e) {
                    e.printStackTrace();
                }
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
