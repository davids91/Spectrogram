package spectrogram_models;

import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.HBox;
import spectrogram_services.VariantTabHandler;

import java.io.File;
import java.io.IOException;

public class SongPane extends TitledPane{

    private ImageView imgV;
    private ScrollPane scrP;
    private Separator fill = new Separator();

    private File songFile;
    private boolean loaded = false;

    public SongPane(File songFile, VariantTabHandler parent){
        this.songFile = songFile;

        /* Content */
        imgV = new ImageView();
        scrP = null;
        scrP = new ScrollPane();
        setText(songFile.getName());
        setGraphic(getSongControls());

        /* DragAndDrop Handlers */ /* TODO: Set a Drag View */
        setOnDragDetected(event -> {
            Dragboard db = startDragAndDrop(TransferMode.MOVE);
            ClipboardContent content = new ClipboardContent();
            content.putString("why do I need this");
            db.setContent(content);
            event.consume();
        });
        setOnDragDropped(event -> {
            parent.putAAfterB(((SongPane)event.getGestureSource()),((SongPane)event.getGestureTarget()));
            event.setDropCompleted(true);
            event.consume();
        });
        setOnDragOver(event -> {
            event.acceptTransferModes(TransferMode.MOVE);
            event.consume();
        });
    }

    private void loadImage() throws IOException {  loadImage(false);  }
    private void loadImage(boolean forceLoading) throws IOException {
        if(!loaded || forceLoading){

            /* Content */
            imgV.setImage( Global.getCache().getCachedSpectrogram(songFile) );
            scrP.setContent(imgV);

            /* Alignment */
            imgV.setFitHeight(300); /* TODO: Set a static size, display image correctly */
            scrP.setFitToHeight(true);
            scrP.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

            setContent(scrP);
            loaded = true;
        }
    }

    private void hide(){ setContent(fill); }

    private HBox getSongControls(){
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
