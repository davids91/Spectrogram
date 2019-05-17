package spectrogram;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.stage.Stage;

public class Main extends Application {


    @Override
    public void start(Stage primaryStage) throws Exception{
        Global.primaryStage = primaryStage; /* Set the stage */
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/main.fxml"));
        Parent root = loader.load();
        primaryStage.setTitle("The world is my instrument, this is my playlist.");
        primaryStage.setScene(new Scene(root, 800, 600));

        Global.primaryStage.getScene().getAccelerators().put( /* Register Ctrl + O keystroke reaction */
        new KeyCodeCombination(KeyCode.O, KeyCombination.CONTROL_DOWN),
        () -> ((Controller)loader.getController()).openExistingPlayList());

        primaryStage.show();

    }


    public static void main(String[] args) {
        launch(args);
    }
}
