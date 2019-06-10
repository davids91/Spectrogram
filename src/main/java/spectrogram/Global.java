package spectrogram;

import javafx.scene.Parent;
import javafx.stage.Stage;

public class Global {
    private static Stage primaryStage = null;
    private static Parent variantRoot = null;

    public static void setStage(Main fromApp){
        if(null != fromApp)primaryStage = fromApp.stage;
    }
    public static void setVariantRoot(Main fromApp){
        if(null != fromApp)primaryStage = fromApp.stage;
    }

    public static Stage getStage() {return primaryStage;}
    public static Parent getVariantRoot(){return variantRoot;}
}
