package com.otr.streamingaudio;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.URLUtil;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    ListView lista;
    ArrayList<String> arrayListNombre = new ArrayList<>();
    ArrayList<String> arrayListUrl = new ArrayList<>();

    public final static String URL_CANCION = "urlcancion";
    public final static String LISTA_URLS = "listaurls";
    public final static String POSICION = "posicion";

    DatabaseReference db = FirebaseDatabase.getInstance().getReference("url");
    static String URL;

    @Override
    protected void onStart() {
        super.onStart();
        lista = findViewById(R.id.listaCanciones);
        lista.setClickable(true);
        lista.setEnabled(true);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        FirebaseApp.initializeApp(this);
        //
        lista = findViewById(R.id.listaCanciones);
        lista.setClickable(true);
        lista.setEnabled(true);
        //
        db.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                arrayListNombre.clear();
                arrayListUrl.clear();

                for(DataSnapshot url : dataSnapshot.getChildren()) {
                    URL = url.getValue(String.class);
                    arrayListUrl.add(URL);
                    //
                    URL = URLUtil.guessFileName(URL, null, null);
                    URL = URL.replace(".mp3", "");
                    arrayListNombre.add(URL);
                }

                ArrayAdapter<String> adaptador = new ArrayAdapter<>(getApplication(),android.R.layout.simple_list_item_1, arrayListNombre);
                lista.setAdapter(adaptador);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {}
        });



        //listener al hacer click en un item de la lista
        lista.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                lista.setClickable(false);
                lista.setEnabled(false);
                Intent i = new Intent(getApplicationContext(), SongActivity.class);
                i.putExtra(URL_CANCION, arrayListUrl.get(position));
                i.putExtra(LISTA_URLS, arrayListUrl);
                i.putExtra(POSICION, position);
                startActivity(i);
            }
        });
    }
}
