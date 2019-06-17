package spectrogram_models;

import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import spectrogram_services.PlaylistHandler;

public class VariantTab extends Tab {

    private String variant = "";
    private PlaylistHandler plHandler = null;

    public VariantTab(PlaylistHandler plHandler, String variant){
        setText(variant);
        this.variant = variant;
        setClosable(true);
        if(null != plHandler){
            this.plHandler = plHandler;
            setOnCloseRequest(removeVariantRequest);

            /* Add an Accordion and a titledPane to add new Music */
            Accordion mainAccordion = new Accordion();
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
        System.out.println("Adding song to " + variant);
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
