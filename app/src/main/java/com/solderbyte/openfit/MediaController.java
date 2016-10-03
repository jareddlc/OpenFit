package com.solderbyte.openfit;

import com.solderbyte.openfit.util.OpenFitIntent;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.os.Build;
import android.util.Log;
import android.view.KeyEvent;

import java.util.Calendar;

public class MediaController {
    private static final String LOG_TAG = "OpenFit:MediaController";

    public static String CURRENT_TRACK = "Open Fit Track";
    public static byte CURRENT_VOLUME = 15;
    public static int MAX_VOLUME = 0;
    public static int ACT_VOLUME = 0;
    public static int CURRENT_PLAYER = 0; // 0 = stock, 1 = spotify
    public static int CURRENT_METHOD = 0; // 0 = music service command, 1 = keycode
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
        // context.registerReceiver(playMusicReceiver, new IntentFilter("com.google.android.music.metachanged"));
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
        context.registerReceiver(mediacontrollerReceiver, new IntentFilter(OpenFitIntent.INTENT_MEDIACONTROLLER_METHOD));
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

    private static BroadcastReceiver mediacontrollerReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(LOG_TAG, "mediacontrollerReceiver");
            String msg = intent.getStringExtra(OpenFitIntent.INTENT_EXTRA_MSG);
            if(msg.equals("true")) {
                CURRENT_METHOD = 1;
                Log.d(LOG_TAG, "keycode");
            }
            else {
                CURRENT_METHOD = 0;
            }
        }
    };

    private static BroadcastReceiver serviceStopReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            //context.unregisterReceiver(playMusicReceiver);
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
        String artist = "Unsupported Player Artist";
        try {
            if(intent.hasExtra("artist")) {
                artist = intent.getStringExtra("artist");
                if(artist == null) {
                    artist = "OpenFit Artist";
                }
            }
        }
        catch(RuntimeException e) {
            Log.e(LOG_TAG, "Could not get artist: " + e);
        }

        return artist;
    }

    public static String getAlbum(Intent intent) {
        String album = "Unsupported Player Album";
        try {
            if(intent.hasExtra("album")) {
                album = intent.getStringExtra("album");
                if(album == null) {
                    album = "OpenFit Album";
                }
            }
        }
        catch(RuntimeException e) {
            Log.e(LOG_TAG, "Could not get album: " + e);
        }

        return album;
    }

    public static String getTrack(Intent intent) {
        String track = "Unsupported Player Track";
        try {
            if(intent.hasExtra("track")) {
                track = intent.getStringExtra("track");
                if(track == null) {
                    track = "OpenFit Track";
                }
            }
        }
        catch (RuntimeException e) {
            Log.e(LOG_TAG, "Could not get track: " + e);
        }
        return track;
    }

    public static Intent prevTrack() {
        Intent i = null;
        if(CURRENT_METHOD == 0) {
            if(CURRENT_PLAYER == 0) {
                i = new Intent("com.android.music.musicservicecommand");
                i.putExtra("command", "previous");
            }
            else if(CURRENT_PLAYER == 1) {
                i = new Intent("com.spotify.mobile.android.ui.widget.PREVIOUS");
            }
        }
        else {
            i = new Intent();
            sendKeycode(KeyEvent.KEYCODE_MEDIA_PREVIOUS);
        }

        return i;
    }

    public static Intent nextTrack() {
        Intent i = null;
        if(CURRENT_METHOD == 0) {
            if(CURRENT_PLAYER == 0) {
                i = new Intent("com.android.music.musicservicecommand");
                i.putExtra("command", "next");
            }
            else if(CURRENT_PLAYER == 1) {
                i = new Intent("com.spotify.mobile.android.ui.widget.NEXT");
            }
        }
        else {
            i = new Intent();
            sendKeycode(KeyEvent.KEYCODE_MEDIA_NEXT);
        }

        return i;
    }

    public static Intent playTrack() {
        Intent i = null;
        if(CURRENT_METHOD == 0) {
            if(CURRENT_PLAYER == 0) {
                i = new Intent("com.android.music.musicservicecommand");
                i.putExtra("command", "togglepause");
            }
            else if(CURRENT_PLAYER == 1) {
                i = new Intent("com.spotify.mobile.android.ui.widget.PLAY");
            }
        }
        else {
            i = new Intent();
            sendKeycode(KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE);
        }

        return i;
    }

    private static void sendKeycode(int keyCode) {
        Log.d(LOG_TAG, "sendKeycode");
        long eventTime = Calendar.getInstance().getTimeInMillis() - 1;

        KeyEvent downEvent = new KeyEvent(eventTime, eventTime, KeyEvent.ACTION_DOWN, keyCode, 0);
        sendKeyevent(downEvent);

        eventTime++;
        KeyEvent upEvent = new KeyEvent(eventTime, eventTime, KeyEvent.ACTION_UP, keyCode, 0);
        sendKeyevent(upEvent);
    }

    private static void sendKeyevent(KeyEvent keyEvent)  {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            // use AudioManager.dispatchKeyEvent() from API 19 (KitKat)
            // http://stackoverflow.com/questions/19890643/android-4-4-play-default-music-player
            audioManager.dispatchMediaKeyEvent(keyEvent);
        }
        else {
            // use broadcasting fake intent method
            // http://stackoverflow.com/questions/12925896/android-emulate-send-action-media-button-intent
            Intent i = new Intent(Intent.ACTION_MEDIA_BUTTON);
            i.putExtra(Intent.EXTRA_KEY_EVENT, keyEvent);
            context.sendOrderedBroadcast(i, null);
        }
    }
}
