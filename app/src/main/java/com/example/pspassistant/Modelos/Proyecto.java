package com.example.pspassistant.Modelos;

import com.example.pspassistant.App.Aplicacion;

import java.text.SimpleDateFormat;
import java.util.Date;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;
import io.realm.annotations.Required;

public class Proyecto extends RealmObject {

    @PrimaryKey
    private int Id_Proyecto;
    @Required
    private String Nombre_Proyecto;
    private String Nombre_Estudiante;
    private String Nombre_Instructor;
    @Required
    private String Fecha_Creacion;
    @Required
    private String Lenguaje;
    private int Planeación;
    private int Diseño;
    private int Codificación;
    private int Compilación;
    private int Pruebas;
    private int LOC;
    private RealmList<Registro_Tiempos> bitacora_tiempos = new RealmList<Registro_Tiempos>();
    private RealmList<Registro_Defectos> bitacora_defectos = new RealmList<Registro_Defectos>();


    public Proyecto(){ }

    public Proyecto(String nombre_Proyecto, String lenguaje, Date fecha_Creacion){
        this.Id_Proyecto = Aplicacion.ProyectoId.incrementAndGet();
        this.Nombre_Proyecto = nombre_Proyecto;
        this.Lenguaje = lenguaje;
        this.Fecha_Creacion = new SimpleDateFormat("dd-MM-yyyy").format(fecha_Creacion);
    }

    public int getId_Proyecto() {
        return Id_Proyecto;
    }

    public String getNombre_Proyecto() {
        return Nombre_Proyecto;
    }

    public String getNombre_Instructor() {
        return Nombre_Instructor;
    }

    public void setNombre_Instructor(String nombre_Instructor) {
        Nombre_Instructor = nombre_Instructor;
    }

    public String getFecha_Creacion() {
        return Fecha_Creacion;
    }

    public String getLenguaje() {
        return Lenguaje;
    }

    public int getPlaneacion() {
        return Planeación;
    }

    public void setPlaneacion(int planeacion) {
        Planeación = planeacion;
    }

    public int getDiseño() {
        return Diseño;
    }

    public void setDiseño(int diseño) {
        Diseño = diseño;
    }

    public int getCodificacion() {
        return Codificación;
    }

    public void setCodificacion(int codificacion) {
        Codificación = codificacion;
    }

    public int getCompilacion() {
        return Compilación;
    }

    public void setCompilacion(int compilacion) {
        Compilación = compilacion;
    }

    public int getPruebas() {
        return Pruebas;
    }

    public void setPruebas(int pruebas) {
        Pruebas = pruebas;
    }

    public RealmList<Registro_Tiempos> getBitacora_tiempos() {
        return bitacora_tiempos;
    }

    public void setBitacora_tiempos(Registro_Tiempos tiempos) {
        bitacora_tiempos.add(tiempos);
    }

    public RealmList<Registro_Defectos> getBitacora_defectos() {
        return bitacora_defectos;
    }

    public void setBitacora_defectos(Registro_Defectos bitacora_defectos) {
        this.bitacora_defectos.add(bitacora_defectos);
    }

    public void setLOC(int loc){
        this.LOC = loc;
    }

    public int getLOC(){
        return LOC;
    }
}

