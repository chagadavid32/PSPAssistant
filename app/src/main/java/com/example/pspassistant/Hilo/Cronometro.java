package com.example.pspassistant.Hilo;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.widget.Toast;

import com.example.pspassistant.Activities.RegistroPspActivity;
import com.example.pspassistant.R;

import java.util.Timer;
import java.util.TimerTask;

import io.realm.Realm;

public class Cronometro extends Service {

    private static final String CHANNELL_ID = "A";
    private static int minutos = -1;
    public static Timer contador;

    @Override
    public void onDestroy() {
        contador.cancel();
        minutos = -1;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        contador = new Timer();
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            CharSequence nombre = getString(R.string.Titulo_Canal);
            String descripcion = getString(R.string.Descripcion_Canal);
            int importancia = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel canal = new NotificationChannel(CHANNELL_ID, nombre, importancia);
            canal.setDescription(descripcion);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(canal);
        }
    }

    public static int getMinutos(){
        return minutos;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        int id = intent.getExtras().getInt("id");
        String nombre = intent.getExtras().getString("proyecto");
        String etapa = intent.getExtras().getString("etapa");

        Intent intentNotificacion = new Intent(getBaseContext(), RegistroPspActivity.class);
        intentNotificacion.putExtra("id", id);
        PendingIntent pendingIntent = PendingIntent.getActivity(getBaseContext(), 0, intentNotificacion, PendingIntent.FLAG_UPDATE_CURRENT);


        Notification notificacion = new NotificationCompat.Builder(this, CHANNELL_ID)
                .setContentTitle(nombre) //Nombre del proyecto
                .setContentText(etapa)  //etapa
                .setSmallIcon(R.drawable.ic_icono)
                .setContentIntent(pendingIntent)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setAutoCancel(true)
                .build();

        startForeground(id, notificacion);

        contador.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                minutos++;
            }
        },0,999);

        return START_NOT_STICKY;
    }


}
