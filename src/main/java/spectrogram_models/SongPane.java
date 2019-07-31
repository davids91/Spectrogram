package spectrogram_models;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.HBox;
import spectrogram_services.VariantTabHandler;

import java.io.IOException;
import java.io.InvalidObjectException;

public class SongPane extends TitledPane{

    private ImageView imgV;
    private ScrollPane scrP;
    private Separator fill = new Separator();

    private JsonObject songObj;
    private boolean loaded = false;

    public SongPane(JsonElement song, VariantTabHandler parent) throws InvalidObjectException {
        if(PlaylistStructure.isValidSong(song)){
            songObj = song.getAsJsonObject();

            /* Content */
            imgV = new ImageView();
            scrP = null;
            scrP = new ScrollPane();
            setText(PlaylistStructure.getSongAsFile(song).getName());
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
        }else throw new InvalidObjectException("Unable to initialize Song DIsplay as the given JsonObject is not a valid song!");
    }

    private void loadImage() throws IOException, NoSuchFieldException {  loadImage(false);  }
    private void loadImage(boolean forceLoading) throws IOException, NoSuchFieldException {
        if(!loaded || forceLoading){

            /* Content */
            imgV.setImage( Global.getCache().getCachedSpectrogram(PlaylistStructure.getSongAsFile(songObj)) );
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
                } catch (IOException | NoSuchFieldException e) {
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

    public JsonObject getObject(){
        return songObj;
    }
}
