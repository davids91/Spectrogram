package spectrogram_services;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Tab;
import spectrogram.Global;

public class VariantTabHandler extends Tab {

    public VariantTabHandler(PlaylistHandler plHandler, String variant){
        setText(variant);
        setClosable(true);
        if(null != plHandler){
            setOnCloseRequest((event) -> {
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Delete " + variant + " ?", ButtonType.YES, ButtonType.NO, ButtonType.CANCEL);
                alert.showAndWait();
                if (alert.getResult() != ButtonType.YES) {
                    event.consume();
                }else if(!Global.plHandler.removeVariant(variant))
                {
                    (new Alert(Alert.AlertType.ERROR, "Unable to remove Variant!")).showAndWait();
                    event.consume();
                }
            });
        }else throw new UnsupportedOperationException("Standalone VariantHandler not supported!");
    }

}
