package com.otr.streamingaudio;

import android.content.Intent;
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
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.IOException;
import java.util.ArrayList;

public class SongActivity extends AppCompatActivity {

    private ImageButton botonPlay;
    private ImageButton botonCancionAnterior;
    private ImageButton botonCancionSiguiente;
    private boolean play = false;
    private TextView titulo;
    private TextView textDuracion;
    private TextView textCurrent;
    private TextView textViewTituloAnterior;
    private TextView textViewTituloSiguiente;

    private SeekBar seekBar;
    private MediaPlayer mediaPlayer;

    ArrayList<String> listaUrls;
    int position;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_song);

        //cargar intent de la actividad anterior
        Intent main = getIntent();
        listaUrls = main.getStringArrayListExtra(MainActivity.LISTA_URLS);
        position = main.getIntExtra(MainActivity.POSICION, 0);

        //cargar todas las vistas
        botonPlay = findViewById(R.id.buttonPlay);
        seekBar = findViewById(R.id.seekBar);
        textDuracion = findViewById(R.id.textDuracion);
        titulo = findViewById(R.id.textViewTitulo);
        textCurrent = findViewById(R.id.textViewCurrent);
        textViewTituloAnterior = findViewById(R.id.textViewTituloAnterior);
        textViewTituloSiguiente = findViewById(R.id.textViewTituloSiguiente);
        botonCancionAnterior = findViewById(R.id.buttonPevious);
        botonCancionSiguiente = findViewById(R.id.buttonNext);

        cargarCancion(listaUrls.get(position));

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
            public void onStartTrackingTouch(SeekBar seekBar) { }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
    }

    private void cargarCancion(String url){
        if(position==0)
            botonCancionAnterior.setEnabled(false);
        else if(position==listaUrls.size()-1)
            botonCancionSiguiente.setEnabled(false);
        else{
            botonCancionSiguiente.setEnabled(true);
            botonCancionAnterior.setEnabled(true);
        }
        //
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);

       setTitulosCanciones();

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

        reproducir();

        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                Log.e("holaa", "holaa");
                mp.release();
                cargarCancion(listaUrls.get(++position));
            }
        });
    }

    public void boton(View v){
        if(!play)
            reproducir();
        else
            pausar();
    }

    public void setTitulosCanciones(){

        //recuperar datos cancion
        String tituloCancion = URLUtil.guessFileName(listaUrls.get(position), null, null);
        tituloCancion = tituloCancion.replace(".mp3", "");
        titulo.setText(tituloCancion);

        //set titulo a la cancion anterior
        if(position-1>=0){
            tituloCancion = URLUtil.guessFileName(listaUrls.get(position-1), null, null);
            tituloCancion = tituloCancion.replace(".mp3", "");
            textViewTituloAnterior.setText(tituloCancion);
        }else{
            textViewTituloAnterior.setText("");
        }

        //set titulo a la siguiente cancion
        if(position+1<=listaUrls.size()-1){
            tituloCancion = URLUtil.guessFileName(listaUrls.get(position+1), null, null);
            tituloCancion = tituloCancion.replace(".mp3", "");
            textViewTituloSiguiente.setText(tituloCancion);
        }else{
            textViewTituloSiguiente.setText("");
        }
    }

    private void reproducir(){
        mediaPlayer.start();
        botonPlay.setImageResource(android.R.drawable.ic_media_pause);
        play = true;
    }

    private void pausar(){
        mediaPlayer.pause();
        botonPlay.setImageResource(android.R.drawable.ic_media_play);
        play = false;
    }

    public void cancionAnterior(View v){
        mediaPlayer.release();
        cargarCancion(listaUrls.get(--position));
    }

    public void cancionSiguiente(View v){
        mediaPlayer.release();
        cargarCancion(listaUrls.get(++position));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mediaPlayer.stop();
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

}
