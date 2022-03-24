package com.gempukku.gdx.jam20.level.system;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.utils.Disposable;

import java.util.HashMap;
import java.util.Map;

public class SoundSystem implements Disposable {
    private float musicVolume = 0.5f;
    private float soundVolume = 0.5f;
    private float mainVolume = 1.0f;

    private Music playedMusic;
    private String playedMusicName;
    private boolean switchMusic;

    private AssetManager assetManager = new AssetManager();
    private Map<String, String> soundFileNameMap = new HashMap<>();
    private Map<String, String> musicFileNameMap = new HashMap<>();

    public float getMusicVolume() {
        return musicVolume;
    }

    public float getSoundVolume() {
        return soundVolume;
    }

    public float getMainVolume() {
        return mainVolume;
    }

    public void setSoundVolume(float soundVolume) {
        this.soundVolume = soundVolume;
    }

    public void setMusicVolume(float musicVolume) {
        this.musicVolume = musicVolume;
        if (playedMusic != null) {
            playedMusic.setVolume(musicVolume * mainVolume);
        }
    }

    public void setMainVolume(float mainVolume) {
        this.mainVolume = mainVolume;
        if (playedMusic != null) {
            playedMusic.setVolume(musicVolume * mainVolume);
        }
    }

    public void loadMusic(String name, String fileName) {
        assetManager.load(fileName, Music.class);
        musicFileNameMap.put(name, fileName);
    }

    public void loadSound(String name, String fileName) {
        assetManager.load(fileName, Sound.class);
        soundFileNameMap.put(name, fileName);
    }

    public void playMusic(String name) {
        this.playedMusicName = name;
        this.switchMusic = true;
    }

    public void playSound(String name) {
        Sound sound = assetManager.get(soundFileNameMap.get(name), Sound.class, false);
        if (sound != null)
            sound.play(soundVolume * mainVolume);
    }

    public void update(float delta) {
        assetManager.update();
        if (switchMusic) {
            Music music = assetManager.get(musicFileNameMap.get(playedMusicName), Music.class, false);
            if (music != null) {
                if (playedMusic != null)
                    playedMusic.stop();

                playedMusic = music;
                playedMusic.setLooping(true);
                playedMusic.setVolume(musicVolume * mainVolume);
                playedMusic.play();

                switchMusic = false;
            }
        }
    }

    @Override
    public void dispose() {
        assetManager.dispose();
    }
}
