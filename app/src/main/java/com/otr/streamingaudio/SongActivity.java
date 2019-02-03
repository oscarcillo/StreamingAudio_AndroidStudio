package com.otr.streamingaudio;

import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.URLUtil;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.IOException;

public class SongActivity extends AppCompatActivity {

    private ImageButton botonPlay;
    private boolean play = false;
    private TextView titulo;
    private TextView textDuracion;
    private TextView textCurrent;

    private SeekBar seekBar;
    private MediaPlayer mediaPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_song);
        //
        botonPlay = findViewById(R.id.button);
        seekBar = findViewById(R.id.seekBar);
        textDuracion = findViewById(R.id.textDuracion);
        titulo = findViewById(R.id.textViewTitulo);
        textCurrent = findViewById(R.id.textViewCurrent);
        //
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);

        //recuperar datos cancion
        String url = getIntent().getStringExtra(MainActivity.URL_CANCION);

        String tituloCancion = URLUtil.guessFileName(url, null, null);
        tituloCancion = tituloCancion.replace(".mp3", "");
        titulo.setText(tituloCancion);

        try {
            mediaPlayer.setDataSource(url);
            mediaPlayer.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }
        seekBar.setMax(mediaPlayer.getDuration());

        //
        int tiempo = mediaPlayer.getDuration()/1000;
        int minutos = tiempo / 60;
        int segundos = tiempo % 60;

        if(segundos<10)
            textDuracion.setText(minutos + ":0" + segundos);
        else if(segundos<60)
            textDuracion.setText(minutos + ":" + segundos);
        //

        hilo h = new hilo();
        h.start();

        //listener para poder poder mover el seekbar
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if(fromUser){
                    mediaPlayer.seekTo(progress);
                    //
                    int tiempo = mediaPlayer.getCurrentPosition()/1000;
                    int minutos = tiempo / 60;
                    int segundos = tiempo % 60;

                    ///////
                    if(segundos<10)
                        textCurrent.setText(minutos + ":0" + segundos);
                    else if(segundos<60)
                        textCurrent.setText(minutos + ":" + segundos);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    public void boton(View v){
        if(!play) {
            reproducir();
            botonPlay.setImageResource(android.R.drawable.ic_media_pause);
            play = true;
        }else {
            pausar();
            botonPlay.setImageResource(android.R.drawable.ic_media_play);
            play = false;
        }
    }

    private void reproducir(){
        mediaPlayer.start();
    }

    private void pausar(){
        mediaPlayer.pause();
    }

    class hilo extends Thread{

        @Override
        public void run() {
            while(true) {
                seekBar.setProgress(mediaPlayer.getCurrentPosition());
                textCurrent.post(new Runnable() {

                    int tiempo = mediaPlayer.getCurrentPosition()/1000;
                    int segundos = 0;
                    int minutos = 0;

                    @Override
                    public void run() {

                        minutos = tiempo / 60;
                        segundos = tiempo % 60;

                        ///////
                        if(segundos<10)
                            textCurrent.setText(minutos + ":0" + segundos);
                        else if(segundos<60)
                            textCurrent.setText(minutos + ":" + segundos);
                    }
                });
                try{
                    sleep(1000);
                }catch (Exception e){

                }

            }
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        mediaPlayer.stop();
    }
}
