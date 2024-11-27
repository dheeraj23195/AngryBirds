package io.github.angrybirdsbox2d;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;

public class AudioManager {
    private static AudioManager manager;
    private Music bgMusic;
    private float musicVol;
    private float sfxVol;

    private AudioManager() {
        musicVol = 0.0f;
        sfxVol = 0.5f;
        setupMusic();
    }

    public static AudioManager getInstance() {
        if (manager == null) {
            manager = new AudioManager();
        }
        return manager;
    }

    private void setupMusic() {
        bgMusic = Gdx.audio.newMusic(Gdx.files.internal("music/background_music.mp3"));
        bgMusic.setLooping(true);
        bgMusic.setVolume(musicVol);
    }

    public void playBackgroundMusic() {
        if (bgMusic != null && !bgMusic.isPlaying()) {
            bgMusic.play();
        }
    }

    public void stopBackgroundMusic() {
        if (bgMusic != null && bgMusic.isPlaying()) {
            bgMusic.pause();
        }
    }

    public void setMusicVolume(float vol) {
        this.musicVol = vol;
        if (bgMusic != null) {
            bgMusic.setVolume(vol);
        }
    }

    public void setSoundVolume(float vol) {
        this.sfxVol = vol;
    }

    public float getMusicVolume() {
        return musicVol;
    }

    public float getSoundVolume() {
        return sfxVol;
    }

    public void dispose() {
        if (bgMusic != null) {
            bgMusic.dispose();
        }
    }
}
