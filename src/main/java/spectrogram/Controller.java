    package spectrogram;

    import javafx.event.ActionEvent;
    import javafx.scene.control.Alert;
    import javafx.scene.control.Button;
    import javafx.scene.control.ButtonType;
    import javafx.scene.image.ImageView;
    import javafx.scene.image.PixelWriter;
    import javafx.scene.image.WritableImage;
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
        private Stage primaryStage;
        private final Preferences userPref = Preferences.userNodeForPackage(Controller.class);

        PlaylistHandler plHandler;

        /* Try to load in default playlist */
        String playlistPath = userPref.get("defaultPlayList", "");

        public void initialize()
        {
            /* Initialize objects */
            plHandler = new PlaylistHandler();

            File defPlayList = new File(playlistPath);
            if(
                (!playlistPath.isEmpty())
                    &&(defPlayList.exists())
            )
            {
                openDefaultBtn.setDisable(false);
            }
            else
            {
                openDefaultBtn.setDisable(true);
            }
        }

        public void openExistingPlayList() throws IOException
        {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Open Playlist");
            File resultFile = fileChooser.showOpenDialog(primaryStage);
            
            if(null != resultFile)
            {
                openPlayList(resultFile);
            }
        }

        public void createNewPlayList() throws IOException
        {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Create Playlist File");
            File resultFile = fileChooser.showSaveDialog(primaryStage);

            if(null == resultFile)
            {
                if(!resultFile.exists())
                {
                    resultFile.createNewFile();
                }
                openPlayList(resultFile);
            }
        }

        public void openPlayList(File playlist)
        {
            if(plHandler.openPlayList(playlist))
            System.out.println("Playlist opened!");
            else
            System.out.println("Unable to open playlist!");
        }

        public void setDefaultPlaylist()
        {
            if(plHandler.isPlaylistValid())
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
            primaryStage = stg;
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