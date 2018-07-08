package com.example.abhayansh.runzun;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ContentResolver;
import android.content.ContentUris;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;


import java.io.InputStream;
import java.util.ArrayList;
import java.util.Random;

public class MusicService extends Service implements
        MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener,
        MediaPlayer.OnCompletionListener,AudioManager.OnAudioFocusChangeListener {

    public MediaPlayer player;
    private ArrayList<Song> songs;
    public int songPosn;
    public String songTitle = "";
    public String songArtist = "";
    private static final int NOTIFY_ID = 1;
    public boolean shuffle = false;
    private Random rand;
    public long currSongAlbumId;

    private final IBinder musicBind = new MusicBinder();
    public boolean repeat = false;


    @Override
    public void onCreate() {
        super.onCreate();
        rand=new Random();
        currSongAlbumId = 0;
        songPosn = 0;
        player = new MediaPlayer();
        initMusicPlayer();
    }

    public void initMusicPlayer() {
        player.setWakeMode(getApplicationContext(),
                PowerManager.PARTIAL_WAKE_LOCK);
        player.setAudioStreamType(AudioManager.STREAM_MUSIC);
        player.setOnPreparedListener(this);
        player.setOnCompletionListener(this);
        player.setOnErrorListener(this);
    }


    public void setList(ArrayList<Song> theSongs) {
        songs = theSongs;
    }

    @Override
    public void onAudioFocusChange(int i) {

    }

    public class MusicBinder extends Binder {
        MusicService getService() {
            return MusicService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return musicBind;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        player.stop();
        player.release();
        return false;
    }

    @Override
    public void onCompletion(MediaPlayer mediaPlayer) {
        if(player.getCurrentPosition()>0){
            mediaPlayer.reset();
            playNext();
            Intent intent = new Intent("SongIncremented");
            LocalBroadcastManager bm = LocalBroadcastManager.getInstance(this);
            bm.sendBroadcast(intent);
        }
    }

    @Override
    public boolean onError(MediaPlayer mediaPlayer, int i, int i1) {
        mediaPlayer.reset();
        return false;
    }

    @Override
    public void onDestroy() {
        stopForeground(true);
    }


    @Override
    public void onPrepared(MediaPlayer mp) {
        mp.start();
        Intent notIntent = new Intent(this, MainActivity.class);
        notIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendInt = PendingIntent.getActivity(this, 0,
                notIntent, PendingIntent.FLAG_UPDATE_CURRENT);

       Notification.Builder builder = new Notification.Builder(this);

        builder.setContentIntent(pendInt)
                .setSmallIcon(R.drawable.play)
                .setLargeIcon(getArtistImage(currSongAlbumId))
                .setTicker(songTitle)
                .setOngoing(true)
                .setContentTitle(songTitle)
                .setContentText(songArtist);
        Notification not = builder.build();

        startForeground(NOTIFY_ID, not);

        Intent onPreparedIntent = new Intent("MEDIA_PLAYER_PREPARED");
        LocalBroadcastManager.getInstance(this).sendBroadcast(onPreparedIntent);
    }

    public void setSong(int songIndex) {
        songPosn = songIndex;
    }

    public void playSong() {
        player.reset();
        Song playSong = songs.get(songPosn);
        songTitle = playSong.getTitle();
        songArtist = playSong.getArtist();
        long currSong = playSong.getID();
        currSongAlbumId = playSong.getAlbumID();
        Uri trackUri = ContentUris.withAppendedId(
                android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                currSong);

        try {
            player.setDataSource(getApplicationContext(), trackUri);
        } catch (Exception e) {
            Log.e("MUSIC SERVICE", "Error setting data source", e);
        }
        player.prepareAsync();

    }

    private Bitmap getArtistImage(Long albumid) {


        Bitmap artwork = null;
        try {
            Uri sArtworkUri = Uri
                    .parse("content://media/external/audio/albumart");
            Uri uri = ContentUris.withAppendedId(sArtworkUri,
                    albumid);
            ContentResolver res = getApplicationContext().getContentResolver();
            InputStream in = res.openInputStream(uri);
            artwork = BitmapFactory.decodeStream(in);

        } catch (Exception e) {
            Log.e("Exception", e.toString());
        }
        return artwork;
    }



    public long getCurrSongAlbumId(){
        return currSongAlbumId;
    }


    public int getPosn() {
        return player.getCurrentPosition();
    }

    public int getDur() {
        return player.getDuration();
    }

    public boolean isPng() {
        return player.isPlaying();
    }

    public void pausePlayer() {
        player.pause();
    }

    public String songPlaying() {
        return songTitle;
    }

    public void seek(int posn) {
        player.seekTo(posn);
    }

    public void go() {
        player.start();
    }

    public void playPrev() {
        songPosn--;
        if (songPosn < 0) songPosn = songs.size() - 1;
        {
            playSong();
        }
    }

    public void playNext() {
        if (shuffle) {
            int newSong = songPosn;
            while (newSong == songPosn) {
                newSong = rand.nextInt(songs.size());
            }
            songPosn = newSong;
        }else if (repeat) {
            int newSong = songPosn;
            songPosn = newSong;
        } else {
            songPosn++;
            if (songPosn >= songs.size()) songPosn = 0;
        }
        playSong();
    }


    public boolean setRepeat() {

        if (repeat) repeat = false;
        else repeat = true;
        return repeat;


    }




    public boolean setShuffle() {

            if (shuffle) shuffle = false;
            else shuffle = true;
            return shuffle;


    }

}


