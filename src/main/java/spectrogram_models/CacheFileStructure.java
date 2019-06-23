package spectrogram_models;

import javafx.scene.image.Image;
import spectrogram_services.WavConverter;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.HashMap;

public class CacheFileStructure extends HashMap<String, SerializableImage> implements Serializable {

    public boolean hasFile(File file){
        if(this.containsKey(file.getAbsolutePath())) return true;
            else return false;
    }

    public Image putFile(File mp3File) throws FileNotFoundException {
        Image cachedImg = WavConverter.imageFromMp3(mp3File);
        put(mp3File.getAbsolutePath(),new SerializableImage(cachedImg));
        return cachedImg;
    }

    public Image getFile(File mp3File) throws FileNotFoundException {
        if(hasFile(mp3File)){
            return this.get(mp3File.getAbsolutePath()).getImage();
        }else{
            return putFile(mp3File);
        }
    }
}
