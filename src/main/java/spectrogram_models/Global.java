package spectrogram_models;

import javafx.scene.Parent;
import javafx.stage.Stage;
import spectrogram.Main;

public class Global {
    private static Stage primaryStage = null;

    public static void setStage(Main fromApp){
        if(null != fromApp)primaryStage = fromApp.stage;
    }

    public static Stage getStage() {return primaryStage;}
}