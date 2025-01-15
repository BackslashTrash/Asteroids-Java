import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;

public class BackgroundMusic {
    File backgroundMusic;
    boolean run = true;
    Thread replay;
    public BackgroundMusic() {
        backgroundMusic = new File("background.wav");
        replay = new Thread(() -> {         //Separate thread so it won't stop the game thread from updating
            try {
                play(backgroundMusic);                          //Plays the background music
            } catch (UnsupportedAudioFileException | LineUnavailableException | IOException e) {
                throw new RuntimeException(e);
            }
        });
    }
    public void play(File file) throws UnsupportedAudioFileException, IOException, LineUnavailableException  {
        while (run) {
            AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(file);       //makes an audioinputstream to capture sound from file
            AudioFormat audioFormat = audioInputStream.getFormat();
            DataLine.Info info = new DataLine.Info(SourceDataLine.class, audioFormat);
            SourceDataLine sourceDataLine = (SourceDataLine) AudioSystem.getLine(info);
            sourceDataLine.open(audioFormat);
            sourceDataLine.start();
            byte[] fileBuffer = new byte[4096];         //create buffer
            int read;
            while ((read = audioInputStream.read(fileBuffer, 0, fileBuffer.length)) != -1) {        // read the buffer
                if (!Player.isMute()) {
                    sourceDataLine.write(fileBuffer, 0, read);
                }
            }
            sourceDataLine.drain();     //releases system resource
            sourceDataLine.stop();
            sourceDataLine.close();
        }
    }

    public void startSoundThread() {
        replay.start();
    }
}
