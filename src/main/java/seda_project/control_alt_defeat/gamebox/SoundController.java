package seda_project.control_alt_defeat.gamebox;

import javafx.scene.media.AudioClip;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

import java.net.URL;
import java.util.HashMap;

public class SoundController {
    private static SoundController instance;
    private final HashMap<String, Media> musicclips = new HashMap<>();
    private final HashMap<String, AudioClip> sfxclips = new HashMap<>();
    private AudioClip looping;
    private boolean konami = false;
    private int konamiSound = 1;
    private MediaPlayer mediaPlayer;
    private boolean muteState = false;

    public static SoundController getInstance() {
        if (instance == null) {
            instance = new SoundController();
        }
        return instance;
    }

    /**
     * Play Sound file with volume 1
     * @param name the name of the sound file
     */
    //Method for sound Indicators like buttons etc.
    public void play(String name) {
        play(name, 1.0);
    }

    /**
     * Play a Sound file with a specified volume
     * @param name the name of the sound file
     * @param volume the volume of the sound to be played. Double in between 0 and 1
     */
    public void play(String name, double volume) {
        if (muteState) return;
        AudioClip clip = sfxclips.computeIfAbsent(name, key -> {
            URL url = getClass().getResource("/Sounds/" + key + ".wav");
            return new AudioClip(url.toString());
        });

        clip.play(volume);
    }

    /**
     * Play a sound file until it is stopped with a volume of 1
     * @param name the name of the sound file
     */
    public void playLooping(String name) {
        playLooping(name, 1.0);
    }

    /**
     * Play a sound file with a specified volume until it is stopped
     * @param name the name of the sound file
     * @param volume the volume of the sound to be played. Double in between 0 and 1
     */
    public void playLooping(String name, double volume) {
        if (konami) return;

        stopLooping();

        Media media = musicclips.computeIfAbsent(name, key -> {
            URL url = getClass().getResource("/Sounds/" + key + ".wav");
            return new Media(url.toString());
        });

        mediaPlayer = new MediaPlayer(media);
        mediaPlayer.setVolume(volume);
        mediaPlayer.setCycleCount(MediaPlayer.INDEFINITE);
        if (muteState) return;
        mediaPlayer.play();
    }

    /**
     * Stop the currently running music
     */
    public void stopLooping() {
        if (!konami) {
            if (mediaPlayer != null) {
                mediaPlayer.stop();
                mediaPlayer = null;
            }
        }
    }

    /**
     * Activate the Konami Code state in which only the music of Nemesis (Gradius) is played
     */
    public void activateKonami() {
        if (!konami) {
            stopLooping();
            konami = true;
            next();
        }

    }


    /**
     * Radio Player for the Konami code. If one has finished play the next one
     */
    private void next() {
        Media media = new Media(getClass().getResource("/Sounds/konami_" + konamiSound + ".wav").toString());
        mediaPlayer = new MediaPlayer(media);
        mediaPlayer.setOnEndOfMedia(() -> {
            if (konamiSound == 11) {
                konamiSound = 1;
            } else {
                konamiSound++;
            }
            next();
        });
        if (muteState) return;
        mediaPlayer.play();
    }

    /**
     * @return returns the mute state of the sound Controller
     */
    public boolean getMute() {
        return muteState;
    }

    /** Set the mute state of the sound Controller and play/stop the music accordingly
     * @param muteState
     */
    public void setMute(boolean muteState) {
        this.muteState = muteState;
        if (muteState) {
            pause();
        } else {
            resume();
        }
    }


    /**
     * Pause the music
     */
    public void pause() {
        if (mediaPlayer != null) {
            mediaPlayer.pause();
        }
    }

    /**
     * Resume the music
     */
    public void resume() {
        if (mediaPlayer != null) {
            mediaPlayer.play();
        }
    }
}
