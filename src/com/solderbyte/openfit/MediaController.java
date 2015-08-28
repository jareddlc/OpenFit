package com.solderbyte.openfit;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.view.KeyEvent;

public class MediaController {

    public static String CURRENT_TRACK = "Open Fit Track";
    public static byte CURRENT_VOLUME = 15;
    public static int MAX_VOLUME = 0;
    public static int ACT_VOLUME = 0;
    public static AudioManager audioManager = null;

    public static void init(Context context) {
        audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        MAX_VOLUME = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        ACT_VOLUME = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        CURRENT_VOLUME = (byte) ACT_VOLUME;
    }

    public static IntentFilter getIntentFilter() {
        IntentFilter media = new IntentFilter();
        media.addAction("com.android.music.metachanged");
        media.addAction("com.htc.music.metachanged");
        media.addAction("fm.last.android.metachanged");
        media.addAction("com.sec.android.app.music.metachanged");
        media.addAction("com.nullsoft.winamp.metachanged");
        media.addAction("com.amazon.mp3.metachanged");
        media.addAction("com.miui.player.metachanged");
        media.addAction("com.real.IMP.metachanged");
        media.addAction("com.sonyericsson.music.metachanged");
        media.addAction("com.rdio.android.metachanged");
        media.addAction("com.samsung.sec.android.MusicPlayer.metachanged");
        media.addAction("com.andrew.apollo.metachanged");
        return media;
    }

    public static void setTrack(String track) {
        CURRENT_TRACK = track;
    }

    public static String getTrack() {
        return CURRENT_TRACK;
    }

    public static void setVolume(byte vol) {
        if(audioManager != null) {
            if(CURRENT_VOLUME != vol) {
                audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, vol, AudioManager.FLAG_PLAY_SOUND);
            }
            /*if(CURRENT_VOLUME < vol) {
                audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_RAISE, AudioManager.FLAG_PLAY_SOUND);
            }
            else if(CURRENT_VOLUME > vol){
                audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_LOWER, AudioManager.FLAG_PLAY_SOUND);
            }*/
        }
        CURRENT_VOLUME = vol;
    }

    public static byte getVolume() {
        return CURRENT_VOLUME;
    }
    
    public static byte getActualVolume() {
        ACT_VOLUME = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        return (byte) ACT_VOLUME;
    }

    public static String getArtist(Intent intent) {
        String artist = intent.getStringExtra("artist");
        if(artist == null) {
            artist = "OpenFit Artist";
        }
        return artist;
    }

    public static String getAlbum(Intent intent) {
        String album = intent.getStringExtra("album");
        if(album == null) {
            album = "OpenFit Album";
        }
        return album;
    }

    public static String getTrack(Intent intent) {
        String track = intent.getStringExtra("track");
        if(track == null) {
            track = "OpenFit Track";
        }
        return track;
    }

    public static Intent prevTrackDown() {
        Intent i = new Intent(Intent.ACTION_MEDIA_BUTTON);
        i.putExtra(Intent.EXTRA_KEY_EVENT, new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_MEDIA_PREVIOUS));
        return i;
    }

    public static Intent prevTrackUp() {
        Intent i = new Intent(Intent.ACTION_MEDIA_BUTTON);
        i.putExtra(Intent.EXTRA_KEY_EVENT, new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_MEDIA_PREVIOUS));
        return i;
    }

    public static Intent nextTrackDown() {
        Intent i = new Intent(Intent.ACTION_MEDIA_BUTTON);
        i.putExtra(Intent.EXTRA_KEY_EVENT, new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_MEDIA_NEXT));
        return i;
    }

    public static Intent nextTrackUp() {
        Intent i = new Intent(Intent.ACTION_MEDIA_BUTTON);
        i.putExtra(Intent.EXTRA_KEY_EVENT, new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_MEDIA_NEXT));
        return i;
    }

    public static Intent playTrackDown() {
        Intent i = new Intent(Intent.ACTION_MEDIA_BUTTON);
        i.putExtra(Intent.EXTRA_KEY_EVENT, new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE));
        return i;
    }

    public static Intent playTrackUp() {
        Intent i = new Intent(Intent.ACTION_MEDIA_BUTTON);
        i.putExtra(Intent.EXTRA_KEY_EVENT, new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE));
        return i;
    }
}
