package com.example.pspassistant.Modelos;

import com.example.pspassistant.App.Aplicacion;

import java.text.SimpleDateFormat;
import java.util.Date;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;
import io.realm.annotations.Required;

public class Registro_Defectos extends RealmObject {

    @PrimaryKey
    private int Id_defecto;
    @Required
    private String Fecha_Registro;

    private String Tipo;
    @Required
    private String Etapa_Introduccion;
    @Required
    private String Etapa_Removido;
    private int Tiempo_Reparacion;
    private String Descripcion;

    public Registro_Defectos(){ }

    public Registro_Defectos(Date Fecha, String tipo, String etapa_Introduccion, String etapa_Removido){
        this.Id_defecto = Aplicacion.DefectosId.incrementAndGet();
        this.Fecha_Registro = new SimpleDateFormat("dd-MM-yyyy").format(Fecha);
        this.Tipo = tipo;
        this.Etapa_Introduccion = etapa_Introduccion;
        this.Etapa_Removido = etapa_Removido;
    }

    public int getId_defecto() {
        return Id_defecto;
    }

    public String getFecha_Registro() {
        return Fecha_Registro;
    }

    public String getTipo() {
        return Tipo;
    }

    public String getEtapa_Introduccion() {
        return Etapa_Introduccion;
    }

    public String getEtapa_Removido() {
        return Etapa_Removido;
    }

    public int getTiempo_Reparacion() {
        return Tiempo_Reparacion;
    }

    public void setTiempo_Reparacion(int tiempo_Reparacion) {
        Tiempo_Reparacion = tiempo_Reparacion;
    }

    public String getDescripcion() {
        return Descripcion;
    }

    public void setDescripcion(String descripcion) {
        Descripcion = descripcion;
    }
}
