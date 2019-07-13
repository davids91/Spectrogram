package spectrogram_models;

import javafx.scene.control.*;

import java.util.Random;

/* TODO: play Button Functionality */
public class VariantTab extends Tab {
    private static Button makeAddSongBtn(){
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

    public static ScrollPane createPlaylistContent(Accordion mainAccordion){
        ScrollPane scrPane = new ScrollPane(mainAccordion);
        scrPane.setFitToWidth(true);
        scrPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

        return scrPane;
    }

}
