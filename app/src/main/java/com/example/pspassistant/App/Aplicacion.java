package com.example.pspassistant.App;

import android.app.Application;

import com.example.pspassistant.Modelos.Proyecto;
import com.example.pspassistant.Modelos.Registro_Defectos;
import com.example.pspassistant.Modelos.Registro_Tiempos;

import java.util.concurrent.atomic.AtomicInteger;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmObject;
import io.realm.RealmResults;

public class Aplicacion extends Application {

    public static AtomicInteger ProyectoId = new AtomicInteger();
    public static AtomicInteger TiemposId = new AtomicInteger();
    public static AtomicInteger DefectosId = new AtomicInteger();

    @Override
    public void onCreate() {
        super.onCreate();
        ConfiguraRealm();
        Realm realm = Realm.getDefaultInstance();
        ProyectoId = getId("Id_Proyecto", realm, Proyecto.class);
        TiemposId = getId("Id_tiempo", realm, Registro_Tiempos.class);
        DefectosId = getId("Id_defecto", realm, Registro_Defectos.class);
        realm.close();
    }

    private void ConfiguraRealm(){
        Realm.init(this);
        RealmConfiguration configuration = new RealmConfiguration
                .Builder()
                .name("PSPAssistant.realm")
                .build();
        Realm.setDefaultConfiguration(configuration);
    }

    private <T extends RealmObject> AtomicInteger getId(String campo, Realm realm, Class<T> clase){
        RealmResults<T> resultados = realm.where(clase).findAll();
        return (resultados.size() > 0)? new AtomicInteger(resultados.max(campo).intValue()) : new AtomicInteger();
    }

}
