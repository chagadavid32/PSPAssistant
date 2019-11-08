package com.example.pspassistant.Fragments;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.pspassistant.Modelos.Proyecto;
import com.example.pspassistant.Modelos.Registro_Defectos;
import com.example.pspassistant.R;

import java.util.ArrayList;
import java.util.Date;

import io.realm.Realm;
import io.realm.RealmList;

public class FragmentDefectos extends Fragment implements AdapterView.OnItemSelectedListener, View.OnClickListener {

    private int id;
    private String Nombre_Prefs = "TIEMPOS";
    private CharSequence[] SeleccionEtapas = {"Planeación", "Diseño", "Codificación", "Compilación", "Pruebas"};
    private SharedPreferences general;
    private SharedPreferences archivo;
    private SharedPreferences.Editor editor;
    private int indiceEtapa;
    private String Tipo = "";
    private String Etapa = "";
    public static long temporal;

    private Proyecto proyecto;
    private Realm realm;
    private RealmList<Registro_Defectos> defectos;
    private Registro_Defectos defecto;

    private View view;
    public static Spinner Tipos, Etapas;
    private Button Registrar, Finalizar;

    private ArrayAdapter<CharSequence> AdapterTipos;
    private ArrayAdapter<CharSequence> AdapterEtapas;
    private ArrayList<CharSequence> Seleccion_etapas;
    public static ArrayList<CharSequence> TiposDefectos;
    private int posicion;


    public FragmentDefectos(){

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_registro_defectos, container, false);

        obtenerreferencias();

        Bundle bundle = getActivity().getIntent().getExtras();
        if(bundle != null) {
            id = bundle.getInt("id");

            realm = Realm.getDefaultInstance();
            proyecto = realm.where(Proyecto.class).equalTo("Id_Proyecto", id).findFirst();
            defectos = proyecto.getBitacora_defectos();

            general = getActivity().getSharedPreferences("general", Context.MODE_PRIVATE);
            archivo = getActivity().getSharedPreferences(Nombre_Prefs + id, Context.MODE_PRIVATE);
        }

        if(!FragmentTiempos.Estado_servicio || general.getInt("proyecto", 0) != id){

            Tipos.setEnabled(false);

            Etapas.setEnabled(false);
            Registrar.setEnabled(false);
            Finalizar.setEnabled(false);
        }



        Seleccion_etapas = new ArrayList<CharSequence>();
        Seleccion_etapas.add("Etapa de introducción");
        //Obtener el indice de la etapa actual
        indiceEtapa = archivo.getInt("etapa", 0);
        for (int i=0; i<indiceEtapa; i++){
            Seleccion_etapas.add(SeleccionEtapas[i]);
        }

        TiposDefectos = new ArrayList<CharSequence>();
        TiposDefectos.add("Tipo de defecto");
        TiposDefectos.add("10  Documentación");
        TiposDefectos.add("20  Sintaxis");
        TiposDefectos.add("30  Construcción, Paquete");
        TiposDefectos.add("40  Asignación");
        TiposDefectos.add("50  Interfaz");
        TiposDefectos.add("60  Chequeo");
        TiposDefectos.add("70  Datos");
        TiposDefectos.add("80  Función");
        TiposDefectos.add("90  Sistema");
        TiposDefectos.add("100 Ambiente o entorno");


        AdapterTipos = CreaAdapter(TiposDefectos);
        AdapterEtapas = CreaAdapter(Seleccion_etapas);


        AdapterTipos.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        AdapterEtapas.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        Tipos.setAdapter(AdapterTipos);
        Etapas.setAdapter(AdapterEtapas);

        Tipos.setOnItemSelectedListener(this);
        Etapas.setOnItemSelectedListener(this);

        Registrar.setOnClickListener(this);
        Finalizar.setOnClickListener(this);


        return view;
    }

    private void obtenerreferencias(){
        Tipos = (Spinner) view.findViewById(R.id.Spinner_tipos_defecto);
        Etapas = (Spinner) view.findViewById(R.id.Spinner_etapa_introduccion);
        Registrar = (Button) view.findViewById(R.id.Boton_registrar_defecto);
        Finalizar = (Button) view.findViewById(R.id.Boton_reparar_defecto);
    }

    private ArrayAdapter<CharSequence> CreaAdapter (ArrayList<CharSequence> lista){
        ArrayAdapter adapter = new ArrayAdapter<CharSequence>(getContext(), android.R.layout.simple_spinner_item, lista){
            @Override
            public boolean isEnabled(int position) {
                return !(position == 0);
            }

            @Override
            public View getDropDownView(int position, View convertView, ViewGroup parent) {
                View view = super.getDropDownView(position, convertView, parent);
                TextView textView = (TextView) view;
                if(position == 0){
                    textView.setTextColor(Color.GRAY);
                }else{
                    textView.setTextColor(Color.BLACK);
                }
                return view;
            }
        };
        return adapter;
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        if(position != 0){
            switch(parent.getId()){
                case R.id.Spinner_etapa_introduccion:
                    Etapa = parent.getItemAtPosition(position).toString();
                    break;
                case R.id.Spinner_tipos_defecto:
                    Tipo = parent.getItemAtPosition(position).toString();
                    posicion = position;
                    break;
            }
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.Boton_registrar_defecto:
                if(ValidarSpinner()){
                    editor = archivo.edit();
                    editor.putBoolean("defecto_reparado", false);
                    editor.putLong("tiempo_reparacion", new Date().getTime());
                    editor.commit();
                    Tipos.setEnabled(false);
                    Etapas.setEnabled(false);
                }else{
                    Toast.makeText(getContext(), "Por favor selecciona los campos correctamente", Toast.LENGTH_SHORT).show();
                }

                break;
            case R.id.Boton_reparar_defecto:
                if(!archivo.getBoolean("defecto_reparado", true)){
                    editor = archivo.edit();
                    editor.putBoolean("defecto_reparado", true);
                    editor.putLong("tiempo_reparacion", new Date().getTime()-archivo.getLong("tiempo_reparacion", 0));
                    editor.commit();
                    temporal += archivo.getLong("tiempo_reparacion", 0);
                    Tipos.setSelection(0);
                    Etapas.setSelection(0);
                    Tipos.setEnabled(true);
                    Etapas.setEnabled(true);
                    //Meter en base de datos el defecto

                    defecto = new Registro_Defectos(new Date(), posicion+"0", Etapa,
                            FragmentTiempos.etiquetaEtapa.getText().toString());

                    realm.beginTransaction();
                    defecto.setTiempo_Reparacion((int)(temporal/999));
                    defectos.add(defecto);
                    realm.commitTransaction();
                    CrearMensaje("Descripcion");

                    temporal = 0;

                }else{
                    Toast.makeText(getContext(), "No se ha registrado ningun defecto", Toast.LENGTH_SHORT).show();
                }

                break;
        }
    }

    private boolean ValidarSpinner(){
        if(!Tipo.isEmpty() && !Etapa.isEmpty()){
            Toast.makeText(getContext(),"Registro de defecto comenzado", Toast.LENGTH_LONG).show();
            return true;
        }
        return false;
    }

    private void CrearMensaje(String titulo){
        AlertDialog.Builder Constructor_mensaje = new AlertDialog.Builder(getContext());
        if(titulo != null) Constructor_mensaje.setTitle(titulo);

        View view = LayoutInflater.from(getContext()).inflate(R.layout.dialogo_defecto, null);
        Constructor_mensaje.setView(view);

        final EditText descripcion = view.findViewById(R.id.Edit_descripcion_defecto);

        Constructor_mensaje.setPositiveButton("Guardar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String Descripcion = descripcion.getText().toString().trim();
                if (Descripcion.length() > 0) {
                    realm.beginTransaction();
                    defecto.setDescripcion(Descripcion);
                    realm.commitTransaction();
                }else{
                    Toast.makeText(getContext(), "Introduce una descripcion", Toast.LENGTH_SHORT).show();
                }
            }
        });

        AlertDialog mensaje = Constructor_mensaje.create();
        mensaje.show();
    }
}

