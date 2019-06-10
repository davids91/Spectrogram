package spectrogram_services;

import javafx.beans.value.ChangeListener;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.SingleSelectionModel;
import javafx.scene.control.Tab;
import spectrogram.Global;


public class VariantTabHandler extends Tab {

    private String variant = "";

    public VariantTabHandler(PlaylistHandler plHandler, String variant){
        setText(variant);
        this.variant = variant;
        setClosable(true);
        if(null != plHandler){

            setOnCloseRequest(removeVariantRequest);

        }else throw new UnsupportedOperationException("Standalone VariantHandler not supported!");
    }

    private EventHandler<Event> removeVariantRequest = (event) -> {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Delete " + variant + " ?", ButtonType.YES, ButtonType.NO, ButtonType.CANCEL);
        alert.showAndWait();
        if (alert.getResult() != ButtonType.YES) {
            event.consume();
        }else if(!Global.plHandler.removeVariant(variant))
        {
            (new Alert(Alert.AlertType.ERROR, "Unable to remove Variant!")).showAndWait();
            event.consume();
        }
    };

}
