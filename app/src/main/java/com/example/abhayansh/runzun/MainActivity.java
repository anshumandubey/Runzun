package com.example.abhayansh.runzun;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.ContentUris;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Random;

import android.net.Uri;
import android.content.ContentResolver;
import android.database.Cursor;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.os.IBinder;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.MediaController;
import android.widget.MediaController.MediaPlayerControl;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.example.abhayansh.runzun.MusicService.MusicBinder;
import com.l4digital.fastscroll.FastScroller;


public class MainActivity extends AppCompatActivity implements MediaPlayerControl{

    private ArrayList<Song> songList;
    private boolean paused=false, playbackPaused=false;
    private MusicService musicSrv;
    private Intent playIntent;
    private boolean musicBound=false;
    private MusicController controller;
    private Editor editor;
    private int LastSong;

    private boolean startFlag = true;

    private RecyclerView mRecyclerView;
    private RecyclerView.LayoutManager mLayoutManager;
    private SongListAdapter songAdt;
    private SwipeRefreshLayout swipeRefreshLayout;
    private FastScroller fastScroller;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        /*setTheme(R.style.AppTheme);*/


        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        String color = preferences.getString("color_choice","4");
        if (color.equals("0")){ setTheme(R.style.AppThemeRed);}
        else if (color.equals("1")){ setTheme(R.style.AppThemeGreen);}
        else if (color.equals("2")){ setTheme(R.style.AppThemeBrown);}
        else if (color.equals("3")){ setTheme(R.style.AppThemeGrey);}
        else setTheme(R.style.AppTheme);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},1);
                return;
            }}


        songList = new ArrayList<Song>();
        Random rand = new Random();

        getSongList();
        setController();

        Collections.sort(songList, new Comparator<Song>(){
            public int compare(Song a, Song b){
                return a.getTitle().compareTo(b.getTitle());
            }
        });

        String themeValue = preferences.getString("theme_choice","0");
        if (themeValue.equals("1")){
            RelativeLayout wholeContainer = findViewById(R.id.whole_container);
            wholeContainer.setBackgroundColor(getResources().getColor(R.color.colorPrimaryGreyDark));
        }

        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.refresh_pull);
        mRecyclerView = (RecyclerView) findViewById(R.id.song_list);

        if (mRecyclerView != null) {
            mRecyclerView.setHasFixedSize(true);
        }
        mRecyclerView.setItemViewCacheSize(50);
        mRecyclerView.setDrawingCacheEnabled(true);
        mRecyclerView.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);

        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.addItemDecoration(new DividerItemDecoration(this, LinearLayoutManager.VERTICAL));

        songAdt = new SongListAdapter(this, songList);
        mRecyclerView.setAdapter(songAdt);
        songAdt.notifyDataSetChanged();
        fastScroller =  findViewById(R.id.fast_scroller);
        fastScroller.setSectionIndexer(songAdt);
        fastScroller.attachRecyclerView(mRecyclerView);

        Context context = getApplicationContext();

        mRecyclerView.addOnItemTouchListener(
                new RecyclerItemClickListener(context, new RecyclerItemClickListener.OnItemClickListener() {
                    @Override public void onItemClick(View view, int position) {
                        songPicked(view,position);
                    }
                })
        );

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                swipeRefreshLayout.setRefreshing(false);
                refresh();
            }
        });

        SharedPreferences pref = getApplicationContext().getSharedPreferences("SongPref", 0);

        editor = pref.edit();

        LastSong = pref.getInt("SongPosn",0);

       /* FloatingActionButton playStarter = (FloatingActionButton) findViewById(R.id.fab);
        playStarter.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, PlayingActivity.class);
                startActivity(intent);
            }

        });*/
        RelativeLayout bottomView = findViewById(R.id.bottom_view);
        if (color.equals("0")){ bottomView.setBackgroundResource(R.color.colorPrimaryRed);fastScroller.setBubbleColor(getResources().getColor(R.color.colorPrimaryRed));}
        else if (color.equals("1")){ bottomView.setBackgroundResource(R.color.colorPrimaryGreen);fastScroller.setBubbleColor(getResources().getColor(R.color.colorPrimaryGreen));}
        else if (color.equals("2")){ bottomView.setBackgroundResource(R.color.colorPrimaryBrown);fastScroller.setBubbleColor(getResources().getColor(R.color.colorPrimaryBrown));}
        else if (color.equals("3")){ bottomView.setBackgroundResource(R.color.colorPrimaryGrey);fastScroller.setBubbleColor(getResources().getColor(R.color.colorPrimaryGrey));}
        else bottomView.setBackgroundResource(R.color.colorPrimary);

        IntentFilter filter = new IntentFilter("SongIncremented");
        LocalBroadcastManager bm = LocalBroadcastManager.getInstance(this);
        bm.registerReceiver(mBroadcastReceiver, filter);

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        moveTaskToBack(true);
    }

    public void refresh(){
        songList.clear();
        getSongList();
        Collections.sort(songList, new Comparator<Song>(){
            public int compare(Song a, Song b){
                return a.getTitle().compareTo(b.getTitle());
            }
        });
        mRecyclerView.removeAllViewsInLayout();
        songAdt = new SongListAdapter(MainActivity.this, songList);
        mRecyclerView.setAdapter(songAdt);
        songAdt.notifyDataSetChanged();
        fastScroller =  findViewById(R.id.fast_scroller);
        fastScroller.setSectionIndexer(songAdt);
        fastScroller.attachRecyclerView(mRecyclerView);
    }


    private final BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals("SongIncremented")) {
                setTitle();
                setAlbumBg();
                editor.putInt("SongPosn", musicSrv.songPosn);
                editor.apply();
            }
        }
    };

    private ServiceConnection musicConnection = new ServiceConnection(){
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MusicBinder binder = (MusicBinder)service;
            musicSrv = binder.getService();
            musicSrv.setList(songList);
            musicBound = true;
            if (songList!=null) {
                songPicked(null, LastSong);
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            musicBound = false;
        }

    };
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.main , menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_end:
                stopService(playIntent);
                musicSrv=null;
                System.exit(0);
                break;
            case R.id.action_settings:
                startActivity(new Intent(MainActivity.this, SettingsActivity.class));
                return true;
        }
        return super.onOptionsItemSelected(item);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopService(playIntent);
        musicSrv=null;
        System.exit(0);
        musicSrv.stopForeground(true);
        LocalBroadcastManager bm = LocalBroadcastManager.getInstance(this);
        bm.unregisterReceiver(mBroadcastReceiver);
        fastScroller.detachRecyclerView();
    }

    @Override
    protected void onPause() {
        super.onPause();
        paused=true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(this).registerReceiver(onPrepareReceiver,
                new IntentFilter("MEDIA_PLAYER_PREPARED"));
        if(paused){
            setController();
            controller.show(0);
            paused=false;
        }

    }

    @Override
    protected void onStop() {
        super.onStop();
        controller.hide();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if(playIntent==null){
            playIntent = new Intent(this, MusicService.class);
            bindService(playIntent, musicConnection, Context.BIND_AUTO_CREATE);
            startService(playIntent);
        }
    }

    private BroadcastReceiver onPrepareReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context c, Intent i) {
            if (startFlag){
                pause();
                startFlag = false;
            }
            controller.show(0);
        }
    };

    public void songPicked(View view,int p){
        musicSrv.setSong(p);
        musicSrv.playSong();
        if(playbackPaused){
            setController();
            playbackPaused=false;
        }
        setTitle();
        setAlbumBg();
        editor.putInt("SongPosn", p);
        editor.apply();
        controller.requestFocus();
    }


    public void getSongList() {
        ContentResolver musicResolver = getContentResolver();
        Uri musicUri = android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        Cursor musicCursor = musicResolver.query(musicUri, null, null, null, null);


        if(musicCursor!=null && musicCursor.moveToFirst()){
            int titleColumn = musicCursor.getColumnIndex
                    (android.provider.MediaStore.Audio.Media.TITLE);
            int idColumn = musicCursor.getColumnIndex
                    (android.provider.MediaStore.Audio.Media._ID);
            int artistColumn = musicCursor.getColumnIndex
                    (android.provider.MediaStore.Audio.Media.ARTIST);
            int albumID = musicCursor.getColumnIndex
                    (android.provider.MediaStore.Audio.Media.ALBUM_ID);
            do {
                long thisId = musicCursor.getLong(idColumn);
                String thisTitle = musicCursor.getString(titleColumn);
                String thisArtist = musicCursor.getString(artistColumn);
                long thisAlbumID = musicCursor.getLong(albumID);
                songList.add(new Song(thisId, thisTitle, thisArtist, thisAlbumID));
            }
            while (musicCursor.moveToNext());
        }
        else musicCursor.moveToNext();
    }

    private void playNext(){
        musicSrv.playNext();
        if(playbackPaused){
            setController();
            playbackPaused=false;
        }
        setTitle();
        setAlbumBg();
        editor.putInt("SongPosn", musicSrv.songPosn);
        editor.apply();
        controller.requestFocus();
    }

    private void playPrev(){
        musicSrv.playPrev();
        if(playbackPaused){
            setController();
            playbackPaused=false;
        }
        setTitle();
        setAlbumBg();
        editor.putInt("SongPosn", musicSrv.songPosn);
        editor.apply();
        controller.requestFocus();
    }

    public void setTitle(){

                    TextView currSongView = (TextView) findViewById(R.id.curr_song_title);
                    if (musicSrv != null)
                        currSongView.setText(musicSrv.songPlaying());
    }

    public void setAlbumBg(){

        ImageView currAlbumView = (ImageView) findViewById(R.id.album_bg);
        if (musicSrv != null)
            currAlbumView.setImageBitmap(getArtistImage(musicSrv.getCurrSongAlbumId()));
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


    protected void styleMediaController(View view) {
        if (view instanceof MediaController) {
            MediaController v = (MediaController) view;
            for(int i = 0; i < v.getChildCount(); i++) {
                styleMediaController(v.getChildAt(i));
            }
        } else
        if (view instanceof LinearLayout) {
            LinearLayout ll = (LinearLayout) view;
            for(int i = 0; i < ll.getChildCount(); i++) {
                styleMediaController(ll.getChildAt(i));
            }
        } else if (view instanceof SeekBar) {
            ((SeekBar) view).setProgressDrawable(getResources().getDrawable(R.drawable.custom_seekbar_progress));
            ((SeekBar) view).setThumb(getResources().getDrawable(R.drawable.custom_seekbar_thumb));
        }
    }




    private void setController(){

        if (controller == null) controller = new MusicController(this,false);
        controller.setPrevNextListeners(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playNext();
            }
        }, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playPrev();

            }
        });
        controller.setMediaPlayer(this);
        controller.setAnchorView(findViewById(R.id.bottom_view));
        controller.setEnabled(true);
        controller.setAlpha(1.0f);
        controller.setBackgroundTintList(null);
        this.styleMediaController(controller);
        controller.shuff.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                shuffle();
            }
        });

        controller.repeat.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                repeatSong();
            }
        });


        LinearLayout viewGroupLevel1 = (LinearLayout) controller.getChildAt(0);
        viewGroupLevel1.setBackgroundColor(getResources().getColor(android.R.color.transparent));


    }

    public void repeatSong(){
        boolean repeatFlag = musicSrv.setRepeat();
        if (repeatFlag)
            controller.repeat.setImageTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorAccent)));
        else
            controller.repeat.setImageTintList(ColorStateList.valueOf(getResources().getColor(R.color.white)));

    }

    public void shuffle(){
        boolean shuffleFlag = musicSrv.setShuffle();
        if (shuffleFlag)
            controller.shuff.setImageTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorAccent)));
        else
            controller.shuff.setImageTintList(ColorStateList.valueOf(getResources().getColor(R.color.white)));

    }

    @Override
    public void start() {
        musicSrv.go();

    }

    @Override
    public void pause() {
        playbackPaused=true;
        musicSrv.pausePlayer();
    }

    @Override
    public int getDuration() {
        if(musicSrv!=null && musicBound && musicSrv.isPng())
        return musicSrv.getDur();
        else return 0;
    }

    @Override
    public int getCurrentPosition() {
        if(musicSrv!=null && musicBound && musicSrv.isPng())
        return musicSrv.getPosn();
        else return 0;
    }

    @Override
    public void seekTo(int pos) {
        musicSrv.seek(pos);
    }

    @Override
    public boolean isPlaying() {
        if(musicSrv!=null && musicBound) {
            return musicSrv.isPng();
        }
        return false;
    }

    @Override
    public int getBufferPercentage() {
        return 0;
    }

    @Override
    public boolean canPause() {
        return true;
    }

    @Override
    public boolean canSeekBackward() {
        return true;
    }

    @Override
    public boolean canSeekForward() {
        return true;
    }

    @Override
    public int getAudioSessionId() {
        return 0;
    }


}
