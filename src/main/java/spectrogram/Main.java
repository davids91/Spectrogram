package spectrogram;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.stage.Stage;
import spectrogram_models.Global;
import spectrogram_services.CacheFileHandler;

import java.io.File;

public class Main extends Application {

    public Stage stage;
    public CacheFileHandler ccfh;

    @Override
    public void start(Stage primaryStage) throws Exception{
        FXMLLoader mainLoader = new FXMLLoader(getClass().getClassLoader().getResource("fxml/main.fxml"));

        /* Set up Global */
        ccfh = new CacheFileHandler(new File(".cache"));
        stage = primaryStage;

        Global.setStage(this);
        Global.setCache(this);

        /* Load UI */
        Parent root = mainLoader.load();
        primaryStage.setTitle("The world is my instrument, this is my playlist.");
        primaryStage.setScene(new Scene(root, 800, 600));


        /* Set Global Keystrokes */
        primaryStage.getScene().getAccelerators().put( /* Register Ctrl + O keystroke reaction */
        new KeyCodeCombination(KeyCode.O, KeyCombination.CONTROL_DOWN),
        () -> ((Controller)mainLoader.getController()).openExistingPlayList());

        primaryStage.show();

    }


    public static void main(String[] args) {
        launch(args);
    }
}
