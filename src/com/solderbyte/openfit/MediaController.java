package com.solderbyte.openfit;

import android.content.Intent;
import android.content.IntentFilter;
import android.view.KeyEvent;

public class MediaController {

    public static final String ACTION_MUSIC_META_CHANGED = "com.android.music.metachanged";
    public static final String ACTION_MUSIC_PLAYSTATE_CHANGED = "com.android.music.playstatechanged";
    public static final String ACTION_MUSIC_STATUS_INFO = "com.sec.android.music.musicservicecommnad.mediainfo";
    public static final String ACTION_VIDEO_META_CHANGED = "com.sec.android.videoplayer.metachanged";
    public static final String ACTION_VIDEO_STATUS_INFO = "com.sec.android.videoplayer.playerstatus";
    public static final String CONTEXT_AWARE_MUSIC_INFO = "android.intent.action.CONTEXT_AWARE_MUSIC_INFO";
    public static String CURRENT_TRACK = "Open Fit Track";
    public static byte CURRENT_VOLUME = 15;

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
        CURRENT_VOLUME = vol;
    }

    public static byte getVolume() {
        return CURRENT_VOLUME;
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

    public static Intent increaseVolumeDown() {
        Intent i = new Intent(Intent.ACTION_MEDIA_BUTTON);
        i.putExtra(Intent.EXTRA_KEY_EVENT, new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_VOLUME_UP));
        return i;
    }

    public static Intent increaseVolumeUp() {
        Intent i = new Intent(Intent.ACTION_MEDIA_BUTTON);
        i.putExtra(Intent.EXTRA_KEY_EVENT, new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_VOLUME_UP));
        return i;
    }
    
    public static Intent decreaseVolumeDown() {
        Intent i = new Intent(Intent.ACTION_MEDIA_BUTTON);
        i.putExtra(Intent.EXTRA_KEY_EVENT, new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_VOLUME_DOWN));
        return i;
    }

    public static Intent decreaseVolumeUp() {
        Intent i = new Intent(Intent.ACTION_MEDIA_BUTTON);
        i.putExtra(Intent.EXTRA_KEY_EVENT, new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_VOLUME_DOWN));
        return i;
    }
}

/*
public static final String SERVICECMD = "com.android.music.musicservicecommand";
public static final String CMDNAME = "command";
public static final String CMDTOGGLEPAUSE = "togglepause";
public static final String CMDSTOP = "stop";
public static final String CMDPAUSE = "pause";
public static final String CMDPREVIOUS = "previous";
public static final String CMDNEXT = "next";


iF.addAction("com.android.music.metachanged");
iF.addAction("com.android.music.playstatechanged");
iF.addAction("com.android.music.playbackcomplete");
iF.addAction("com.android.music.queuechanged");

registerReceiver(mReceiver, iF);
}

private BroadcastReceiver mReceiver = new BroadcastReceiver() {

@Override
public void onReceive(Context context, Intent intent)
{
String action = intent.getAction();
String cmd = intent.getStringExtra("command");
Log.d("mIntentReceiver.onReceive ", action + " / " + cmd);
String artist = intent.getStringExtra("artist");
String album = intent.getStringExtra("album");
String track = intent.getStringExtra("track");
Log.d("Music",artist+":"+album+":"+track);
}
};
}

public class CurrentMusicTrackInfoActivity extends Activity {

    public static final String SERVICECMD = "com.android.music.musicservicecommand";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        IntentFilter iF = new IntentFilter();
        iF.addAction("com.android.music.metachanged");
        iF.addAction("com.android.music.playstatechanged");
        iF.addAction("com.android.music.playbackcomplete");
        iF.addAction("com.android.music.queuechanged");

        registerReceiver(mReceiver, iF);
    }

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            String cmd = intent.getStringExtra("command");
            Log.v("tag ", action + " / " + cmd);
            String artist = intent.getStringExtra("artist");
            String album = intent.getStringExtra("album");
            String track = intent.getStringExtra("track");
            Log.v("tag", artist + ":" + album + ":" + track);
            Toast.makeText(CurrentMusicTrackInfoActivity.this, track, Toast.LENGTH_SHORT).show();
        }
    };

}


String scheme = mAudioUri.getScheme();
String title = "";
String artist = "";
if(scheme.equals("content")) {
    String[] proj = {MediaStore.Audio.Media.TITLE, MediaStore.Audio.Media.ARTIST};
    Cursor cursor = this.getContentResolver().query(mAudioUri, proj, null, null, null);
    if(cursor != null && cursor.getCount() > 0) {
        cursor.moveToFirst();
        if(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE) != -1) {  
            title = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE));
            artist = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST));
        }
    }
}

*/