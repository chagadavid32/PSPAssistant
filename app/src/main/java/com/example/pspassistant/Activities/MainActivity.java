package com.example.pspassistant.Activities;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.example.pspassistant.Adaptadores.Adaptador_Proyecto;
import com.example.pspassistant.Modelos.Proyecto;
import com.example.pspassistant.R;

import java.util.Date;

import io.realm.Realm;
import io.realm.RealmResults;

public class MainActivity extends AppCompatActivity implements View.OnClickListener , Adaptador_Proyecto.OnItemClickListener, Adaptador_Proyecto.OnLongClickListener{

    private RealmResults<Proyecto> proyectos;
    private RecyclerView recyclerView;
    private RecyclerView.Adapter adapter;
    private RecyclerView.LayoutManager layoutManager;
    private Realm realm;
    private SharedPreferences general;
    private SharedPreferences.Editor editor;

    private FloatingActionButton Agregar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Realm.init(this);
        realm = Realm.getDefaultInstance();


        general = getSharedPreferences("general", Context.MODE_PRIVATE);
        if(!general.getBoolean("nombreregisrado", false)){
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Bienvenid@");

            View view = LayoutInflater.from(this).inflate(R.layout.inicio, null);
            builder.setView(view);

            final EditText nombre = view.findViewById(R.id.tunombre);

            builder.setPositiveButton("Iniciar", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    String name = nombre.getText().toString().trim();
                    if(name != null){
                        editor = general.edit();
                        editor.putString("nombre", name);
                        editor.putBoolean("nombreregisrado", true);
                        editor.commit();
                    }else{
                        onDestroy();
                    }
                }
            });

            AlertDialog mensaje = builder.create();
            mensaje.show();
        }

        Agregar = (FloatingActionButton) findViewById(R.id.Agregar);
        Agregar.setOnClickListener(this);

        proyectos = getProyectos();

        recyclerView = findViewById(R.id.RecyclerViewProyecto);
        layoutManager = new LinearLayoutManager(this);
        adapter = new Adaptador_Proyecto(proyectos, R.layout.proyecto_lista, this, this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);
        recyclerView.setHasFixedSize(true);
        recyclerView.setItemAnimator(new DefaultItemAnimator());

    }

    @Override
    protected void onDestroy() {
        realm.close();
        super.onDestroy();
    }

    private RealmResults<Proyecto> getProyectos(){
         return realm.where(Proyecto.class).findAll();
    }

    private void CrearMensaje(String Titulo, String Mensaje){
        AlertDialog.Builder Constructor_mensaje = new AlertDialog.Builder(this);

        if(Titulo!=null) Constructor_mensaje.setTitle(Titulo);
        if(Mensaje!=null) Constructor_mensaje.setMessage(Mensaje);

        View view = LayoutInflater.from(this).inflate(R.layout.dialogo_crear_proyecto, null);
        Constructor_mensaje.setView(view);

        final EditText Nombre_Proyecto = (EditText) view.findViewById(R.id.edit_Nombre);
        final EditText Lenguaje = (EditText) view.findViewById(R.id.edit_Lenguaje);

        Constructor_mensaje.setPositiveButton("Agregar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String Nombre_proyecto = Nombre_Proyecto.getText().toString().trim();
                String lenguaje = Lenguaje.getText().toString().trim();

                if(Nombre_Proyecto.length() > 0 && lenguaje.length() > 0){
                    CreaProyecto(Nombre_proyecto, lenguaje);
                }else{
                    Toast.makeText(MainActivity.this, "Por favor, rellene los campos que se piden", Toast.LENGTH_SHORT).show();
                }
            }
        });

        AlertDialog mensaje = Constructor_mensaje.create();
        mensaje.show();

    }

    private void EliminaProyecto(Proyecto proyecto, int posicion){
        realm.beginTransaction();
        proyecto.deleteFromRealm();
        realm.commitTransaction();
        adapter.notifyItemRemoved(posicion);
        Toast.makeText(this, "Proyecto eliminado", Toast.LENGTH_SHORT).show();
    }

    private void CreaProyecto(String Nombre_Proyecto, String Lenguaje){
        realm.beginTransaction();
        Proyecto p = new Proyecto(Nombre_Proyecto, Lenguaje, new Date());
        realm.copyToRealmOrUpdate(p);
        realm.commitTransaction();
        adapter.notifyItemInserted(adapter.getItemCount());
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()) {
            case R.id.Agregar:
                CrearMensaje("Nuevo Proyecto", "Por favor, rellena los campos");
                break;
        }
    }

    @Override
    public void OnClick(Proyecto proyecto, int posicion) {

        Intent intent =  new Intent(MainActivity.this, RegistroPspActivity.class);
        intent.putExtra("id", proyecto.getId_Proyecto());
        startActivity(intent);
    }

    @Override
    public void OnLongClick(final Proyecto proyecto, final int posicion) {
        AlertDialog.Builder Eliminacion = new AlertDialog.Builder(this);

        Eliminacion.setTitle("Confirmar eliminación");
        Eliminacion.setMessage("¿Estás seguro?");

        Eliminacion.setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                EliminaProyecto(proyecto, posicion);
            }
        });
        AlertDialog mensaje = Eliminacion.create();
        mensaje.show();
    }
}
