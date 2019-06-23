package spectrogram_models;

import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Serializable;

public class SerializableImage implements Serializable {
    private byte[] data = null;

    public SerializableImage(Image img) throws IOException {
        putImage(img);
    }

    public Image getImage() throws IOException {
        ByteArrayInputStream bais = new ByteArrayInputStream(data);
        return SwingFXUtils.toFXImage(ImageIO.read(bais), null);
    }

    public void putImage(Image img) throws IOException {
        BufferedImage bImage = SwingFXUtils.fromFXImage(img, null);
        ByteArrayOutputStream s = new ByteArrayOutputStream();
        ImageIO.write(bImage, "png", s);
        data = s.toByteArray();
        s.close();
    }
}
