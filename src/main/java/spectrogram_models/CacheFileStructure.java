package spectrogram_models;

import javafx.scene.image.Image;
import spectrogram_services.WavConverter;

import java.io.*;
import java.util.HashMap;

public class CacheFileStructure extends HashMap<String, Image> {

    private boolean dirty = true;

    /* @brief: Writes out the whole of the cache to the given File
     * @returns: operation success */
    public boolean writeCacheToFile(File file) throws IOException {
        if(null != file){
            FileOutputStream f = new FileOutputStream(file);
            ObjectOutputStream s = new ObjectOutputStream(f);
            s.writeObject(file);
            s.close();
            dirty = false;
            return true;
        }else return false; /* Unable to write out HashMap */
    }

    public boolean isDirty(){
        return dirty;
    }

    public boolean hasFile(File file){
        if(this.containsKey(file.getAbsolutePath())) return true;
            else return false;
    }

    public Image putFile(File mp3File) throws FileNotFoundException {
        Image cachedImg = WavConverter.imageFromMp3(mp3File);
        put(mp3File.getAbsolutePath(),cachedImg);
        return cachedImg;
    }

    public Image getFile(File mp3File) throws FileNotFoundException {
        if(hasFile(mp3File)){
            return getFile(mp3File);
        }else{
            dirty = true;
            return putFile(mp3File);
        }
    }

}
