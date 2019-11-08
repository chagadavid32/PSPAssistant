package com.example.pspassistant.Fragments;

import android.app.ActivityManager;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.pspassistant.Activities.RegistroPspActivity;
import com.example.pspassistant.Hilo.Cronometro;
import com.example.pspassistant.Modelos.Proyecto;
import com.example.pspassistant.Modelos.Registro_Tiempos;
import com.example.pspassistant.R;
import com.example.pspassistant.Resultados.EscribirPDF;

import java.text.SimpleDateFormat;
import java.util.Date;

import io.realm.Realm;
import io.realm.RealmList;

public class FragmentTiempos extends Fragment implements View.OnClickListener{

    int tiempo_estimado;
    private String Nombre_Prefs = "TIEMPOS";
    private String[] Etapas = {"Planeación", "Diseño", "Codificación", "Compilación", "Pruebas"};
    private SharedPreferences archivo;
    private SharedPreferences general;
    private SharedPreferences.Editor editor;

    private int id;
    private int tiempo_cronometro;
    private Proyecto proyecto;
    private Realm realm;
    private RealmList<Registro_Tiempos> tiempos;
    private int indice;

    private View view;
    public static TextView etiquetaEtapa;
    private EditText editTiempo;
    private Button botonRegistra;
    private FloatingActionButton Inicio;
    private FloatingActionButton Pausa;
    private FloatingActionButton Detener;
    private Button Reporte;
    public static boolean Estado_servicio;
    private ViewPager viewPager;

    public FragmentTiempos (){ }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_registro_tiempos, container, false);
        ObtenerReferencias();

        realm = Realm.getDefaultInstance();

        Bundle bundle = getActivity().getIntent().getExtras();
        if(bundle != null){
            id = bundle.getInt("id");

            general = getActivity().getSharedPreferences("general", Context.MODE_PRIVATE);
            archivo = getActivity().getSharedPreferences(Nombre_Prefs+id, Context.MODE_PRIVATE);
            if(archivo.getInt("tiempo_estimado", 0) != 0){
                editTiempo.setText(archivo.getInt("tiempo_estimado", 0)+"");
                editTiempo.setEnabled(false);
            }
        }
        if(ObtenerEtapa() == 5){
            Inicio.setVisibility(View.INVISIBLE);
            Pausa.setVisibility(View.INVISIBLE);
            Detener.setVisibility(View.INVISIBLE);
            editTiempo.setVisibility(View.INVISIBLE);
            botonRegistra.setVisibility(View.INVISIBLE);
            Reporte.setVisibility(View.VISIBLE);
            etiquetaEtapa.setText("Ejercicio finalizado");
        }else{
            ActualizarEtapa();
        }

        proyecto = realm.where(Proyecto.class).equalTo("Id_Proyecto", id).findFirst();
        tiempos = proyecto.getBitacora_tiempos();

        Estado_servicio = ServicioEnEjecucion(Cronometro.class);
        if(Estado_servicio){
            if(ObtenerProyectoCaptura() == id){
                Inicio.setVisibility(View.INVISIBLE);
                Pausa.setVisibility(View.VISIBLE);
            }
            editTiempo.setEnabled(false);
        }

        botonRegistra.setOnClickListener(this);
        Inicio.setOnClickListener(this);
        Pausa.setOnClickListener(this);
        Detener.setOnClickListener(this);
        Reporte.setOnClickListener(this);

        return view;
    }

    private void ObtenerReferencias(){
        Inicio = (FloatingActionButton) view.findViewById(R.id.Boton_inicio);
        Pausa = (FloatingActionButton) view.findViewById(R.id.Boton_pausa);
        Detener = (FloatingActionButton) view.findViewById(R.id.Boton_detener);
        botonRegistra = (Button) view.findViewById(R.id.Button_captura_estimado);
        editTiempo = (EditText) view.findViewById(R.id.Edit_tiempo_estimado);
        etiquetaEtapa = (TextView) view.findViewById(R.id.Text_etapa);
        Reporte = (Button) view.findViewById(R.id.Genera_reporte);
        viewPager = (ViewPager) view.findViewById(R.id.Pager);
    }

    private boolean ServicioEnEjecucion( Class<? extends Service> Clase){
        ActivityManager manager = (ActivityManager) getActivity().getApplicationContext().getSystemService(Context.ACTIVITY_SERVICE);
        for(ActivityManager.RunningServiceInfo servicio: manager.getRunningServices(Integer.MAX_VALUE)){
            if(Clase.getName().equals(servicio.service.getClassName())){
                return true;
            }
        }
        return false;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.Button_captura_estimado:
                CapturarTiempoEstimado();
                break;
            case R.id.Boton_inicio:
                if(!editTiempo.isEnabled()){
                    Estado_servicio = ServicioEnEjecucion(Cronometro.class);
                    if(!Estado_servicio){
                        if(archivo.getBoolean("paro", false)){
                            long hora_paro = archivo.getLong("hora_pausa",0);
                            long tiempo_interrupcion = new Date().getTime() - hora_paro;
                            Rellena_Tiempo(tiempo_interrupcion);
                        }
                        IniciarEtapa();
                        GuardarProyectoCaptura(id);
                        RegistroPspActivity.viewPager.getAdapter().notifyDataSetChanged();
                    }else{
                        Toast.makeText(getContext(), "No es posible capturar en más de un proyecto a la vez", Toast.LENGTH_LONG).
                                show();
                    }
                }else{
                    Toast.makeText(getActivity().getApplicationContext(), "Por favor, rellena el campo",Toast.LENGTH_SHORT).show();
                }
                if(!archivo.getBoolean("defecto_reparado", true)){
                    FragmentDefectos.Tipos.setEnabled(false);
                    FragmentDefectos.Etapas.setEnabled(false);
                }
                break;
            case R.id.Boton_pausa:
                PausarEtapa();
                FragmentDefectos.Etapas.setSelected(false);
                FragmentDefectos.Tipos.setSelected(false);
                Estado_servicio = ServicioEnEjecucion(Cronometro.class);
                RegistroPspActivity.viewPager.getAdapter().notifyDataSetChanged();
                break;
            case R.id.Boton_detener:
                if(Inicio.getVisibility() == View.INVISIBLE){
                    if(archivo.getBoolean("defecto_reparado", true)){
                        TerminarEtapa();
                        FragmentDefectos.Tipos.setSelection(0);
                        FragmentDefectos.Etapas.setSelection(0);
                        if(ObtenerEtapa() == 4){
                            FinalizarProyecto();
                        }else{
                            CambiarEtapa();
                            ActualizarEtapa();
                            editTiempo.setText("");
                            editTiempo.setEnabled(true);
                            Inicio.setVisibility(View.VISIBLE);
                            Pausa.setVisibility(View.INVISIBLE);
                            GuardarProyectoCaptura(0);
                        }
                    }else{
                        Toast.makeText(getContext(), "El defecto no ha sido reparado", Toast.LENGTH_SHORT).show();
                    }

                }else{
                    Toast.makeText(getActivity().getApplicationContext(), "La etapa no ha sido iniciada o reanudada",
                            Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.Genera_reporte:
                //Tiene que pedir los datos de docente y LOC
                if(archivo.getBoolean("pdfgenerado", false)){
                    Toast.makeText(getContext(), "Ya se genero un archivo para este proyecto", Toast.LENGTH_SHORT).show();
                }else{
                    EscribirPDF.verifyStoragePermissions(getActivity());
                    AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                    builder.setTitle("Generar reporte");
                    builder.setMessage("Por favor, proporciona los datos");

                    View view = LayoutInflater.from(getContext()).inflate(R.layout.datos_reporte, null);
                    builder.setView(view);

                    final EditText instructor = (EditText) view.findViewById(R.id.nombre_instructor);
                    final EditText loc = (EditText) view.findViewById(R.id.loc);

                    builder.setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            String nombre = instructor.getText().toString().trim();
                            String lineas = loc.getText().toString().trim();

                            if(instructor.length() > 0 && loc.length() > 0){
                                realm.beginTransaction();
                                proyecto.setNombre_Instructor(nombre);
                                proyecto.setLOC(Integer.parseInt(lineas));
                                realm.commitTransaction();
                                GenerarReporte();
                            }else{
                                Toast.makeText(getContext(), "Debes llenar los campos", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });

                    AlertDialog mensaje = builder.create();
                    mensaje.show();
                }
                break;
        }
    }

    private void GenerarReporte(){
        EscribirPDF escribirPDF = new EscribirPDF(getContext(), id, general, getActivity());
        escribirPDF.AbrirDocumento();
        escribirPDF.setMetadatos(general.getString("nombre", ""));
        escribirPDF.EncabezadoDoumento();
        escribirPDF.TablaPlan();
        escribirPDF.EscribirBitacoraTiempos(new String[]{"Fecha", "Inicio", "Paro", "Interrupcion", "Delta", "Fase", "Comentarios"});
        escribirPDF.EscribirBitacoraDefectos(new String[]{"Fecha", "Numero", "Tipo", "Introducción", "Removido", "Tiempo"});
        escribirPDF.Fin();
        escribirPDF.CierraDocumento();
        Toast.makeText(getContext(), "Archivo pdf generado", Toast.LENGTH_SHORT).show();
        editor = archivo.edit();
        editor.putBoolean("pdfgenerado", true);
        editor.commit();
    }

    private void CapturarTiempoEstimado(){
        if(!editTiempo.getText().toString().isEmpty()){
            if(!editTiempo.getText().toString().startsWith("0")){
                tiempo_estimado = Integer.parseInt(editTiempo.getText().toString());
                editTiempo.setEnabled(false);
                GuardarEstimado();
            }else{
                Toast.makeText(getContext(), "El tiempo no debe iniciar en 0", Toast.LENGTH_SHORT).show();
            }
        }else{
            Toast.makeText(getActivity().getApplicationContext(), "Por favor, rellena el campo",
                    Toast.LENGTH_SHORT).show();
        }
    }

    private void IniciarEtapa(){
        Registro_Tiempos tiempo = new Registro_Tiempos(new Date());
        realm.beginTransaction();
        tiempos.add(tiempo);
        realm.commitTransaction();
        IniciarServicio();
        Pausa.setVisibility(View.VISIBLE);
        Inicio.setVisibility(View.INVISIBLE);
        editor = archivo.edit();
        editor.putLong("tiempo_reparacion", new Date().getTime());
        editor.commit();
    }

    private void PausarEtapa() {
        tiempo_cronometro = Cronometro.getMinutos();
        DetenerServicio();
        setParo(true);
        setHoraParo(new Date());

        GeneraRegistroParo(tiempo_cronometro);

        Inicio.setVisibility(View.VISIBLE);
        Pausa.setVisibility(View.INVISIBLE);

        editor = archivo.edit();
        editor.putBoolean("estado", true);
        editor.putLong("tiempo_reparacion", new Date().getTime()-archivo.getLong("tiempo_reparacion", 0));
        editor.commit();
        FragmentDefectos.temporal += archivo.getLong("tiempo_reparacion", 0);

    }

    private void TerminarEtapa() {
        tiempo_cronometro = Cronometro.getMinutos();
        editor = archivo.edit();
        editor.commit();
        DetenerServicio();
        setParo(false);

        GeneraRegistroParo(tiempo_cronometro);
    }

    private void FinalizarProyecto() {
        RellenaEtapas();
        Inicio.setVisibility(View.INVISIBLE);
        Pausa.setVisibility(View.INVISIBLE);
        Detener.setVisibility(View.INVISIBLE);
        Reporte.setVisibility(View.VISIBLE);

        Estado_servicio = ServicioEnEjecucion(Cronometro.class);

        //Se guarda en cache el estado terminado para que el otro fragment no haga funcionalidad
        editor = archivo.edit();
        editor.putBoolean("estado", false);
        editor.commit();
        CambiarEtapa();
    }

    private void GeneraRegistroParo(int delta_Tiempo){
        Date paro = new Date();
        String hora_paro = new SimpleDateFormat("HH:mm").format(paro);
        Registro_Tiempos registro = proyecto.getBitacora_tiempos().last();
        realm.beginTransaction();
        registro.setDelta_Tiempo(delta_Tiempo);
        registro.setHora_paro(hora_paro);
        registro.setFase(Etapas[ObtenerEtapa()]);
        realm.commitTransaction();
    }

    private void Rellena_Tiempo(long tiempo){
        int minutos_interrupcion = (int) (tiempo/999);
        Registro_Tiempos relleno = proyecto.getBitacora_tiempos().last();
        realm.beginTransaction();
        relleno.setTiempo_Interrumpcion(minutos_interrupcion);
        realm.commitTransaction();
    }

    private void RellenaEtapas(){
        realm.beginTransaction();
        proyecto.setPlaneacion(archivo.getInt(Etapas[0], 0));
        proyecto.setDiseño(archivo.getInt(Etapas[1],0));
        proyecto.setCodificacion(archivo.getInt(Etapas[2],0));
        proyecto.setCompilacion(archivo.getInt(Etapas[3], 0));
        proyecto.setPruebas(archivo.getInt(Etapas[4],0));
        realm.commitTransaction();
    }


    private void CambiarEtapa(){
        editor = archivo.edit();
        editor.putInt("etapa", ObtenerEtapa()+1);
        editor.putInt("tiempo_estimado", 0);
        editor.commit();
        RegistroPspActivity.viewPager.getAdapter().notifyDataSetChanged();
    }

    private void GuardarProyectoCaptura(int id){
        editor = general.edit();
        editor.putInt("proyecto", id);
        editor.commit();
    }

    private int ObtenerProyectoCaptura(){
        return general.getInt("proyecto", 0);
    }

    private int ObtenerEtapa(){
        indice = archivo.getInt("etapa", 0);
        return indice;
    }

    private void GuardarEstimado(){
        editor = archivo.edit();
        editor.putInt("tiempo_estimado", tiempo_estimado);
        editor.putInt(Etapas[ObtenerEtapa()], tiempo_estimado);
        editor.commit();
    }

    private void setParo(boolean Paro){
        editor = archivo.edit();
        editor.putBoolean("paro", Paro);
        editor.commit();
    }

    private void setHoraParo(Date horaParo){
        editor = archivo.edit();
        editor.putLong("hora_pausa", horaParo.getTime());
        editor.commit();
    }

    private void IniciarServicio(){
        Intent intent = new Intent(getActivity().getApplicationContext(), Cronometro.class);
        intent.putExtra("id", id);
        intent.putExtra("proyecto", proyecto.getNombre_Proyecto());
        intent.putExtra("etapa", etiquetaEtapa.getText().toString());
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            getActivity().getApplicationContext().startForegroundService(intent);
        }else{
            getActivity().getApplicationContext().startService(intent);
        }
    }

    private void DetenerServicio(){
        Intent intentstop = new Intent(getActivity().getApplicationContext(), Cronometro.class);
        getActivity().getApplicationContext().stopService(intentstop);
    }

    private void ActualizarEtapa(){
        etiquetaEtapa.setText(Etapas[ObtenerEtapa()]);
    }
}
