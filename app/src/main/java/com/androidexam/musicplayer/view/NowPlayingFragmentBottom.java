package com.androidexam.musicplayer.view;

import static android.content.Context.MODE_PRIVATE;
import static com.androidexam.musicplayer.MainActivity.ARTIST_TO_FRAG;
import static com.androidexam.musicplayer.MainActivity.PATH_TO_FRAG;
import static com.androidexam.musicplayer.MainActivity.SHOW_MINI_PLAYER;
import static com.androidexam.musicplayer.MainActivity.SONG_NAME_TO_FRAG;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.media.MediaMetadataRetriever;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.os.IBinder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.androidexam.musicplayer.MusicService;
import com.androidexam.musicplayer.R;
import com.bumptech.glide.Glide;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class NowPlayingFragmentBottom extends Fragment implements ServiceConnection {

    ImageView nextBtn, albumArt;
    TextView artist, songName;
    FloatingActionButton playPauseBtn;
    View view;
    MusicService musicService;
    public static final String MUSIC_LAST_PLAYED = "LAST_PLAYED";
    public static final String MUSIC_FILE = "STORED_MUSIC";
    public static final String ARTIST_NAME = "ARTIST NAME";
    public static final String SONG_NAME = "SONG NAME";

    public NowPlayingFragmentBottom() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_now_playing_bottom,container,false);
        artist = view.findViewById(R.id.song_artist_miniPlayer);
        songName = view.findViewById(R.id.song_name_miniPlayer);
        albumArt = view.findViewById(R.id.bottom_album_art);
        nextBtn = view.findViewById(R.id.skip_next_bottom);
        playPauseBtn = view.findViewById(R.id.play_pause_miniPlayer);
        nextBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getContext(), "Next", Toast.LENGTH_SHORT).show();
                if(musicService != null) {
                    musicService.nextBtnClicked();
                    if(getActivity() != null) {
                        SharedPreferences.Editor editor = getActivity().getSharedPreferences(MUSIC_LAST_PLAYED,
                                        MODE_PRIVATE)
                                .edit();
                        editor.putString(MUSIC_FILE, musicService.songs.get(musicService.position).getPath());
                        editor.putString(ARTIST_NAME, musicService.songs.get(musicService.position).getArtist());
                        editor.putString(SONG_NAME, musicService.songs.get(musicService.position).getTitle());
                        editor.apply();
                        if(SHOW_MINI_PLAYER) {
                            byte[] art = getAlbumArt(PATH_TO_FRAG);
                            Glide.with(getContext()).load(art)
                                    .into(albumArt);
                            songName.setText(SONG_NAME_TO_FRAG);
                            artist.setText(ARTIST_TO_FRAG);
                        }
                    }
                }
            }
        });
        playPauseBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getContext(), "PlayPause", Toast.LENGTH_SHORT).show();
                if(musicService != null) {
                    musicService.playPauseBtnClicked();
                    if(musicService.isPlaying()){
                        playPauseBtn.setImageResource(R.drawable.ic_baseline_pause_24);
                    } else {
                        playPauseBtn.setImageResource((R.drawable.ic_baseline_play_arrow_24));
                    }
                }
            }
        });
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        if(SHOW_MINI_PLAYER) {
            byte[] art = getAlbumArt(PATH_TO_FRAG);
            Glide.with(getContext()).load(art)
                    .into(albumArt);
            songName.setText(SONG_NAME_TO_FRAG);
            artist.setText(ARTIST_TO_FRAG);
            Intent intent = new Intent(getContext(), MusicService.class);
            if(getContext() != null) {
                getContext().bindService(intent,this, Context.BIND_AUTO_CREATE);
            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if(getContext() != null) {
            getContext().unbindService(this);
        }
    }

    private byte[] getAlbumArt(String uri) {
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource(uri);
        byte[] art = retriever.getEmbeddedPicture();
        return art;
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        MusicService.MyBinder binder = (MusicService.MyBinder) service;
        musicService = binder.getService();
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        musicService = null;
    }
}