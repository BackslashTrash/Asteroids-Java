import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;

public class BackgroundMusic {
    File backgroundMusic;
    File skyHigh;
    File higher;
    File energy;
    File ymca;
    boolean run = true;
    Thread replay;
    File[] musicList;
    public static int index = 0;
    public BackgroundMusic() {
        backgroundMusic = new File("background.wav");
        skyHigh =  new File("skyhigh.wav");
        higher = new File("higher.wav");
        energy = new File("energy.wav");
        ymca = new File("ymca.wav");
        musicList = new File[5];
        musicList[0] = backgroundMusic;
        musicList[1] =  skyHigh;
        musicList[2] = higher;
        musicList[3] = energy;
        musicList[4] = ymca;
        
        replay = new Thread(() -> {         //Separate thread so it won't stop the game thread from updating
            try {
                play(musicList);                          //Plays the background music
            } catch (UnsupportedAudioFileException | LineUnavailableException | IOException e) {
                throw new RuntimeException(e);
            }
        });
    }
    public void play(File[] file) throws UnsupportedAudioFileException, IOException, LineUnavailableException  {
        while (run) {
            AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(file[index]);       //makes an audioinputstream to capture sound from file
            AudioFormat audioFormat = audioInputStream.getFormat();
            DataLine.Info info = new DataLine.Info(SourceDataLine.class, audioFormat);
            SourceDataLine sourceDataLine = (SourceDataLine) AudioSystem.getLine(info);
            sourceDataLine.open(audioFormat);
            sourceDataLine.start();
            byte[] fileBuffer = new byte[4096];         //create buffer
            int read;

            while ((read = audioInputStream.read(fileBuffer, 0, fileBuffer.length)) != -1) {        // read the buffer
                if (!Player.isMute()) {
                    if (!Player.mouse.shuffleClicked) {
                        sourceDataLine.write(fileBuffer, 0, read);
                    } else {
                        reset(sourceDataLine);
                        index ++;
                        if (index == 5) {
                            index = 0;
                        }
                        Player.mouse.shuffleClicked = false;
                    }
                } else {
                    reset(sourceDataLine);
                }
            }
            reset(sourceDataLine);
        }
    }

    public void startSoundThread() {
        replay.start();
    }

    private void reset(SourceDataLine sdl) {
        sdl.drain();
        sdl.stop();
        sdl.close();
    }
}
