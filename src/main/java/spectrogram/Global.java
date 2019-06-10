package spectrogram;

import javafx.application.Application;
import javafx.stage.Stage;

public class Global {
    private static Stage primaryStage = null;

    public static void setStage(Main fromApp){
        if(null != fromApp)primaryStage = fromApp.stage;
    }

    public static Stage getStage() {return primaryStage;}
}
