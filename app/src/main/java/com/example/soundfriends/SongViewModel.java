package com.example.soundfriends;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class SongViewModel extends ViewModel {
    private final MutableLiveData<Boolean> mediaPlayerPlaying = new MutableLiveData<>();
    private final MutableLiveData<Boolean> shouldResumeMusic = new MutableLiveData<>();

    public LiveData<Boolean> getShouldResumeMusic() {
        return shouldResumeMusic;
    }

    public void setMediaPlayerPlaying(boolean isPlaying) {
        mediaPlayerPlaying.setValue(isPlaying);
    }
    public void setShouldResumeMusic(boolean shouldResume) {
        shouldResumeMusic.setValue(shouldResume);
    }

}
