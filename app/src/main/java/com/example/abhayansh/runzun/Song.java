package com.example.abhayansh.runzun;


public class Song {
    private long id;
    private String title;
    private String artist;
    private long albumID;

    public Song(long songID, String songTitle, String songArtist, long thisAlbumID) {
        id=songID;
        title=songTitle;
        artist=songArtist;
        albumID = thisAlbumID;
    }

    public long getID(){return id;}
    public String getTitle(){return title;}
    public String getArtist(){return artist;}
    public long getAlbumID(){return albumID;}


}
