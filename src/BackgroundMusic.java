import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;

public class BackgroundMusic {
    boolean run = true;
    Thread replay;
    File[] musicList;
    public static int index = 0;
    public BackgroundMusic() {
        File awakening = new File("awakening.wav");
        File skyHigh =  new File("skyhigh.wav");
        File higher = new File("higher.wav");
        File energy = new File("energy.wav");
        File ymca = new File("ymca.wav");
        File mystictides = new File("mystictides.wav");
        musicList = new File[6];
        musicList[0] = awakening;
        musicList[1] =  skyHigh;
        musicList[2] = higher;
        musicList[3] = energy;
        musicList[4] = ymca;
        musicList[5] = mystictides;
        
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
                    if (Player.mouse.lastSong) {
                        reset(sourceDataLine);
                        index--;
                        if (index < 0) {                    //cycling the songs by going down the array
                            index = musicList.length -1;
                        }
                        Player.mouse.lastSong = false;
                    } else if (Player.mouse.nextSong){
                        reset(sourceDataLine);
                        index++;
                        if (index > musicList.length-1) {                   //cycling the songs by going up the array
                            index = 0;
                        }
                        Player.mouse.nextSong = false;
                    } else {
                        sourceDataLine.write(fileBuffer, 0, read);
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
