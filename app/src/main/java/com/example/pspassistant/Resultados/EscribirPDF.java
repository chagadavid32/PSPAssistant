package com.example.pspassistant.Resultados;

import android.Manifest;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.widget.Toast;

import com.example.pspassistant.Fragments.FragmentDefectos;
import com.example.pspassistant.Modelos.Proyecto;
import com.example.pspassistant.Modelos.Registro_Defectos;
import com.example.pspassistant.Modelos.Registro_Tiempos;
import com.itextpdf.text.Document;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;

import io.realm.Realm;
import io.realm.RealmList;
import io.realm.RealmResults;

public class EscribirPDF {
    private int id;
    private Realm realm;
    private Proyecto proyecto;
    private RealmList<Registro_Defectos> defectos;
    private RealmList<Registro_Tiempos> tiempos;
    private SharedPreferences general;

    private Context contexto;
    private String nombre_proyecto;
    private File archivopdf;
    private Document documento;
    private PdfWriter writer;
    private Paragraph parrafo;
    private PdfPTable tabla;
    private PdfPCell celda;
    private Font encabezado = new Font(Font.FontFamily.TIMES_ROMAN, 14);
    private Font titulotabla = new Font(Font.FontFamily.TIMES_ROMAN, 12, Font.BOLD);
    private Font contenidotabla = new Font(Font.FontFamily.TIMES_ROMAN, 12);
    private String[] etapas = {"Planeación", "Diseño", "Codificación", "Compilación", "Pruebas"};
    private ArrayList<Integer> estimados = new ArrayList<Integer>();

    private String Plan = "Tabla C14 Resumen del Plan del Proyecto";
    private String Tiempos = "Tabla C16 Bitácora de Registro de Tiempos";
    private String Defectos = "Tabla C18 Bitácora de Registro de Defectos";

    private int tiempototal ;
    private int totaldefectos;

    private Activity activity;

    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };


    public EscribirPDF(Context contexto, int id, SharedPreferences general, Activity activity){
        this.contexto = contexto;
        this.id = id;
        this.activity = activity;
        realm = Realm.getDefaultInstance();
        proyecto = realm.where(Proyecto.class).equalTo("Id_Proyecto", this.id).findFirst();
        defectos = proyecto.getBitacora_defectos();
        tiempos = proyecto.getBitacora_tiempos();
        this.general = general;
        estimados.add(proyecto.getPlaneacion());
        estimados.add(proyecto.getDiseño());
        estimados.add(proyecto.getCodificacion());
        estimados.add(proyecto.getCompilacion());
        estimados.add(proyecto.getPruebas());
        tiempototal = calculartotal();
    }

    private void CreaDirectorio(){
        File Directorio = new File(Environment.getExternalStorageDirectory().toString(), "Registros PSP Assistant");
        if(!Directorio.exists()){
            Directorio.mkdir();

        }
        nombre_proyecto = proyecto.getNombre_Proyecto();
        archivopdf = new File(Directorio, "PSP_Assistant_"+nombre_proyecto+".pdf");
    }

    public void AbrirDocumento(){
        CreaDirectorio();
        try{
            documento = new Document(PageSize.A4);
            writer = PdfWriter.getInstance(documento, new FileOutputStream(archivopdf));
            documento.open();
        }catch (Exception e){ }
    }

    public static void verifyStoragePermissions(Activity activity) {
        // Check if we have read or write permission
        int writePermission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        int readPermission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.READ_EXTERNAL_STORAGE);

        if (writePermission != PackageManager.PERMISSION_GRANTED || readPermission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(
                    activity,
                    PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE
            );
        }
    }

    public void setMetadatos(String autor){
        documento.addTitle("PSP Asistant "+nombre_proyecto);
        documento.addAuthor(autor);
    }

    public void EncabezadoDoumento(){
        try{
            String estudiante = general.getString("nombre", "");
            parrafo = new Paragraph();
            ParrafoHijo(new Paragraph("Nombre del estudiante: "+estudiante+"        Fecha: "+proyecto.getFecha_Creacion(), encabezado));
            ParrafoHijo(new Paragraph("Programa: "+proyecto.getNombre_Proyecto()+"  No. de Prog. "+proyecto.getId_Proyecto(), encabezado));
            ParrafoHijo(new Paragraph("Instructor: "+proyecto.getNombre_Instructor()+"      Lenguaje: "+proyecto.getLenguaje(), encabezado));
            documento.add(parrafo);
        }catch (Exception ex){
            Log.e("EncabezadDocumento", ex.toString());
        }
    }

    private void ParrafoHijo(Paragraph hijo){
        hijo.setAlignment(Element.ALIGN_JUSTIFIED);
        parrafo.add(hijo);
    }

    public void TablaPlan(){
        try{
            String[] encabezados;
            EncabezadoTabla(Plan);
            encabezados = new String[]{"Tiempo en la fase(min.)", "Planeado", "Actual", "A la fecha", "% A la fecha"};
            EscribirTablaPlan(encabezados);
            parrafo = new Paragraph();
            parrafo.setSpacingBefore(20);
            parrafo.setSpacingBefore(10);
            documento.add(parrafo);
            encabezados = new String[]{"Defectos introducidos", "Actual", "A la fecha", "% A la fecha"};
            DefectosPlan(encabezados, "Etapa_Introduccion");
            parrafo = new Paragraph();
            parrafo.setSpacingBefore(20);
            parrafo.setSpacingBefore(10);
            documento.add(parrafo);
            encabezados = new String[]{"Defectos removidos", "Actual", "A la fecha", "% A la fecha"};
            DefectosPlan(encabezados, "Etapa_Removido");

        }catch (Exception ex){
            Log.e("TablaPlan", ex.toString());
        }
    }

    private void EscribirTablaPlan(String[] encabezados){
        try{
            GeneraEncabezados(encabezados);
            //Se llena el resto de la tabla
            int planeado = 0;
            int deltatiempo = 0;
            int actualtotal = calculartotal();
            double porcentaje;
            double porcentajetotal = 0;
            for (int fila = 0; fila < etapas.length; fila++){
                GeneraCelda(etapas[fila]);
                planeado = estimados.get(fila);
                GeneraCelda(planeado+""); //planeado
                RealmResults<Registro_Tiempos> tiempos1 = realm.where(Registro_Tiempos.class).equalTo("Fase", etapas[fila]).findAll();
                deltatiempo = tiempos1.sum("Delta_Tiempo").intValue();
                GeneraCelda(deltatiempo+"");//Actual
                GeneraCelda(deltatiempo+"");//A la fecha

                porcentaje = (deltatiempo*100)/actualtotal;
                porcentajetotal += porcentaje;
                GeneraCelda(porcentaje+"");

            }
            GeneraCelda("Total");
            int totalplaneado = proyecto.getPlaneacion()+proyecto.getDiseño()+proyecto.getCodificacion()+proyecto.getCompilacion()+proyecto.getPruebas();
            GeneraCelda(totalplaneado+""); //totalPlaneado
            GeneraCelda(actualtotal+"");
            GeneraCelda(actualtotal+"");
            GeneraCelda(porcentajetotal+"");

            parrafo.add(tabla);
            documento.add(parrafo);
        }catch (Exception ex){
            Log.e("EscribirTablaPlan", ex.toString());
        }

    }

    private int calculartotal(){
        int deltatiempo;
        int actualtotal = 0;
        for (int i = 0; i < etapas.length; i++){
            RealmResults<Registro_Tiempos> tiempos1 = realm.where(Registro_Tiempos.class).equalTo("Fase", etapas[i]).findAll();
            deltatiempo = tiempos1.sum("Delta_Tiempo").intValue();
            actualtotal += deltatiempo;
        }
        return actualtotal;
    }

    private void DefectosPlan(String[] encabezados, String etapa){
        try{
            GeneraEncabezados(encabezados);
            int defectostotales;
            int totaldefectos = calculardefectos(etapa);
            double porcentaje;
            double porcentajetotal = 0;
            for (int fila = 0; fila < etapas.length; fila++){
                GeneraCelda(etapas[fila]);
                RealmResults<Registro_Defectos> defectosintroducidos =
                        realm.where(Registro_Defectos.class).equalTo(etapa, etapas[fila]).findAll();
                defectostotales = defectosintroducidos.size();

                GeneraCelda(defectostotales+""); //Actual
                GeneraCelda(defectostotales+""); //A la fecha

                porcentaje = (defectostotales*100)/totaldefectos;
                porcentajetotal += porcentaje;
                GeneraCelda(porcentaje+"");
            }
            GeneraCelda("Total del desarrollo");
            GeneraCelda(totaldefectos+"");
            GeneraCelda(totaldefectos+"");
            GeneraCelda(porcentajetotal+"");

            parrafo.add(tabla);
            documento.add(parrafo);
        }catch (Exception ex){
            Log.e("DefectosPlan", ex.toString());
        }

    }

    private int calculardefectos(String etapa){
        int defectostotales;
        totaldefectos = 0;
        for (int fila = 0; fila < etapas.length; fila++) {
            RealmResults<Registro_Defectos> defectosintroducidos =
                    realm.where(Registro_Defectos.class).equalTo(etapa, etapas[fila]).findAll();
            defectostotales = defectosintroducidos.size();
            totaldefectos += defectostotales;
        }
        return totaldefectos;
    }

    private void GeneraEncabezados(String[] encabezados){
        parrafo = new Paragraph();
        parrafo.setFont(titulotabla);
        tabla = new PdfPTable(encabezados.length);
        tabla.setWidthPercentage(100);
        int columnas = 0;
        while(columnas < encabezados.length){
            celda = new PdfPCell(new Phrase(encabezados[columnas++], contenidotabla));
            celda.setHorizontalAlignment(Element.ALIGN_LEFT);
            tabla.addCell(celda);
        }
    }

    private void GeneraCelda(String frase){
        celda = new PdfPCell(new Phrase(frase, contenidotabla));
        celda.setHorizontalAlignment(Element.ALIGN_LEFT);
        tabla.addCell(celda);
    }

    public void EncabezadoTabla(String Encabezado){
        try{
            parrafo = new Paragraph(Encabezado, titulotabla);
            parrafo.setSpacingAfter(3);
            parrafo.setSpacingBefore(5);
            documento.add(parrafo);
        }catch (Exception ex){
            Log.e("EncabezadoTabla", ex.toString());
        }
    }

    public void EscribirBitacoraTiempos(String[] encabezados){
        try{
            EncabezadoTabla(Tiempos);
            GeneraEncabezados(encabezados);
            Registro_Tiempos registo;
            for (int indice = 0; indice < tiempos.size(); indice++){
                registo = tiempos.get(indice);
                GeneraCelda(registo.getFecha_Registro());
                GeneraCelda(registo.getHora_inicio());
                GeneraCelda(registo.getHora_paro());
                GeneraCelda(registo.getTiempo_Interrumpcion()+"");
                GeneraCelda(registo.getDelta_Tiempo()+"");
                GeneraCelda(registo.getFase());
                GeneraCelda(registo.getComentario());
            }
            parrafo.add(tabla);
            documento.add(parrafo);
        }catch(Exception ex){
            Log.e("EscribirBitacoraTiempos", ex.toString());
        }
    }

    public void EscribirBitacoraDefectos(String[] encabezados){
        try{
            EncabezadoTabla(Defectos);
            Registro_Defectos defecto;
            parrafo = new Paragraph();
            parrafo.setFont(titulotabla);
            tabla = new PdfPTable(encabezados.length);
            tabla.setWidthPercentage(100);
            for (int i = 0; i < defectos.size(); i++){
                titulos(encabezados);
                defecto = defectos.get(i);
                AgregarCeldas(defecto);
                GeneraCelda("Descripción");
                descripcion(defecto.getDescripcion());
            }
            parrafo.add(tabla);
            documento.add(parrafo);
        }catch (Exception ex){
            Log.e("EscribirBitaDefectos", ex.toString());
        }
    }

    private void descripcion(String descripcion){
        parrafo = new Paragraph(descripcion, contenidotabla);
        PdfPCell celda = new PdfPCell(parrafo);
        celda.setColspan(5);
        tabla.addCell(celda);
    }

    private void titulos(String[] titulos){
        int columnas = 0;
        while(columnas < titulos.length){
            celda = new PdfPCell(new Phrase(titulos[columnas++], contenidotabla));
            celda.setHorizontalAlignment(Element.ALIGN_LEFT);
            tabla.addCell(celda);
        }
    }

    public void AgregarCeldas(Registro_Defectos defecto){
        GeneraCelda(defecto.getFecha_Registro());
        GeneraCelda(defecto.getId_defecto()+"");
        GeneraCelda(defecto.getTipo());
        GeneraCelda(defecto.getEtapa_Introduccion());
        GeneraCelda(defecto.getEtapa_Removido());
        GeneraCelda(defecto.getTiempo_Reparacion()+"");
    }

    public void Fin (){
        try{
            parrafo = new Paragraph("Tiempo total de desarrollo en minutos: "+tiempototal);
            documento.add(parrafo);
            parrafo = new Paragraph("Produccion de LOC por hora: "+proyecto.getLOC()/60);
            documento.add(parrafo);
            parrafo = new Paragraph("Etapa con mas tiempo: "+etapa());
            documento.add(parrafo);
            parrafo = new Paragraph("Etapa con mas defectos introducidos: "+etapadefectos("Etapa_Introduccion"));
            documento.add(parrafo);
            parrafo = new Paragraph("Etapa con mas defectos reparados: "+etapadefectos("Etapa_Removido"));
            documento.add(parrafo);
            parrafo = new Paragraph("Tipo de defecto mas introducido: "+tipo());
            documento.add(parrafo);
            parrafo = new Paragraph("Tiempo promedio reparando defectos: "+sumatiempoefectos()/totaldefectos);
            documento.add(parrafo);
        }catch(Exception ex){
            Log.e("Fin", ex.toString());
        }
    }

    private int sumatiempoefectos(){
        int suma = 0;
        for (int i = 0; i < etapas.length; i++) {
            suma += realm.where(Registro_Defectos.class).equalTo("Etapa_Removido", etapas[i]).sum("Tiempo_Reparacion").intValue();
        }
        return suma;
    }

    private String tipo(){
        int cantidad = 0;
        String defecto = "";
        for (int i = 0; i < FragmentDefectos.TiposDefectos.size(); i++){
            RealmResults<Registro_Defectos> temporal = realm.where(Registro_Defectos.class).equalTo("Tipo",
                    i+"0").findAll();
            if(temporal.size() > cantidad){
                cantidad = temporal.size();
                defecto = FragmentDefectos.TiposDefectos.get(i).toString();
            }
        }
        return defecto;
    }

    private String etapadefectos(String etapa){
        int cantidad = 0;
        String etapar = "";
        for (int i = 0; i < etapas.length; i++){
            RealmResults<Registro_Defectos> temporal = realm.where(Registro_Defectos.class).equalTo(etapa, etapas[i]).findAll();
            if(temporal.size() > cantidad){
                cantidad = temporal.size();
                etapar = etapas[i];
            }
        }
        return etapar;
    }

    private String etapa(){
        int tiempototal = 0;
        String etapa = "";
        for (int i = 0; i < etapas.length; i++){
            int temporal = realm.where(Registro_Tiempos.class).equalTo("Fase", etapas[i]).sum("Delta_Tiempo").intValue();
            if(temporal > tiempototal){
                tiempototal = temporal;
                etapa = etapas[i];
            }
        }
        return etapa;
    }


    public void CierraDocumento(){
        documento.close();
    }

    public void VerArchivo(){
        if(archivopdf.exists()){
            Uri uri = Uri.fromFile(archivopdf);
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(uri, "application/pdf");
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            try{
                contexto.startActivity(intent);
            }catch(ActivityNotFoundException ex){
                Toast.makeText(contexto, "No tienes ninguna app para ver el archivo", Toast.LENGTH_SHORT).show();
            }
        }else{
            Toast.makeText(contexto, "No existe", Toast.LENGTH_SHORT).show();
        }
    }
}
