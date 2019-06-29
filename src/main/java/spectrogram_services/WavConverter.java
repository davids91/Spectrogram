package spectrogram_services;

import javafx.scene.image.Image;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import org.datavec.audio.Wave;
import org.datavec.audio.extension.Spectrogram;

import javax.sound.sampled.*;
import java.io.*;
import java.net.URISyntaxException;

/* @OVERVIEW
 * Converts any audio file to a wav format for Datavec to read
 * */
public class WavConverter {

    public static Image imageFromMp3(File mp3File) throws FileNotFoundException {
        InputStream is;
        if(mp3File.exists())
        {
            File resultFile = null;
            try {
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
            double localIntensity = 0.0;
            int x = 0, y = 0, currentIntensity = 0, currentEmphasis = 0;
            for(double[] col : spData) /* one sample time */
            {
                y = 0;
                localIntensity = col[0];
                for(double item : col)
                {
                    currentIntensity = Math.min(255,Math.max(0,(int)((item + localIntensity) * 128)));
                    if(220 < currentIntensity) currentEmphasis = 128;
                    else currentEmphasis = 0;
                    pxWr.setColor(x,y,
                        Color.rgb(
                            currentIntensity,
                            currentIntensity/3 + currentEmphasis,
                            currentEmphasis
                        )
                    );
                    localIntensity = localIntensity*0.3 + item*0.7;
                    y++;
                }
                x++;
            }

            return resImg;
        }else throw new FileNotFoundException("File " + mp3File.getPath() + " doesn't exist or reserved!");
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
