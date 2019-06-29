package spectrogram_models;

import javafx.stage.Stage;
import spectrogram.Main;
import spectrogram_services.CacheFileHandler;

public class Global {
    private static Stage primaryStage = null;
    private static CacheFileHandler ccfh = null;

    public static void setStage(Main fromApp){
        if(null != fromApp)primaryStage = fromApp.stage;
    }
    public static void setCache(Main fromApp){
        if(null != fromApp)ccfh = fromApp.ccfh;
    }

    public static Stage getStage() {return primaryStage;}
    public static CacheFileHandler getCache() {return ccfh;}
}
