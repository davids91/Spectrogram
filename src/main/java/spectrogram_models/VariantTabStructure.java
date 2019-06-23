package spectrogram_models;

import javafx.scene.control.Accordion;
import javafx.scene.control.Button;
import javafx.scene.control.TitledPane;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Random;

public class VariantTabStructure {
    public static Button makeAddSongBtn(){
        Button addMusicBtn = new Button();
        addMusicBtn.setId("addMusicBtn" + new Random().nextInt());
        addMusicBtn.setText("+");
        return addMusicBtn;
    }

    public static Button getAddSongBtn(TitledPane songPane) throws ClassCastException{
        return (Button)songPane.getGraphic();
    }

    public static TitledPane createAddSongTitledPane(Accordion mainAccordion){
        TitledPane addMusicTitledPane = new TitledPane();
        addMusicTitledPane.setGraphic(makeAddSongBtn());
        mainAccordion.getPanes().add(addMusicTitledPane);
        return addMusicTitledPane;
    }

    public static Accordion createVariantAccordion(){
        Accordion mainAccordion = new Accordion();
        return mainAccordion;
    }

}
