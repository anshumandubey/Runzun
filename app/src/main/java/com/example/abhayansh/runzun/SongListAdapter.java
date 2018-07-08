package com.example.abhayansh.runzun;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.l4digital.fastscroll.FastScroller;

import java.io.InputStream;
import java.util.ArrayList;

public class SongListAdapter extends RecyclerView.Adapter<SongListAdapter.ViewHolder> implements FastScroller.SectionIndexer {

    private ArrayList<Song> songs;
    private Context context;
    public String titleIndex;


    public SongListAdapter(Context c,ArrayList<Song> songList){
        this.songs = songList;
        this.context = c;
    }

    @NonNull
    @Override
    public SongListAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.song,parent,false);
        ViewHolder vh = new ViewHolder(v);
        return vh;
    }



    private Bitmap getArtistImage(Long albumid) {


        Bitmap artwork = null;
        try {
            Uri sArtworkUri = Uri
                    .parse("content://media/external/audio/albumart");
            Uri uri = ContentUris.withAppendedId(sArtworkUri,
                    albumid);
            ContentResolver res = context.getContentResolver();
            InputStream in = res.openInputStream(uri);
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inPreferredConfig = Config.RGB_565;
            artwork = BitmapFactory.decodeStream(in,null,options);
            artwork = Bitmap.createScaledBitmap(artwork,100,100,true);

        } catch (Exception e) {
            Log.e("Exception", e.toString());
        }
        return artwork;
    }


    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Song currSong = songs.get(position);
        holder.songView.setText(currSong.getTitle());
        holder.artistView.setText(currSong.getArtist());
        if (getArtistImage(currSong.getAlbumID())==null){
            holder.artView.setImageResource(R.drawable.default_art);
        }
        else
            holder.artView.setImageBitmap(getArtistImage(currSong.getAlbumID()));

    }



    @Override
    public int getItemCount() {
        return songs.size();
    }

    @Override
    public String getSectionText(int position) {
        Song currSong = songs.get(position);
        titleIndex = currSong.getTitle().toUpperCase();
        return String.valueOf(titleIndex.charAt(0));
    }

    public static class ViewHolder extends RecyclerView.ViewHolder{
        public TextView songView,artistView;
        public ImageView artView;

        public ViewHolder(View v) {
            super(v);
             songView = (TextView) v.findViewById(R.id.song_title);
             artistView = (TextView) v.findViewById(R.id.song_artist);
             artView=(ImageView) v.findViewById(R.id.song_art);

        }
    }
}
