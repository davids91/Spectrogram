package spectrogram;

import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;

/* @OVERVIEW
 * Converts any audio file to a wav format for Datavec to read
 * */
public class WavConverter {

    /* @brief Main Function to convert
     * fromAudio: Audio FIle location
     * toWav: resulting Wav file location
     * */
    public static boolean createWavFromMp3(File fromAudio, File toWav) throws UnsupportedAudioFileException, IOException, URISyntaxException {
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
