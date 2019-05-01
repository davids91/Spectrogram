    package spectrogram;

    import javafx.event.ActionEvent;
    import javafx.scene.image.ImageView;
    import javafx.scene.image.PixelWriter;
    import javafx.scene.image.WritableImage;
    import javafx.scene.paint.Color;
    import javafx.stage.FileChooser;
    import javafx.stage.Stage;
    import org.datavec.audio.Wave;
    import org.datavec.audio.extension.*;

    import javax.sound.sampled.UnsupportedAudioFileException;
    import java.io.*;
    import java.net.URISyntaxException;

    public class Controller {
        public ImageView imgDisplay;
        private Stage primaryStage;

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
