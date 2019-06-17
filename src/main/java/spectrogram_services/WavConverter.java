package spectrogram_services;

import javafx.event.ActionEvent;
import javafx.scene.image.Image;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.datavec.audio.Wave;
import org.datavec.audio.extension.Spectrogram;

import javax.sound.sampled.*;
import java.io.*;
import java.net.URISyntaxException;
import java.net.URL;

/* @OVERVIEW
 * Converts any audio file to a wav format for Datavec to read
 * */
public class WavConverter {

    public static Image imageFromMp3(File mp3File) throws FileNotFoundException {
        //InputStream is = getClass().getResourceAsStream("/sounds/CambioDolor.wav");
        InputStream is;
        if(mp3File.exists())
        {
            File resultFile = null;
            try {
                //WavConverter.createWavFromMp3(getClass().getResource("/sounds/winxp.mp3"), getClass().getResource("/sounds/winxp.wav"));
                resultFile = File.createTempFile("what","isthis");
                WavConverter.wavFromMp3(mp3File, resultFile);
            } catch (UnsupportedAudioFileException | IOException | URISyntaxException e) {
                e.printStackTrace();
            }

            is = new FileInputStream(resultFile);
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
            return resImg;
        }else throw new FileNotFoundException("File " + mp3File.getPath() + " doesn't exist!");
    }


    /* @brief Main Function to convert
     * fromAudio: Audio FIle location
     * toWav: resulting Wav file location
     * */
    public static boolean wavFromMp3(File fromAudio, File toWav) throws UnsupportedAudioFileException, IOException, URISyntaxException {
        /* open stream */
        AudioInputStream mp3Stream = AudioSystem.getAudioInputStream(fromAudio);
        AudioFormat sourceFormat = mp3Stream.getFormat();
        /* create audio format object for the desired stream/audio format
         * this is *not* the same as the file format (wav) */
        AudioFormat convertFormat = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED,
                sourceFormat.getSampleRate(), 16,
                sourceFormat.getChannels(),
                sourceFormat.getChannels() * 2,
                sourceFormat.getSampleRate(),
                false);
        /* create stream that delivers the desired format */
        AudioInputStream converted = AudioSystem.getAudioInputStream(convertFormat, mp3Stream);
        /* write stream into a file with file format wav */
        if(!toWav.exists())
        {
            toWav.createNewFile();
        }
        AudioSystem.write(converted, AudioFileFormat.Type.WAVE, toWav);

        return toWav.exists();
    }
}
