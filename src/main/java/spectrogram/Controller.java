    package spectrogram;

    import javafx.event.ActionEvent;
    import javafx.scene.control.Alert;
    import javafx.scene.control.Button;
    import javafx.scene.control.Label;
    import javafx.scene.image.ImageView;
    import javafx.scene.image.PixelWriter;
    import javafx.scene.image.WritableImage;
    import javafx.scene.input.KeyCode;
    import javafx.scene.input.KeyCodeCombination;
    import javafx.scene.input.KeyCombination;
    import javafx.scene.paint.Color;
    import javafx.stage.FileChooser;
    import javafx.stage.Stage;
    import org.datavec.audio.Wave;
    import org.datavec.audio.extension.*;

    import java.io.*;
    import java.util.prefs.Preferences;

    public class Controller {
        public ImageView imgDisplay;
        public Button openDefaultBtn;
        public Label playlistNameLabel;
        public Button setPlayListBtn;
        public Button makeDefBtn;
        private Stage primaryStage;
        private final Preferences userPref = Preferences.userNodeForPackage(Controller.class);

        PlaylistHandler plHandler;
        String playlistPath = userPref.get("defaultPlayList", "");
        private final FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("Salsa Assistant Playlist(*.sap)", "*.sap");

        public void initialize()
        {
            /* Initialize objects */
            plHandler = new PlaylistHandler();

            /* Try to load in default playlist */
            File defPlayList = new File(playlistPath);
            if(
                (!playlistPath.isEmpty())
                    &&(defPlayList.exists())
            ) {
                try {
                    plHandler.openPlayList(defPlayList);
                    playlistOpenedUpdateUI();
                } catch (InvalidPlaylistException e) {
                   userPref.put("defaultPlayList","");/* Playlist is invalid! Let's delete it */
                    e.printStackTrace();
                }
            }else{ /* default playlist doesn't exist */ }
        }

        public void openExistingPlayList()
        {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Open Playlist");
            fileChooser.getExtensionFilters().add(extFilter);
            File resultFile = fileChooser.showOpenDialog(primaryStage);

            if(null != resultFile)
            {
                try {
                    plHandler.openPlayList(resultFile);
                    if(PlaylistHandler.Validity.emptyFile == plHandler.isPlaylistValid())
                    {
                        plHandler.initializePlaylist();
                    }
                    playlistOpenedUpdateUI();
                } catch (InvalidPlaylistException e) {
                    e.printStackTrace(); /* The File given as a playlist is not in acceptable format */
                } catch (PlaylistOverrideException e) {
                    e.printStackTrace(); /* The existing playlist was empty, but also valid..? */
                }
            }
        }

        public void createNewPlayList() throws IOException
        {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Create Playlist File");
            fileChooser.getExtensionFilters().add(extFilter);
            File resultFile = fileChooser.showSaveDialog(primaryStage);

            System.out.println("Playlist file is: " + resultFile.getPath());

            if(null == resultFile)
            {
                if(
                    (resultFile.exists()) /* File exists */
                    ||((!resultFile.exists()) /* Or can be created */
                    &&(resultFile.createNewFile()))
                ){
                    try {
                        plHandler.openPlayList(resultFile);
                        plHandler.initializePlaylist();
                        playlistOpenedUpdateUI();
                    } catch (InvalidPlaylistException | PlaylistOverrideException e) {
                        e.printStackTrace();
                    }
                }else throw new IOException("Unable to create new playlist file!");
            }else{ /* Invalid file was given, maybe Cancelled */ }

        }

        public void playlistInvalidUpdateUI()
        {
            playlistNameLabel.setText("<< Playlist name >>");
            openDefaultBtn.setDisable(true); /* TODO: Make a distinction for loaded playlist and valid default playlist */
            makeDefBtn.setDisable(true);
        }

        public void playlistOpenedUpdateUI()
        {
            if(PlaylistHandler.Validity.unknownFormat.ordinal() < plHandler.isPlaylistValid().ordinal())
            {
                try {
                    playlistNameLabel.setText(plHandler.getPlayListName());
                    makeDefBtn.setDisable(false);
                } catch (InvalidPlaylistException e) {
                    e.printStackTrace();
                    playlistInvalidUpdateUI();
                }
            }
            else
            {
                playlistInvalidUpdateUI();
            }
        }

        public void setDefaultPlaylist()
        {
            if(PlaylistHandler.Validity.notAFile.ordinal() < plHandler.isPlaylistValid().ordinal())
            {
                String path = "";
                try {
                    path = plHandler.getPlayListPath();
                } catch (InvalidPlaylistException e) {
                    e.printStackTrace();
                }
                finally
                {
                    userPref.put("defaultPlayList", path);
                    System.out.println("Playlist " + path + "Set as default!");
                }
            }
            else
            {
                Alert alert = new Alert( Alert.AlertType.ERROR);
                alert.setContentText("Invalid playlist!");
                alert.showAndWait();//.filter((response) -> response == ButtonType.OK);
            }
        }

        public void setStage(Stage stg)
        {
            primaryStage = stg; /* Set the stage */
            primaryStage.getScene().getAccelerators().put( /* Register Ctrl + O keystroke reaction */
                    new KeyCodeCombination(KeyCode.O, KeyCombination.CONTROL_DOWN),
                    () -> openExistingPlayList());
        }

        public void loadMusic(ActionEvent actionEvent) {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Open Resource File");
            //File inputFile = fileChooser.showOpenDialog(primaryStage);
            //File resultFile = fileChooser.showSaveDialog(primaryStage);
            /*try {
                //WavConverter.createWavFromMp3(getClass().getResource("/sounds/winxp.mp3"), getClass().getResource("/sounds/winxp.wav"));
                WavConverter.createWavFromMp3(inputFile, resultFile);
            } catch (UnsupportedAudioFileException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (URISyntaxException e) {
                e.printStackTrace();
            } */

            File toAnalyze = fileChooser.showOpenDialog(primaryStage);

            //InputStream is = getClass().getResourceAsStream("/sounds/CambioDolor.wav");
            InputStream is;
            try {
                if(toAnalyze.exists())
                {
                    is = new FileInputStream(toAnalyze);
                    Wave wave = new Wave(is);
                    Spectrogram sptr = new Spectrogram(wave);

                    double[][] spData = sptr.getNormalizedSpectrogramData();
                    WritableImage resImg = new WritableImage(spData.length,spData[0].length);
                    PixelWriter pxWr = resImg.getPixelWriter();

                    int x = 0, y = 0;
                    boolean redRow = false;
                    double fillage = 0;
                    for(double[] col : spData) /* one sample time */
                    {
                        fillage = 0;
                        y = 0;
                        for(double item : col)
                        {
                            if(fillage <= (spData[0].length) * 0.2)
                            { /* quite filled time */
                                resImg.getPixelWriter().setColor(x,y, Color.rgb((int)(item * 255),0,0));
                            }
                            else
                            { /* not quite filled time */
                                resImg.getPixelWriter().setColor(x,y, Color.rgb((int)(item * 255),(int)(item * 255),(int)(item * 255)));
                            }
                            fillage += item;
                            //System.out.println("data: " + item + "\n");
                            y++;
                        }
                        x++;
                    }

                    System.out.println("Done! Image size is: " + x + "," + y);
                    imgDisplay.setFitWidth(x);
                    imgDisplay.setFitHeight(y);
                    imgDisplay.setImage(resImg);
                }else{ /* File doesn't exist */ }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }

        }
    }
