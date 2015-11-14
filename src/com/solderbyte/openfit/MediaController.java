package com.solderbyte.openfit;

import com.solderbyte.openfit.util.OpenFitIntent;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.util.Log;

public class MediaController {
    private static final String LOG_TAG = "OpenFit:MediaController";

    public static String CURRENT_TRACK = "Open Fit Track";
    public static byte CURRENT_VOLUME = 15;
    public static int MAX_VOLUME = 0;
    public static int ACT_VOLUME = 0;
    public static int CURRENT_PLAYER = 0; // 0 = stock, 1 = spotify
    public static AudioManager audioManager = null;
    public static Context context = null;

    public static void init(Context cntxt) {
        Log.d(LOG_TAG, "Initializing MediaController");
        context = cntxt;
        audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        MAX_VOLUME = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        ACT_VOLUME = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        CURRENT_VOLUME = (byte) ACT_VOLUME;

        // google play music
        context.registerReceiver(playMusicReceiver, new IntentFilter("com.google.android.music.metachanged"));
        // stock android music
        IntentFilter stock = new IntentFilter("com.android.music.metachanged");
        stock.addAction("com.android.music.queuechanged");
        //stock.addAction("com.android.music.playstatechanged");
        context.registerReceiver(stockMusicReceiver, stock);
        // samsung music player
        context.registerReceiver(samsungMusicReceiver, new IntentFilter("com.sec.android.app.music.metachanged"));
        // spotify
        IntentFilter spotify = new IntentFilter("com.spotify.music.playbackstatechanged");
        spotify.addAction("com.spotify.music.metadatachanged");
        spotify.addAction("com.spotify.music.queuechanged");
        context.registerReceiver(spotifyReceiver, spotify);
        // pandora
        context.registerReceiver(pandoraReceiver, new IntentFilter("com.pandora.android.widget.RemoteBroadcastsReceiver"));

        context.registerReceiver(serviceStopReceiver, new IntentFilter(OpenFitIntent.INTENT_SERVICE_STOP));
    }

    private static BroadcastReceiver playMusicReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(LOG_TAG, "playMusicReceiver");
            CURRENT_PLAYER = 0;
            processIntent(intent);
        }
    };
    
    private static BroadcastReceiver stockMusicReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(LOG_TAG, "stockMusicReceiver");
            CURRENT_PLAYER = 0;
            processIntent(intent);
        }
    };
    
    private static BroadcastReceiver samsungMusicReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(LOG_TAG, "samsungMusicReceiver");
            CURRENT_PLAYER = 0;
            processIntent(intent);
        }
    };
    
    private static BroadcastReceiver spotifyReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(LOG_TAG, "spotifyReceiver");
            CURRENT_PLAYER = 1;
            processIntent(intent);
        }
    };
    
    private static BroadcastReceiver pandoraReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(LOG_TAG, "pandoraReceiver");
            CURRENT_PLAYER = 2;
            processIntent(intent);
        }
    };

    private static BroadcastReceiver serviceStopReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            context.unregisterReceiver(playMusicReceiver);
            context.unregisterReceiver(stockMusicReceiver);
            context.unregisterReceiver(samsungMusicReceiver);
            context.unregisterReceiver(spotifyReceiver);
            context.unregisterReceiver(pandoraReceiver);

            context.unregisterReceiver(serviceStopReceiver);
        }
    };

    public static void processIntent(Intent intent) {
        String action = intent.getAction();
        Log.d(LOG_TAG, "action " + action);
        String artist = getArtist(intent);
        String track = getTrack(intent);
        String mediaTrack = artist + " - " + track;
        Log.d(LOG_TAG, mediaTrack);
        setTrack(mediaTrack);
        Intent msg = new Intent(OpenFitIntent.INTENT_SERVICE_MEDIA);
        msg.putExtra("artist", artist);
        msg.putExtra("track", track);
        msg.putExtra("mediaTrack", mediaTrack);
        context.sendBroadcast(msg);
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

    public static Intent prevTrack() {
        Intent i = null;
        if(CURRENT_PLAYER == 0) {
            i = new Intent("com.android.music.musicservicecommand");
            i.putExtra("command", "previous");
        }
        else if(CURRENT_PLAYER == 1) {
            i = new Intent("com.spotify.mobile.android.ui.widget.PREVIOUS");
        }

        return i;
    }

    public static Intent nextTrack() {
        Intent i = null;
        if(CURRENT_PLAYER == 0) {
            i = new Intent("com.android.music.musicservicecommand");
            i.putExtra("command", "next");
        }
        else if(CURRENT_PLAYER == 1) {
            i = new Intent("com.spotify.mobile.android.ui.widget.NEXT");
        }

        return i;
    }

    public static Intent playTrack() {
        Intent i = null;
        if(CURRENT_PLAYER == 0) {
            i = new Intent("com.android.music.musicservicecommand");
            i.putExtra("command", "togglepause");
        }
        else if(CURRENT_PLAYER == 1) {
            i = new Intent("com.spotify.mobile.android.ui.widget.PLAY");
        }

        return i;
    }
}
