package com.example.pspassistant.Modelos;

import com.example.pspassistant.App.Aplicacion;

import java.text.SimpleDateFormat;
import java.util.Date;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;
import io.realm.annotations.Required;

public class Registro_Tiempos extends RealmObject {

    @PrimaryKey
    private int Id_tiempo;
    @Required
    private String Fecha_Registro;
    @Required
    private String Hora_inicio;
    private String Hora_paro;
    private int Tiempo_Interrumpcion;
    private int Delta_Tiempo;
    private String Fase;
    private String Comentario;

    public Registro_Tiempos(){ }

    public Registro_Tiempos(Date Fecha){
        this.Id_tiempo = Aplicacion.TiemposId.incrementAndGet();
        this.Fecha_Registro = new SimpleDateFormat("dd-MM-yyyy").format(Fecha);
        this.Hora_inicio = new SimpleDateFormat("HH:mm").format(Fecha);
    }

    public int getId_tiempo() {
        return Id_tiempo;
    }

    public String getFecha_Registro() {
        return Fecha_Registro;
    }

    public String getHora_inicio() {
        return Hora_inicio;
    }


    public int getTiempo_Interrumpcion() {
        return Tiempo_Interrumpcion;
    }

    public void setTiempo_Interrumpcion(int tiempo_Interrumpcion){
        this.Tiempo_Interrumpcion = tiempo_Interrumpcion;
    }

    public int getDelta_Tiempo() {
        return Delta_Tiempo;
    }

    public void setDelta_Tiempo(int delta_Tiempo) {
        Delta_Tiempo = delta_Tiempo;
    }

    public String getHora_paro() {
        return Hora_paro;
    }

    public void setHora_paro(String hora_paro) {
        Hora_paro = hora_paro;
    }

    public String getFase() {
        return Fase;
    }

    public void setFase(String fase){
        this.Fase = fase;
    }

    public String getComentario() {
        return Comentario;
    }

    public void setComentario(String comentario) {
        Comentario = comentario;
    }
}
