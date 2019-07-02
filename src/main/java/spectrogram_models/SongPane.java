package spectrogram_models;

import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;

import java.io.File;
import java.io.IOException;

public class SongPane extends TitledPane{

    private ImageView imgV = null;
    private ScrollPane scrP = null;
    private Separator fill = new Separator();

    private File songFile = null;
    private boolean loaded = false;

    public SongPane(File songFile){
        this.songFile = songFile;

        /* Content */
        imgV = new ImageView();
        scrP = new ScrollPane();
        setText(songFile.getName());
        setGraphic(getSongControls());
    }

    public void loadImage() throws IOException {  loadImage(false);  }
    public void loadImage(boolean forceLoading) throws IOException {
        if(!loaded || forceLoading){

            /* Content */
            imgV.setImage( Global.getCache().getCachedSpectrogram(songFile) );
            scrP.setContent(imgV);

            /* Alignment */
            imgV.setFitHeight(300); /* TODO: Set a static size, display image correctly */
            scrP.setFitToHeight(true);
            scrP.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

            setContent(scrP);
        }
    }

    public void hide(){ setContent(fill); }

    public HBox getSongControls(){
        HBox controlBox = new HBox();

        ProgressBar pbr = new ProgressBar();
        pbr.setProgress(1);

        Button spectroBtn = new Button();
        spectroBtn.setText("*");
        spectroBtn.setOnAction((actionEvent) -> {
            if(spectroBtn.getText().equals("*")){
                spectroBtn.setText("X");
                try {
                    loadImage(); /* TODO: Use another thread with Progressbar */
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }else{
                spectroBtn.setText("*");
                hide();
            }
        });

        controlBox.getChildren().add(spectroBtn);
        controlBox.getChildren().add(pbr);

        return controlBox;
    }
}
