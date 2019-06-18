package spectrogram_services;

import javafx.scene.image.Image;
import spectrogram_models.CacheFileStructure;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;

public class FileCacheHandler {
    private File cacheFile = null;
    private CacheFileStructure cache = null;

    public FileCacheHandler(File whereTo) throws IOException, ClassNotFoundException {
        if(null != whereTo){
            cacheFile = whereTo;
            if(!cacheFile.exists()){
                cacheFile.createNewFile();
                cache = new CacheFileStructure();
            }else{
                FileInputStream f = new FileInputStream(cacheFile);
                ObjectInputStream s = new ObjectInputStream(f);
                cache =  (CacheFileStructure) s.readObject();
                s.close();
            }
        }else throw new FileNotFoundException("Unable to create the Cache file");
    }

    public boolean moveCacheTo(File destFile){
        if(
            ((null != cacheFile)&&(null != destFile))
            &&(!cacheFile.getAbsolutePath().equalsIgnoreCase(destFile.getAbsolutePath()))
        ){
            try{
                Files.move(
                    Paths.get(cacheFile.getAbsolutePath()), /* From */
                    Paths.get(destFile.getAbsolutePath()) /* To */
                );
                return true;
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
        }else return false; /* Unable to move File! */
    }

    public Image getCachedSpectrogram(File mp3File) throws IOException {
        Image retImg = cache.getFile(mp3File);
        if(cache.isDirty())cache.writeCacheToFile(cacheFile);
        return retImg;
    }

    /* TODO: New thread for maintenance */
    /* TODO: New thread for exploration */

}
